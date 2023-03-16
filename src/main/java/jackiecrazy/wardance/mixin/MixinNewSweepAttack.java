package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public class MixinNewSweepAttack {

    @Shadow
    @Final
    ServerGamePacketListenerImpl this$0;

    @Shadow
    @Final
    Entity val$entity;

    @Inject(method = "onAttack", locals = LocalCapture.CAPTURE_FAILSOFT,
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/server/level/ServerPlayer;attack(Lnet/minecraft/world/entity/Entity;)V"))
    private void sweep(CallbackInfo ci, ItemStack itemstack) {
        ServerPlayer player = this.this$0.player;
        Entity entity = this.val$entity;
        if (CombatUtils.getCooledAttackStrength(player, InteractionHand.MAIN_HAND, 1f) >= 0.9f) {
            double move = player.walkDist - player.walkDistO;
            int temp = player.attackStrengthTicker;
            if (!(player.fallDistance > 0.0F && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger()) && !player.isSprinting() && player.isOnGround() && move < (double) player.getSpeed())
                CombatUtils.sweep(player, entity, InteractionHand.MAIN_HAND, GeneralUtils.getAttributeValueSafe(player, ForgeMod.REACH_DISTANCE.get()));
            player.attackStrengthTicker = temp;
        }
    }

}
