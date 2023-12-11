package jackiecrazy.wardance.compat;

import com.elenai.elenaidodge2.capability.PlayerInvincibilityProvider;
import com.elenai.feathers.api.FeathersHelper;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.DodgeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiCompat {
    public static void manipulateFeather(ServerPlayer e, int amount) {
        if (amount > 0)
            FeathersHelper.addFeathers(e, amount);
        else FeathersHelper.spendFeathers(e, -amount);
    }

    public static void syncIFrames(Player player){
        player.getCapability(PlayerInvincibilityProvider.PLAYER_INVINCIBILITY).ifPresent((i) -> CombatData.getCap(player).setRollTime(Math.min(-i.getInvincibility(), CombatData.getCap(player).getRollTime())));
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void noDodge(DodgeEvent.ServerDodgeEvent e) {
//        if (CombatConfig.elenai && CombatData.getCap(e.getPlayer()).getStunTime() > 0) {
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

//    @SubscribeEvent
//    public static void onPlayerHit(LivingAttackEvent event) {
//        LivingEntity var2 = event.getEntity();
//        if (var2 instanceof ServerPlayer player) {
//            player.getCapability(PlayerInvincibilityProvider.PLAYER_INVINCIBILITY).ifPresent((i) -> {
//                if (i.getInvincibility() > 0 && (event.getSource() == event.getEntity().level().damageSources().dragonBreath() || event.getSource().getEntity() instanceof Mob || event.getSource().getEntity() instanceof ServerPlayer)) {
//                    event.setCanceled(true);
//                }
//
//            });
//        }
//
//    }
}
