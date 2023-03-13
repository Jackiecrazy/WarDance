package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinNewSweepAttack {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleInteract", locals = LocalCapture.CAPTURE_FAILSOFT,
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/entity/player/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"))
    private void sweep(ServerboundInteractPacket packetIn, CallbackInfo ci, ServerLevel serverworld, Entity entity, double d0, InteractionHand hand, ItemStack itemstack, Optional optional) {
        if (player != null && CombatUtils.getCooledAttackStrength(player, InteractionHand.MAIN_HAND, 1f) >= 0.9f) {
            double move = player.walkDist - player.walkDistO;
            int temp = player.attackStrengthTicker;
            if (!(player.fallDistance > 0.0F && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger()) && !player.isSprinting() && player.isOnGround() && move < (double) player.getSpeed())
                CombatUtils.sweep(player, entity, InteractionHand.MAIN_HAND, GeneralUtils.getAttributeValueSafe(player, ForgeMod.REACH_DISTANCE.get()));
            player.attackStrengthTicker = temp;
        }
    }

}
