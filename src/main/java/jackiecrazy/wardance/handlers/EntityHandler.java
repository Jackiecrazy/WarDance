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
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.side == LogicalSide.SERVER && e.player.isAlive()) {
            CombatData.getCap(e.player).update();
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingUpdateEvent e) {
        if (!(e.getEntityLiving() instanceof PlayerEntity) && e.getEntityLiving().ticksExisted % 20 == 0) {
            CombatData.getCap(e.getEntityLiving()).update();
        }
    }
}
