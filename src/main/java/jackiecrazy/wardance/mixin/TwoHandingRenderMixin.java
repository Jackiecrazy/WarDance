package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.handlers.TwoHandingHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class TwoHandingRenderMixin {
    @Inject(method = "getArmPose", at = @At(value = "RETURN"), cancellable = true)
    private static void combatModeOverride(AbstractClientPlayer p,
                                           InteractionHand h,
                                           CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (StylishData.getCap(p).isCombatMode() && h == InteractionHand.MAIN_HAND && TwoHandingHandler.suppressOffhand(p, p.getMainHandItem())) {
            //two-handing
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
        }
        if(FlyingWeaponData.getCap(p).getHeldBlock()!=null){
            cir.setReturnValue(HumanoidModel.ArmPose.THROW_SPEAR);
        }
    }
}
