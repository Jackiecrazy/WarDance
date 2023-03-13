package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinSweepDurabilitySuppressor {
    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private void halt(int amount, LivingEntity entityIn, Consumer<LivingEntity> onBroken, CallbackInfo ci) {
        if (!GeneralConfig.sweepDurability && CombatUtils.isSweeping) ci.cancel();
    }
}
