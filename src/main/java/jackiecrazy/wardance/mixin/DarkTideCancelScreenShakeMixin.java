package jackiecrazy.wardance.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.common.reflection.qual.Invoke;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GameRenderer.class)
public class DarkTideCancelScreenShakeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(method = "bobHurt", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F")), at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"))
    public void steady(PoseStack instance, Quaternionf f) {
        if(this.minecraft.getCameraEntity() instanceof LivingEntity le){
            if(le.getHurtDir()==-99999d){
                return;
            }
        }
        instance.mulPose(f);
    }
}
