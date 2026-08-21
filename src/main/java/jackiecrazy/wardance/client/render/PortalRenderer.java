package jackiecrazy.wardance.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.footwork.client.render.ItemEntityRenderer;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.wardance.entity.skill.BabylonPortal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class PortalRenderer extends ItemEntityRenderer {
    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlyingItemEntity entity,
                       float yaw,
                       float partialTicks,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight) {
        BabylonPortal bwe=((BabylonPortal) entity);
        final float size = Mth.sin((bwe.size + partialTicks) / 40f);
        poseStack.pushPose();
        poseStack.scale(size,size,size);
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
