package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.RequestUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

import java.util.EnumSet;


@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class EntityHandler {
    private static class NoGoal extends Goal {
        LivingEntity e;
        static final EnumSet<Flag> mutex = EnumSet.allOf(Flag.class);

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

//    @SubscribeEvent
//    public static void attribute(EntityAttributeCreationEvent e) {
//        //todo hmm
//    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingUpdateEvent e) {
        if (!e.getEntityLiving().world.isRemote && !(e.getEntityLiving() instanceof PlayerEntity)) {
            //staggered mobs bypass update interval
            ICombatCapability cap = CombatData.getCap(e.getEntityLiving());
            if (cap.getStaggerTime() > 0 || e.getEntityLiving().ticksExisted % CombatConfig.mobUpdateInterval == 0)
                cap.update();
        }
    }

    @SubscribeEvent
    public static void noJump(LivingEvent.LivingJumpEvent e) {
        if (!(e.getEntityLiving() instanceof PlayerEntity) && CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0) {
            e.getEntityLiving().setMotion(0, 0, 0);
        }
    }
}
