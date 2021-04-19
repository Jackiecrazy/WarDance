package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class EntityHandler {
    public static HashMap<PlayerEntity, Entity> mustUpdate = new HashMap<>();
    public static ConcurrentHashMap<Tuple<World, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void start(FMLServerStartingEvent e) {
        mustUpdate = new HashMap<>();
        alertTracker = new ConcurrentHashMap<>();
    }

    @SubscribeEvent
    public static void stop(FMLServerStoppingEvent e) {
        mustUpdate = new HashMap<>();
        alertTracker = new ConcurrentHashMap<>();
    }

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity) {
            e.addCapability(new ResourceLocation("wardance:combatinfo"), new CombatData((LivingEntity) e.getObject()));
        }
    }

    @SubscribeEvent
    public static void takeThis(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof MobEntity) {
            MobEntity mob = (MobEntity) e.getEntity();
            mob.goalSelector.addGoal(-1, new NoGoal(mob));
            mob.targetSelector.addGoal(-1, new NoGoal(mob));
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.Clone e) {
        if (!e.isWasDeath()) {
            CombatData.getCap(e.getPlayer()).read(CombatData.getCap(e.getOriginal()).write());
            CombatData.getCap(e.getPlayer()).setFatigue(0);
            CombatData.getCap(e.getPlayer()).setBurnout(0);
            CombatData.getCap(e.getPlayer()).setWounding(0);
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
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingUpdateEvent e) {
        LivingEntity elb = e.getEntityLiving();
        if (!elb.world.isRemote && !(elb instanceof PlayerEntity)) {
            if (elb instanceof MobEntity && ((MobEntity) elb).getAttackTarget() != null)
                for (Entity fan : elb.world.getEntitiesWithinAABBExcludingEntity(elb, elb.getBoundingBox().grow(2))) {
                    if (fan instanceof LivingEntity && GeneralUtils.getDistSqCompensated(fan, elb) < 2 && fan != ((MobEntity) elb).getAttackTarget()) {
                        //mobs "avoid" clumping together
                        Vector3d diff = elb.getPositionVec().subtract(fan.getPositionVec());
                        fan.addVelocity(diff.x == 0 ? 0 : -0.03 / diff.x, 0, diff.z == 0 ? 0 : -0.03 / diff.z);
                        elb.addVelocity(diff.x == 0 ? 0 : 0.03 / diff.x, 0, diff.z == 0 ? 0 : 0.03 / diff.z);
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
            if (!GeneralUtils.isFacingEntity(watcher, sneaker, Math.max(CombatConfig.baseHorizontalDetection, sneaker.getTotalArmorValue() * CombatConfig.anglePerArmor), Math.max(CombatConfig.baseVerticalDetection, sneaker.getTotalArmorValue() * sneaker.getTotalArmorValue())))
                e.modifyVisibility(0.2);
            if (!watcher.isPotionActive(Effects.NIGHT_VISION)) {
                World world = sneaker.world;
                if (world.isAreaLoaded(sneaker.getPosition(), 5) && world.isAreaLoaded(watcher.getPosition(), 5))
                    e.modifyVisibility(0.5 + world.getLight(sneaker.getPosition()) * world.getLight(sneaker.getPosition()) * 0.05);
            }
            e.modifyVisibility(sneaker.isSprinting() ? 1.1 : watcher.canEntityBeSeen(sneaker) ? 0.4 : 1);
        }
    }

    @SubscribeEvent
    public static void pray(LivingSetAttackTargetEvent e) {
        if (e.getTarget() != null && e.getEntityLiving() != null && CombatConfig.stealthSystem && !GeneralUtils.isFacingEntity(e.getEntityLiving(), e.getTarget(), Math.max(CombatConfig.baseHorizontalDetection, e.getTarget().getTotalArmorValue() * CombatConfig.anglePerArmor), Math.max(CombatConfig.baseVerticalDetection, e.getTarget().getTotalArmorValue() * e.getTarget().getTotalArmorValue()))) {
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
            while (it.hasNext()) {
                Map.Entry<Tuple<World, BlockPos>, Float> n = it.next();
                if (n.getKey().getA().isAreaLoaded(n.getKey().getB(), n.getValue().intValue())) {
                    for (CreatureEntity c : (n.getKey().getA().getEntitiesWithinAABB(CreatureEntity.class, new AxisAlignedBB(n.getKey().getB()).grow(n.getValue())))) {
                        if (CombatUtils.getAwareness(null, c) == CombatUtils.AWARENESS.UNAWARE)
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
