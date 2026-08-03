package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.action.ActionData;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.event.KickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class WarCallbacks {
    @SubscribeEvent
    public static void callback(KickEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ArgumentContext addtlctx = new ArgumentContext(null, e.getEntity()).addContext("target", e.getEntity()).addContext("kicker", e.getEntity()).addContext("damage", e.getDamage()).addContext("posture_damage", e.getPostureDamage()).addContext("source", e.getDamageSource());
        if (e.getTarget() != null)
            ActionData.getCap(e.getTarget()).triggerCallback("kicked", addtlctx);
        if (e.getEntity() != null) {
            ActionData.getCap(e.getEntity()).triggerCallback("kick_other", addtlctx);
        }
        cancelEvent(e);
    }

    private static void cancelEvent(EntityEvent e) {
        if (e.isCancelable() && e.getEntity().getPersistentData().getInt("cancel_event") == 1) e.setCanceled(true);
        e.getEntity().getPersistentData().remove("cancel_event");
    }
}
