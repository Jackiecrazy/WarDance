package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class EntityHandler {
    public static final HashMap<PlayerEntity, Entity> mustUpdate = new HashMap<>();
    public static final ConcurrentHashMap<Tuple<World, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void start(FMLServerStartingEvent e) {
        mustUpdate.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void stop(FMLServerStoppingEvent e) {
        mustUpdate.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity) {
            e.addCapability(new ResourceLocation("wardance:combatinfo"), new CombatData((LivingEntity) e.getObject()));
            e.addCapability(new ResourceLocation("wardance:statuseffects"), new Marks((LivingEntity) e.getObject()));
            if (e.getObject() instanceof PlayerEntity)
                e.addCapability(new ResourceLocation("wardance:casterinfo"), new CasterData((LivingEntity) e.getObject()));
        }
    }

    @SubscribeEvent
    public static void takeThis(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof MobEntity) {
            MobEntity mob = (MobEntity) e.getEntity();
            mob.goalSelector.addGoal(-1, new NoGoal(mob));
            mob.targetSelector.addGoal(-1, new NoGoal(mob));
        } else if (e.getEntity() instanceof ServerPlayerEntity) {
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e.getEntity()), new SyncSkillPacket(CasterData.getCap((LivingEntity) e.getEntity()).write()));
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.Clone e) {
        CasterData.getCap(e.getPlayer()).read(CasterData.getCap(e.getOriginal()).write());
        if (!e.isWasDeath()) {
            final ICombatCapability icc = CombatData.getCap(e.getPlayer());
            icc.read(CombatData.getCap(e.getOriginal()).write());

            icc.setFatigue(0);
            icc.setBurnout(0);
            icc.setWounding(0);
        }
        //CasterData.getCap(e.getPlayer()).read(CasterData.getCap(e.getOriginal()).write());
        ISkillCapability cap = CasterData.getCap(e.getPlayer());
        cap.setEquippedSkills(CasterData.getCap(e.getOriginal()).getEquippedSkills());
        for (Skill s : cap.getEquippedSkills())
            if (s != null) {
                s.onEquip(e.getPlayer());
            }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.player.isAlive() && e.phase == TickEvent.Phase.START) {
            if (e.side != LogicalSide.SERVER) {
                CombatData.getCap(e.player).clientTick();
                return;
            } else CombatData.getCap(e.player).serverTick();
            CasterData.getCap(e.player).update();
            Marks.getCap(e.player).update();
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingUpdateEvent e) {
        LivingEntity elb = e.getEntityLiving();
        if (!elb.world.isRemote) {
            if (elb.isPotionActive(WarEffects.PETRIFY.get())) {
                elb.rotationPitch = elb.prevRotationPitch;
                elb.rotationYaw = elb.prevRotationYaw;
                elb.rotationYawHead = elb.prevRotationYawHead;
                elb.setMotion(0, 0, 0);
            }
            if (!(elb instanceof PlayerEntity)) {
                Marks.getCap(elb).update();
                        /*
                        The battle circle AI basically works like this (from an enemy's perspective):
First, walk towards the player until I get within a "danger" radius
While in "danger" mode, don't get too close to another enemy, unless I am given permission to attack the player.
Also while in "danger" mode, try to approach the player. If there are too many enemies in my way, I will effectively not be able to reach the player until the enemies move or the player moves.
When the player is in my "attack" radius (roughly the maximum range of my attack) ask the player if I'm allowed to attack. If so, add me to the list of current attackers on the player object.
If there are already the maximum allowed number of attackers on the list, I'm denied permission.
If I'm denied permission, try strafing for a second or two in a random direction until I'm given permission.
If the player moves out of attack range—even if I'm attacking—remove me from the attacker list.
If I die, or am stunned or otherwise unable to attack, remove me from the attacker list.
The maximum allowed number of simultaneous attackers is critical in balancing your battle circle. A higher number causes an exponential increase in pressure. In the example demo I have it set at 2; less twitchy and more "cinematic" games set it at 1. If you put this number too high, you defeat the purpose of the circle, because large groups of enemies become unassailable or can only be defeated with uninteresting poke-and-run tactics.
Of similar importance is the enemy attack rate. This is not the fastest possible attack rate of the enemy, but how often they will choose to attack when given permission.
As you would expect, a lower number increases pressure, but you should generally have this be several times higher than the real attack rate. You can make this rate a bit more unpredictable (and thus the amount of pressure slightly less predictable) by increasing attackRateFluctuation, which will increase or decrease the attack rate after each attack.
Mobs should move into a position that is close to the player, far from allies, and close to them.
                         */
                //staggered mobs bypass update interval
                ICombatCapability cap = CombatData.getCap(elb);
                if (cap.getStaggerTime() > 0 || mustUpdate.containsValue(e.getEntity()))
                    cap.serverTick();
            }
        }
    }

    @SubscribeEvent
    public static void noJump(LivingEvent.LivingJumpEvent e) {
        if (!(e.getEntityLiving() instanceof PlayerEntity) && CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0) {
            e.getEntityLiving().setMotion(0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void sneak(final LivingEvent.LivingVisibilityEvent e) {
        if (e.getLookingEntity() != e.getEntityLiving() && e.getLookingEntity() instanceof LivingEntity && StealthConfig.stealthSystem) {
            double mult = 1;
            LivingEntity sneaker = e.getEntityLiving(), watcher = (LivingEntity) e.getLookingEntity();
            if (sneaker.getFireTimer() > 0) return;//you're on fire and it's super obvious!
            CombatUtils.StealthData sd = CombatUtils.stealthMap.getOrDefault(watcher.getType().getRegistryName(), CombatUtils.STEALTH);
            if (sd.isVigilant()) return;
            if (watcher.getAttackingEntity() != sneaker && watcher.getRevengeTarget() != sneaker && watcher.getLastAttackedEntity() != sneaker && (!(watcher instanceof MobEntity) || ((MobEntity) watcher).getAttackTarget() != sneaker)) {
                double stealth = GeneralUtils.getAttributeValueSafe(sneaker, WarAttributes.STEALTH.get());
                if (watcher.isPotionActive(Effects.BLINDNESS))
                    mult *= (1f / (watcher.getActivePotionEffect(Effects.BLINDNESS).getAmplifier() + 4));
                if (stealth > 20)
                    mult *= (10 / (stealth - 10));
                if (!sd.isAllSeeing() && !GeneralUtils.isFacingEntity(watcher, sneaker, Math.max(StealthConfig.baseHorizontalDetection, (int) ((20 - stealth) * StealthConfig.anglePerArmor)), Math.max(StealthConfig.baseVerticalDetection, (int) ((20 - stealth) * (20 - stealth)))))
                    mult *= (0.3);
                if (!sd.isPerceptive()) {
                    final double speedSq = GeneralUtils.getSpeedSq(sneaker);
                    mult *= (0.5 + MathHelper.sqrt(speedSq) * 2);
                }
                if (!sd.isNightVision() && !watcher.isPotionActive(Effects.NIGHT_VISION) && !sneaker.isPotionActive(Effects.GLOWING)) {
                    World world = sneaker.world;
                    if (world.isAreaLoaded(sneaker.getPosition(), 5) && world.isAreaLoaded(watcher.getPosition(), 5)) {
                        final int light = world.getLight(sneaker.getPosition());
                        float lightMalus = MathHelper.clamp((13 - light) * 0.1f, 0, 1);
                        if (!sd.isDeaf())
                            lightMalus *= Math.min(stealth / 20f, 1f);
                        mult *= (1 - lightMalus);
                    }
                }
            }
            if (!sd.isAllSeeing() && !watcher.canEntityBeSeen(sneaker))
                mult *= (0.4);
            e.modifyVisibility(Math.min(mult, 1));
        }
    }

//    @SubscribeEvent
//    public static void kitCheck(LivingEvent.LivingUpdateEvent e) {
//        final LivingEntity le = e.getEntityLiving();
//        if (le instanceof PlayerEntity) {
//            if (GeneralUtils.isKitMain(le.getHeldItemMainhand())) {
//                ItemStack is = le.getHeldItemMainhand();
//                is.getOrCreateTag().put("kitItem", le.getHeldItemOffhand().write(new CompoundNBT()));
//                //this is called before kitUp, so this quickly overrides the latter
//            }
//        }
//    }

//    @SubscribeEvent
//    public static void kitUp(LivingEquipmentChangeEvent e) {
//        if (e.getSlot() == EquipmentSlotType.MAINHAND && !ItemStack.areItemsEqual(e.getFrom(), e.getTo())) {
//            ItemStack from = e.getFrom(), to = e.getTo();
//            final LivingEntity elb = e.getEntityLiving();
//            if (GeneralUtils.isKitMain(from)) {
//                //interesting, it's being written, then removed immediately.
//                e.getFrom().getOrCreateTag().put("kitItem", elb.getHeldItemOffhand().write(new CompoundNBT()));
//                elb.setHeldItem(Hand.OFF_HAND, CombatData.getCap(elb).getTempItemStack());
//            }
//            if (GeneralUtils.isKitMain(to)) {
//                CombatData.getCap(elb).setTempItemStack(elb.getHeldItemOffhand());
//                ItemStack replace = ItemStack.read(to.getOrCreateTag().getCompound("kitItem"));
//                elb.setHeldItem(Hand.OFF_HAND, replace);
//            }
//        }
//    }

    @SubscribeEvent
    public static void pray(LivingSetAttackTargetEvent e) {
        if (e.getTarget() == null) return;
        if (!(e.getEntityLiving() instanceof MobEntity)) return;
        final MobEntity mob = (MobEntity) e.getEntityLiving();
        if (mob.isPotionActive(WarEffects.FEAR.get()) || mob.isPotionActive(WarEffects.CONFUSION.get()) || mob.isPotionActive(WarEffects.SLEEP.get()))
            mob.setAttackTarget(null);
        if (mob.getRevengeTarget() != e.getTarget() && StealthConfig.stealthSystem && !GeneralUtils.isFacingEntity(mob, e.getTarget(), StealthConfig.baseHorizontalDetection, StealthConfig.baseVerticalDetection)) {
            CombatUtils.StealthData sd = CombatUtils.stealthMap.getOrDefault(mob.getType().getRegistryName(), CombatUtils.STEALTH);
            if (sd.isVigilant() || sd.isAllSeeing() || sd.isOlfactory()) return;
            //outside of LoS, perform luck check. Pray to RNGesus!
            double luckDiff = GeneralUtils.getAttributeValueSafe(e.getTarget(), Attributes.LUCK) - GeneralUtils.getAttributeValueSafe(mob, Attributes.LUCK);
            mob.setAttackTarget(null);
            if (luckDiff <= 0 || WarDance.rand.nextFloat() > (luckDiff / (2 + luckDiff))) {
                //mob.rotateTowards();
                mob.lookAt(EntityAnchorArgument.Type.FEET, e.getTarget().getPositionVec());//.getLookController().setLookPositionWithEntity(e.getTarget(), 0, 0);
            }
        }
    }

    @SubscribeEvent
    public static void nigerundayo(final PotionEvent.PotionAddedEvent e) {
        if (e.getPotionEffect().getPotion() == Effects.BLINDNESS && GeneralConfig.blindness) {
            if (e.getEntityLiving() instanceof MobEntity)
                ((MobEntity) e.getEntityLiving()).setAttackTarget(null);
            e.getEntityLiving().setRevengeTarget(null);
        }
    }

    @SubscribeEvent
    public static void lure(TickEvent.ServerTickEvent e) {
        Iterator<Map.Entry<Tuple<World, BlockPos>, Float>> it = alertTracker.entrySet().iterator();
        {
            //how does a concurrenthashmap throw CME?
            while (it.hasNext()) {
                Map.Entry<Tuple<World, BlockPos>, Float> n = it.next();
                if (n.getKey().getA().isAreaLoaded(n.getKey().getB(), n.getValue().intValue())) {
                    for (CreatureEntity c : (n.getKey().getA().getLoadedEntitiesWithinAABB(CreatureEntity.class, new AxisAlignedBB(n.getKey().getB()).grow(n.getValue())))) {
                        if (CombatUtils.getAwareness(null, c) == CombatUtils.Awareness.UNAWARE && !CombatUtils.stealthMap.getOrDefault(c.getType().getRegistryName(), CombatUtils.STEALTH).isDeaf()) {
                            c.getNavigator().clearPath();
                            c.getNavigator().setPath(c.getNavigator().getPathToPos(n.getKey().getB(), (int) (n.getValue() + 3)), 1);
                            BlockPos vec = n.getKey().getB();
                            c.lookAt(EntityAnchorArgument.Type.EYES, new Vector3d(vec.getX(), vec.getY(), vec.getZ()));
                        }
                    }
                }
            }
        }
        alertTracker.clear();
    }

    private static class NoGoal extends Goal {
        static final EnumSet<Flag> mutex = EnumSet.allOf(Flag.class);
        LivingEntity e;

        NoGoal(LivingEntity bind) {
            e = bind;
        }

        @Override
        public boolean shouldExecute() {
            return (CombatData.getCap(e).isValid() && CombatData.getCap(e).getStaggerTime() > 0) || e.isPotionActive(WarEffects.PETRIFY.get()) || e.isPotionActive(WarEffects.PARALYSIS.get()) || e.isPotionActive(WarEffects.SLEEP.get());
        }

        @Override
        public boolean isPreemptible() {
            return false;
        }

        @Override
        public EnumSet<Flag> getMutexFlags() {
            return mutex;
        }
    }
}
