package jackiecrazy.wardance.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jackiecrazy.wardance.entity.skill.JackpotCoin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CoinEntityRenderer extends EntityRenderer<JackpotCoin> {
    private static final ItemStack STACK = new ItemStack(Items.GOLD_NUGGET);
    private final ItemRenderer itemRenderer;

    public CoinEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(JackpotCoin entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Float it a bit in the air
        poseStack.translate(0.0, 0.3, 0.0);

        // Smooth rotation around the X-axis
        float age = (entity.tickCount) + partialTicks;
        float xsalt= (float) (entity.getX()%entity.getId());
        float ysalt= (float) (entity.getX()%entity.getId());
        float zsalt= (float) ((entity.getX()*entity.getZ()));
        poseStack.mulPose(Axis.YP.rotationDegrees(zsalt)); // 12f = rotation speed
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (Math.log(age) * 2000))); // 12f = rotation speed

        // Optional: make it a bit bigger
        poseStack.translate(0,-0.15,0);
        poseStack.scale(1.25f, 1.25f, 1.25f);

        itemRenderer.renderStatic(
                STACK,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(JackpotCoin entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}