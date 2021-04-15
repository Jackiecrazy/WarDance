package jackiecrazy.wardance.compat;

import com.elenai.elenaidodge2.api.DodgeEvent;
import com.elenai.elenaidodge2.api.FeathersHelper;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiCompat {
    private static Object handler;

    private static FeathersHelper getFeatherHandler() {
        if (handler == null) handler = new FeathersHelper();
        return (FeathersHelper) handler;
    }

    public static void manipulateFeather(ServerPlayerEntity e, int amount) {
        if (amount > 0)
            getFeatherHandler().increaseFeathers(e, amount);
        else getFeatherHandler().decreaseFeathers(e, -amount);
    }

    public static void manipulateRegenTime(ServerPlayerEntity e, int amount) {
        if (amount > 0)
            getFeatherHandler().increaseRegenModifier(e, amount);
        else getFeatherHandler().decreaseRegenModifier(e, -amount);
    }

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
            CombatData.getCap(e.getPlayer()).consumePosture(0);
        }
    }
}
