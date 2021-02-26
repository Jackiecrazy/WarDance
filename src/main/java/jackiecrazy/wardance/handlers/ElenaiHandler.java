package jackiecrazy.wardance.handlers;

import com.elenai.elenaidodge2.api.DodgeEvent;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void noDodge(DodgeEvent.ServerDodgeEvent e) {
        if (CombatConfig.elenai && CombatData.getCap(e.getPlayer()).getStaggerTime() > 0) {
            e.setCanceled(true);
            int dir = -1;
            switch (e.getDirection()) {
                case FORWARD:
                    dir = 99;
                    break;
                case BACK:
                    dir = 1;
                    break;
                case LEFT:
                    dir = 0;
                    break;
                case RIGHT:
                    dir = 2;
                    break;
            }
            MovementUtils.attemptDodge(e.getPlayer(), dir, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dodge(DodgeEvent.ServerDodgeEvent e) {
        if (CombatConfig.elenai && !e.isCanceled()) {
            CombatData.getCap(e.getPlayer()).setPostureGrace(CombatConfig.postureCD);
        }
    }
}
