package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FirstPersonRenderer.class)
public abstract class MixinEquipProgress {

    @Shadow private float offHandHeight;

    @Redirect(method = "tick", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F",
                    ordinal = 3))
    private float modifyEquipProgress(float num, float min, float max) {
        if (!GeneralConfig.dual || Minecraft.getInstance().player == null) return MathHelper.clamp(num, min, max);
        boolean requip = num + offHandHeight == 0 || CombatData.getCap(Minecraft.getInstance().player).getHandBind(Hand.OFF_HAND) > 0;
        float f = CombatUtils.getCooledAttackStrength(Minecraft.getInstance().player, Hand.OFF_HAND, 1f);
        return MathHelper.clamp((!requip ? f * f * f : 0.0F) - offHandHeight, min, max);
        //return MathHelper.clamp((float)(!requip ? 1 : 0) - this.equippedProgressOffHand, min, max);
        //return 0;
    }

}