package jackiecrazy.wardance.compat;

import com.elenai.elenaidodge2.capability.PlayerInvincibilityProvider;
import com.elenai.feathers.api.FeathersHelper;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.DodgeEvent;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.MovementUtils;
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

    public static void syncIFrames(Player player) {
        player.getCapability(PlayerInvincibilityProvider.PLAYER_INVINCIBILITY).ifPresent((i) -> {
            //only true if elenai procs dodge frames
            ICombatCapability cap = CombatData.getCap(player);
            if (i.getInvincibility() > cap.getDodgeTime() && i.getInvincibility()>0) {
                //start of a new dodge, refill spirit and give appropriate iframes
                cap.setDodgeTime(i.getInvincibility());
                CombatData.getCap(player).addSpirit(1);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void noDodge(DodgeEvent e) {
        if (GeneralConfig.elenai && CombatData.getCap(e.getEntity()).isStunned()) {
            e.setCanceled(true);
        }
        CombatData.getCap(e.getEntity()).addSpirit(1);
    }

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
