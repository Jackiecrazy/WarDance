package jackiecrazy.wardance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.DummyItem;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.renderable.BakedModelRenderable;

public class WarCustomItemRender extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation BASE_SCROLL = new ResourceLocation(WarDance.MODID, "item/scroll");
    public static final WarCustomItemRender INSTANCE = new WarCustomItemRender(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    private static BakedModelRenderable base = null;

    public WarCustomItemRender(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType type, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay) {
        if (!stack.isEmpty() && stack.is(WarItems.DUMMY.get())) {
            pose.pushPose();
            BakedModel models = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(WarDance.MODID, "skill/" + DummyItem.getSkill(stack).getRegistryName().getPath()));//models = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(pose, models, type, true);
            //pose.translate(-0.5D, -0.5D, -0.5D);
            //TODO this is really really dumb
            for (var model : models.getRenderPasses(stack, true)) {
                for (var rendertype : model.getRenderTypes(stack, true)) {
                    VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(bufferSource, rendertype, true, stack.hasFoil());
                    Minecraft.getInstance().getItemRenderer().renderModelLists(model, stack, light, overlay, pose, vertexconsumer);
                }
            }
//            matrixStack.translate(0, 0, zOffset + 200);
//            MultiBufferSource vc = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//            String id = String.valueOf(6);
//            renderer.draw(id, (float) (x + 19 - 2 - renderer.getWidth(id)), (float) (y + 6 + 3), 16777215, true, matrixStack.peek().getPositionMatrix(), vc, false, 0, 15728880);
//            vc.draw();

            pose.popPose();
        }
    }

    private void drawSkill(BufferBuilder builder, int x, int y, Skill s) {
        RenderSystem.setShaderTexture(0, s.icon());
        fillRect(builder, x, y, 256, 256, s.getColor().getRGB());
    }

    private void fillRect(BufferBuilder builder, int x, int y, int width, int height, int color) {
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255, a = 255;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex((double) (x + 0), (double) (y + 0), 0.0D).color(r, g, b, a).endVertex();
        builder.vertex((double) (x + 0), (double) (y + height), 0.0D).color(r, g, b, a).endVertex();
        builder.vertex((double) (x + width), (double) (y + height), 0.0D).color(r, g, b, a).endVertex();
        builder.vertex((double) (x + width), (double) (y + 0), 0.0D).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }
}
