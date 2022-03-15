package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.goal.GoalCapabilityProvider;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.entity.ai.InvestigateSoundGoal;
import jackiecrazy.wardance.entity.ai.NoGoal;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.networking.UpdateTargetPacket;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

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
            else if (e.getObject() instanceof MobEntity)
                e.addCapability(new ResourceLocation("wardance:targeting"), new GoalCapabilityProvider());
        }
    }

    @SubscribeEvent
    public static void takeThis(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof MobEntity) {
            MobEntity mob = (MobEntity) e.getEntity();
            mob.goalSelector.addGoal(-1, new NoGoal(mob));
            mob.targetSelector.addGoal(-1, new NoGoal(mob));
            if (e.getEntity() instanceof CreatureEntity) {
                CreatureEntity creature = (CreatureEntity) e.getEntity();
                if (!StealthUtils.stealthMap.getOrDefault(creature.getType().getRegistryName(), StealthUtils.STEALTH).isDeaf())
                    mob.goalSelector.addGoal(0, new InvestigateSoundGoal(creature));
            }
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
        if (!elb.level.isClientSide) {
            if (elb.hasEffect(WarEffects.PETRIFY.get())) {
                elb.xRot = elb.xRotO;
                elb.yRot = elb.yRotO;
                elb.yHeadRot = elb.yHeadRotO;
                elb.setDeltaMovement(0, 0, 0);
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
            e.getEntityLiving().setDeltaMovement(0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void sneak(final LivingEvent.LivingVisibilityEvent e) {
        /*
        out of LoS, reduce by 80%
        light, reduce by 80%
        speed, reduce by 50%
        can't see, reduce by 90%
        each stat scaled by stealth logarithmically. 10 stealth=halved bonus
        max is 0.045 in absolute darkness standing still behind
        min is 0.595 in above conditions but full armor
         */
        if (e.getLookingEntity() != e.getEntityLiving() && e.getLookingEntity() instanceof LivingEntity && StealthConfig.stealthSystem) {
            double mult = 1;
            LivingEntity sneaker = e.getEntityLiving(), watcher = (LivingEntity) e.getLookingEntity();
            StealthUtils.StealthData sd = StealthUtils.stealthMap.getOrDefault(watcher.getType().getRegistryName(), StealthUtils.STEALTH);
            if (StealthUtils.stealthMap.getOrDefault(sneaker.getType().getRegistryName(), StealthUtils.STEALTH).isCheliceric())
                return;
            if (watcher.getKillCredit() != sneaker && watcher.getLastHurtByMob() != sneaker && watcher.getLastHurtMob() != sneaker && (!(watcher instanceof MobEntity) || ((MobEntity) watcher).getTarget() != sneaker)) {
                double stealth = GeneralUtils.getAttributeValueSafe(sneaker, WarAttributes.STEALTH.get());
                double negMult = 1;
                double posMult = 1;
                //each level of negative stealth multiplies effectiveness by 0.95
                while (stealth < 0) {
                    negMult -= 0.05;
                    stealth++;
                }
                //each level of positive stealth multiplies ineffectiveness by 0.93
                while (stealth > 0) {
                    posMult *= 0.933;
                    stealth--;
                }
                //blinded mobs cannot see
                if (watcher.hasEffect(Effects.BLINDNESS) && !sd.isEyeless())
                    mult /= 8;
                //mobs that can't see behind their backs get a hefty debuff
                if (!sd.isAllSeeing() && !GeneralUtils.isFacingEntity(watcher, sneaker, StealthConfig.baseHorizontalDetection, StealthConfig.baseVerticalDetection))
                    mult *= (1 - (0.7 * negMult)) * posMult;
                //slow is smooth, smooth is fast
                if (!sd.isPerceptive()) {
                    final double speedSq = GeneralUtils.getSpeedSq(sneaker);
                    mult *= (1 - (0.5 - MathHelper.sqrt(speedSq) * 2 * posMult) * negMult);
                }
                //stay dark, stay dank
                if (!sd.isNightVision() && !watcher.hasEffect(Effects.NIGHT_VISION) && !sneaker.hasEffect(Effects.GLOWING) && sneaker.getRemainingFireTicks() <= 0) {
                    World world = sneaker.level;
                    if (world.isAreaLoaded(sneaker.blockPosition(), 5) && world.isAreaLoaded(watcher.blockPosition(), 5)) {
                        final int slight = StealthUtils.getActualLightLevel(world, sneaker.blockPosition());
                        final int wlight = CombatData.getCap(watcher).getRetina();
                        float m = (1 + (slight - wlight) / 15f) * (slight + 3) / 15f;//ugly, but welp.
                        float lightMalus = MathHelper.clamp(1 - m, 0f, 0.7f);
                        mult *= (1 - (lightMalus * negMult)) * posMult;
                    }
                }
            }
            //is this LoS?
            if (!sd.isHeatSeeking() && GeneralUtils.viewBlocked(watcher, sneaker, true))
                mult *= (0.4);
            e.modifyVisibility(MathHelper.clamp(mult, 0.001, 1));
        }
    }

    @SubscribeEvent
    public static void pray(LivingSetAttackTargetEvent e) {
        if (e.getTarget() == null) return;
        if (!(e.getEntityLiving() instanceof MobEntity)) return;
        final MobEntity mob = (MobEntity) e.getEntityLiving();
        if (mob.hasEffect(WarEffects.FEAR.get()) || mob.hasEffect(WarEffects.CONFUSION.get()) || mob.hasEffect(WarEffects.SLEEP.get()))
            mob.setTarget(null);
        if (mob.getLastHurtByMob() != e.getTarget() && StealthConfig.stealthSystem && !GeneralUtils.isFacingEntity(mob, e.getTarget(), StealthConfig.baseHorizontalDetection, StealthConfig.baseVerticalDetection)) {
            StealthUtils.StealthData sd = StealthUtils.stealthMap.getOrDefault(mob.getType().getRegistryName(), StealthUtils.STEALTH);
            if (sd.isAllSeeing() || sd.isWary()) return;
            //outside of LoS, perform luck check. Pray to RNGesus!
            double luckDiff = GeneralUtils.getAttributeValueSafe(e.getTarget(), Attributes.LUCK) - GeneralUtils.getAttributeValueSafe(mob, Attributes.LUCK);
            if (luckDiff <= 0 || WarDance.rand.nextFloat() > (luckDiff / (2 + luckDiff))) {
                //you failed!
                if (sd.isSkeptical()) {
                    mob.setTarget(null);
                    mob.lookAt(EntityAnchorArgument.Type.FEET, e.getTarget().position());//.getLookController().setLookPositionWithEntity(e.getTarget(), 0, 0);
                }
            } else {
                //success!
                mob.setTarget(null);
                if (!sd.isLazy())
                    mob.lookAt(EntityAnchorArgument.Type.FEET, e.getTarget().position());//.getLookController().setLookPositionWithEntity(e.getTarget(), 0, 0);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sync(LivingSetAttackTargetEvent e) {
        if (!e.getEntityLiving().level.isClientSide())
            CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(e::getEntityLiving), new UpdateTargetPacket(e.getEntityLiving().getId(), e.getTarget() == null ? -1 : e.getTarget().getId()));
    }

    @SubscribeEvent
    public static void nigerundayo(final PotionEvent.PotionAddedEvent e) {
        if (e.getPotionEffect().getEffect() == Effects.BLINDNESS && GeneralConfig.blindness) {
            if (e.getEntityLiving() instanceof MobEntity)
                ((MobEntity) e.getEntityLiving()).setTarget(null);
            e.getEntityLiving().setLastHurtByMob(null);
        }
    }

    @SubscribeEvent
    public static void lure(TickEvent.ServerTickEvent e) {
        Iterator<Map.Entry<Tuple<World, BlockPos>, Float>> it = alertTracker.entrySet().iterator();
        {
            while (it.hasNext()) {
                Map.Entry<Tuple<World, BlockPos>, Float> n = it.next();
                if (n.getKey().getA().isAreaLoaded(n.getKey().getB(), n.getValue().intValue())) {
                    for (CreatureEntity c : (n.getKey().getA().getLoadedEntitiesOfClass(CreatureEntity.class, new AxisAlignedBB(n.getKey().getB()).inflate(n.getValue())))) {
                        if (StealthUtils.getAwareness(null, c) == StealthUtils.Awareness.UNAWARE && !StealthUtils.stealthMap.getOrDefault(c.getType().getRegistryName(), StealthUtils.STEALTH).isDeaf()) {

                            c.getNavigation().stop();
                            c.getNavigation().moveTo(c.getNavigation().createPath(n.getKey().getB(), (int) (n.getValue() + 3)), 1);
                            BlockPos vec = n.getKey().getB();
                            c.lookAt(EntityAnchorArgument.Type.EYES, new Vector3d(vec.getX(), vec.getY(), vec.getZ()));
                        }
                    }
                }
            }
        }
        alertTracker.clear();
    }

}
