package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.MonsterEntity;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
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
    public static HashMap<PlayerEntity, Entity> mustUpdate = new HashMap<>();
    public static HashMap<PlayerEntity, Integer> lastSneak = new HashMap<>();
    public static ConcurrentHashMap<Tuple<World, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void start(FMLServerStartingEvent e) {
        mustUpdate = new HashMap<>();
        lastSneak = new HashMap<>();
        alertTracker = new ConcurrentHashMap<>();
    }

    @SubscribeEvent
    public static void stop(FMLServerStoppingEvent e) {
        mustUpdate.clear();
        lastSneak.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity) {
            e.addCapability(new ResourceLocation("wardance:combatinfo"), new CombatData((LivingEntity) e.getObject()));
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
        } else {
            ISkillCapability isc = CasterData.getCap(e.getPlayer());
            isc.clearActiveSkills();
            isc.clearSkillCooldowns();
        }
    }

    @SubscribeEvent
    public static void sleep(PlayerWakeUpEvent e) {
        CombatData.getCap(e.getPlayer()).setFatigue(0);
        CombatData.getCap(e.getPlayer()).setBurnout(0);
        CombatData.getCap(e.getPlayer()).setWounding(0);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.side == LogicalSide.SERVER && e.player.isAlive()) {
            CombatData.getCap(e.player).update();
            CasterData.getCap(e.player).update();
            if (e.player.isSneaking()) {
                if (!lastSneak.containsValue(e.player))
                    lastSneak.put(e.player, e.player.ticksExisted);
            } else lastSneak.remove(e.player);
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingUpdateEvent e) {
        LivingEntity elb = e.getEntityLiving();
        if (!elb.world.isRemote && !(elb instanceof PlayerEntity)) {
            if (elb instanceof MobEntity && CombatData.getCap(elb).getStaggerTime() == 0 && ((MobEntity) elb).getAttackTarget() != null) {
                double safeSpace = (elb.getWidth()) * 2;
                for (Entity fan : elb.world.getEntitiesWithinAABBExcludingEntity(elb, elb.getBoundingBox().grow(safeSpace))) {
                    if (fan instanceof MonsterEntity && ((MobEntity) fan).getAttackTarget() == ((MobEntity) fan).getAttackTarget() &&
                            GeneralUtils.getDistSqCompensated(fan, elb) < (safeSpace + 1) * safeSpace && fan != ((MobEntity) elb).getAttackTarget()) {
                        //mobs "avoid" clumping together
                        Vector3d diff = elb.getPositionVec().subtract(fan.getPositionVec());
                        double targDistSq = elb.getDistanceSq(((MobEntity) elb).getAttackTarget());
                        //targDistSq = Math.max(targDistSq, 1);
                        //fan.addVelocity(diff.x == 0 ? 0 : -0.03 / diff.x, 0, diff.z == 0 ? 0 : -0.03 / diff.z);
                        elb.addVelocity(diff.x == 0 ? 0 : MathHelper.clamp(0.5 / (diff.x * targDistSq), -1, 1), 0, diff.z == 0 ? 0 : MathHelper.clamp(0.5 / (diff.z * targDistSq), -1, 1));
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
                    }
                }
            }
            //staggered mobs bypass update interval
            ICombatCapability cap = CombatData.getCap(elb);
            if (cap.getStaggerTime() > 0 || mustUpdate.containsValue(e.getEntity()) || elb.ticksExisted % CombatConfig.mobUpdateInterval == 0)
                cap.update();
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
        if (e.getLookingEntity() instanceof LivingEntity && CombatConfig.stealthSystem) {
            LivingEntity sneaker = e.getEntityLiving(), watcher = (LivingEntity) e.getLookingEntity();
            CombatUtils.StealthData sd = CombatUtils.stealthMap.getOrDefault(watcher.getType().getRegistryName(), CombatUtils.STEALTH);
            if (sd.isVigilant()) return;
            if (!sd.isAllSeeing() && !GeneralUtils.isFacingEntity(watcher, sneaker, Math.max(CombatConfig.baseHorizontalDetection, sneaker.getTotalArmorValue() * CombatConfig.anglePerArmor), Math.max(CombatConfig.baseVerticalDetection, sneaker.getTotalArmorValue() * sneaker.getTotalArmorValue())))
                e.modifyVisibility(0.2);
            if (!sd.isNightVision() && !watcher.isPotionActive(Effects.NIGHT_VISION)) {
                World world = sneaker.world;
                if (world.isAreaLoaded(sneaker.getPosition(), 5) && world.isAreaLoaded(watcher.getPosition(), 5))
                    e.modifyVisibility(0.5 + world.getLight(sneaker.getPosition()) * world.getLightValue(sneaker.getPosition()) * 0.05);
            }
            if (!sd.isAllSeeing())
                e.modifyVisibility(sneaker.isSprinting() ? 1.1 : watcher.canEntityBeSeen(sneaker) ? 0.4 : 1);
        }
    }

    @SubscribeEvent
    public static void pray(LivingSetAttackTargetEvent e) {
        if (e.getTarget() != null && e.getEntityLiving() != null && CombatConfig.stealthSystem && !GeneralUtils.isFacingEntity(e.getEntityLiving(), e.getTarget(), Math.max(CombatConfig.baseHorizontalDetection, e.getTarget().getTotalArmorValue() * CombatConfig.anglePerArmor), Math.max(CombatConfig.baseVerticalDetection, e.getTarget().getTotalArmorValue() * e.getTarget().getTotalArmorValue()))) {
            CombatUtils.StealthData sd = CombatUtils.stealthMap.getOrDefault(e.getEntityLiving().getType().getRegistryName(), CombatUtils.STEALTH);
            if (sd.isVigilant() || sd.isObservant()) return;
            //outside of LoS, perform luck check. Pray to RNGesus!
            double luckDiff = GeneralUtils.getAttributeValueSafe(e.getTarget(), Attributes.LUCK) - GeneralUtils.getAttributeValueSafe(e.getEntityLiving(), Attributes.LUCK);
            if (luckDiff > 0 && WarDance.rand.nextFloat() < (luckDiff / (2 + luckDiff))) {
                ((MobEntity) e.getEntityLiving()).setAttackTarget(null);
            }
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
                    for (CreatureEntity c : (n.getKey().getA().getEntitiesWithinAABB(CreatureEntity.class, new AxisAlignedBB(n.getKey().getB()).grow(n.getValue())))) {
                        if (CombatUtils.getAwareness(null, c) == CombatUtils.AWARENESS.UNAWARE && !CombatUtils.stealthMap.getOrDefault(c.getType().getRegistryName(), CombatUtils.STEALTH).isDeaf())
                            c.getNavigator().setPath(c.getNavigator().getPathToPos(n.getKey().getB(), (int) (n.getValue() + 3)), 1);
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
            return CombatData.getCap(e).isValid() && CombatData.getCap(e).getStaggerTime() > 0;
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
