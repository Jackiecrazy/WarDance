package jackiecrazy.wardance.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.config.StealthConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingRenderer.class)
public abstract class MixinMobStealth<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M> {

    private int lastcalculation = 0;

    private float cache = 1;

    private T mob;

    protected MixinMobStealth(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", at = @At("HEAD"))
    private void ah(T f3, float f4, float direction, MatrixStack ivertexbuilder, IRenderTypeBuffer i, int layerrenderer, CallbackInfo ci) {
        mob = f3;
    }

    @ModifyConstant(method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", require = 0,
            constant = {
                    @Constant(floatValue = 1.0F, ordinal = 7)
            })
    private float invisible(float constant) {
        if (!StealthConfig.playerStealth || Minecraft.getInstance().player == null) return constant;
        if (mob.tickCount == lastcalculation) return cache;
        lastcalculation = mob.tickCount;
        double visible = CombatData.getCap(Minecraft.getInstance().player).visionRange() * mob.getVisibilityPercent(Minecraft.getInstance().player);
        visible *= visible;
        double distsq = Minecraft.getInstance().player.distanceToSqr(mob);
        float ret;
        if (distsq > visible) ret = 0;
        else ret = (float) ((visible - distsq) / visible);
        cache = ret;
        return ret;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", at = @At(value = "STORE"), ordinal = 0, require = 0)
    private RenderType rt(RenderType former) {
        if (!StealthConfig.playerStealth || cache >= 0.9) return former;
        return RenderType.itemEntityTranslucentCull(getTextureLocation(mob));
    }
}