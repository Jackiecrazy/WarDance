package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinEquipProgress {

    @Shadow private float offHandHeight;

    @Redirect(method = "tick", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F",
                    ordinal = 3))
    private float modifyEquipProgress(float num, float min, float max) {
        if (!GeneralConfig.dual || Minecraft.getInstance().player == null) return Mth.clamp(num, min, max);
        boolean requip = num + offHandHeight == 0 || CombatData.getCap(Minecraft.getInstance().player).getHandBind(InteractionHand.OFF_HAND) > 0;
        float f = CombatUtils.getCooledAttackStrength(Minecraft.getInstance().player, InteractionHand.OFF_HAND, 1f);
        return Mth.clamp((!requip ? f * f * f : 0.0F) - offHandHeight, min, max);
        //return MathHelper.clamp((float)(!requip ? 1 : 0) - this.equippedProgressOffHand, min, max);
        //return 0;
    }

}