package jackiecrazy.wardance.compat;

import com.elenai.elenaidodge2.api.DodgeEvent;
import com.elenai.elenaidodge2.api.FeathersHelper;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.config.GeneralConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiCompat {
    private static Object handler;

    private static FeathersHelper getFeatherHandler() {
        if (handler == null) handler = new FeathersHelper();
        return (FeathersHelper) handler;
    }

    public static void manipulateFeather(ServerPlayer e, int amount) {
        if (amount > 0)
            getFeatherHandler().increaseFeathers(e, amount);
        else getFeatherHandler().decreaseFeathers(e, -amount);
    }

    public static void manipulateRegenTime(ServerPlayer e, int amount) {
        if (amount > 0)
            getFeatherHandler().increaseRegenModifier(e, amount);
        else getFeatherHandler().decreaseRegenModifier(e, -amount);
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void noDodge(DodgeEvent.ServerDodgeEvent e) {
//        if (CombatConfig.elenai && CombatData.getCap(e.getPlayer()).getStaggerTime() > 0) {
//            e.setCanceled(true);
//            int dir = -1;
//            switch (e.getDirection()) {
//                case FORWARD:
//                    dir = 99;
//                    break;
//                case BACK:
//                    dir = 1;
//                    break;
//                case LEFT:
//                    dir = 0;
//                    break;
//                case RIGHT:
//                    dir = 2;
//                    break;
//            }
//            MovementUtils.attemptDodge(e.getPlayer(), dir);
//        }
//    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dodge(DodgeEvent.ServerDodgeEvent e) {
        if (GeneralConfig.elenai && !e.isCanceled()) {
            jackiecrazy.footwork.event.DodgeEvent v = new jackiecrazy.footwork.event.DodgeEvent(e.getPlayer(), jackiecrazy.footwork.event.DodgeEvent.Direction.values()[e.getDirection().ordinal()], e.getForce());
            MinecraftForge.EVENT_BUS.post(v);
            if (e.isCanceled()) e.setCanceled(true);
            else {
                CombatData.getCap(e.getPlayer()).consumePosture(0);
                e.setForce(v.getForce());
                e.setDirection(DodgeEvent.Direction.values()[v.getDirection().ordinal()]);
            }

        }
    }
}
