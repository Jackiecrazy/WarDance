package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class ItchyCurseMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    protected ItchyCurseMixin(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Inject(method = "isShaking", at=@At("HEAD"), cancellable = true)
    private void shake(T mob, CallbackInfoReturnable<Boolean> cir){
        if(Marks.getCap(mob).isMarked(WarSkills.ITCHY_CURSE.get())){
            cir.setReturnValue(true);
        }
    }
}
