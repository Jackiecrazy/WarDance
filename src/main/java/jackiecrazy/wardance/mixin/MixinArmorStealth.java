package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.WarAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class MixinArmorStealth {

    @Redirect(method = "getVisibilityPercent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getArmorCoverPercentage()F"))
    private float change(LivingEntity e) {
        final double stealth = e.getAttributeValue(WarAttributes.STEALTH.get());
        if (stealth >= 0) return 0;
        return (float) MathHelper.clamp(stealth / -10, 0, 1);
    }
}