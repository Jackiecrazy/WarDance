package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {"net/minecraft/server/network/ServerGamePacketListenerImpl$1"})
public class MixinNewSweepAttack {

    @Redirect(method = "onAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;attack(Lnet/minecraft/world/entity/Entity;)V"))
    private void sweep(ServerPlayer player, Entity entity) {
        if (CombatUtils.getCooledAttackStrength(player, InteractionHand.MAIN_HAND, 1f) >= 0.9f) {
            int temp = player.attackStrengthTicker;
            if (!player.hasEffect(MobEffects.BLINDNESS))
                CombatUtils.sweep(player, entity, InteractionHand.MAIN_HAND, player.getAttributeValue(ForgeMod.ATTACK_RANGE.get()));
            player.attackStrengthTicker = temp;
        }
        player.attack(entity);
    }

}
