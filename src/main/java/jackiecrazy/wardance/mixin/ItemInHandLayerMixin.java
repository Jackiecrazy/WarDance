package jackiecrazy.wardance.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.client.RenderEvents;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(
        method = "renderArmWithItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hideSwingingWeapon(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext context,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            CallbackInfo ci
    ) {

        if (RenderEvents.handBusy(entity, entity.getMainArm()==arm? InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND)) {
            ci.cancel();
        }
    }
}