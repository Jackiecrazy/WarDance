package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ServerPlayNetHandler.class)
public class MixinNewSweepAttack {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "handleInteract", locals = LocalCapture.CAPTURE_FAILSOFT,
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/entity/player/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"))
    private void sweep(CUseEntityPacket packetIn, CallbackInfo ci, ServerWorld serverworld, Entity entity, double d0, Hand hand, ItemStack itemstack, Optional optional) {
        if (player != null && CombatUtils.getCooledAttackStrength(player, Hand.MAIN_HAND, 1f) >= 0.9f) {
            double move = player.walkDist - player.walkDistO;
            int temp = player.attackStrengthTicker;
            if (!(player.fallDistance > 0.0F && !player.onClimbable() && !player.isInWater() && !player.hasEffect(Effects.BLINDNESS) && !player.isPassenger()) && !player.isSprinting() && player.isOnGround() && move < (double) player.getSpeed())
                CombatUtils.sweep(player, entity, Hand.MAIN_HAND, GeneralUtils.getAttributeValueSafe(player, ForgeMod.REACH_DISTANCE.get()));
            player.attackStrengthTicker = temp;
        }
    }

}
