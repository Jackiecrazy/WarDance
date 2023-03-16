package jackiecrazy.wardance.compat;

import com.elenai.feathers.api.FeathersHelper;
import jackiecrazy.footwork.event.DodgeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiCompat {
    public static void manipulateFeather(ServerPlayer e, int amount) {
        if (amount > 0)
            FeathersHelper.addFeathers(e, amount);
        else FeathersHelper.spendFeathers(e, -amount);
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
    public static void dodge(DodgeEvent e) {
//        if (GeneralConfig.elenai && !e.isCanceled()) {
//            jackiecrazy.footwork.event.DodgeEvent v = new jackiecrazy.footwork.event.DodgeEvent(e.getEntity(), jackiecrazy.footwork.event.DodgeEvent.Direction.values()[e.getDirection().ordinal()], e.getForce());
//            MinecraftForge.EVENT_BUS.post(v);
//            if (e.isCanceled()) e.setCanceled(true);
//            else {
//                CombatData.getCap(e.getEntity()).consumePosture(0);
//                e.setForce(v.getForce());
//                e.setDirection(DodgeEvent.Direction.values()[v.getDirection().ordinal()]);
//            }
//
//        }
    }
}
