package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.CombatData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class EntityHandler {
    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity) {
            e.addCapability(new ResourceLocation("wardance:combatinfo"), new CombatData((LivingEntity) e.getObject()));
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
    public static void sleep(PlayerWakeUpEvent e){
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
        if (!e.getEntityLiving().world.isRemote && !(e.getEntityLiving() instanceof PlayerEntity) && e.getEntityLiving().ticksExisted % 20 == 0) {
            CombatData.getCap(e.getEntityLiving()).update();
        }
    }

    @SubscribeEvent
    public static void noJump(LivingEvent.LivingJumpEvent e) {
        if (!(e.getEntityLiving() instanceof PlayerEntity) && CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0) {
            e.setCanceled(true);
        }
    }
}
