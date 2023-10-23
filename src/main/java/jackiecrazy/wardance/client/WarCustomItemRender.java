package jackiecrazy.wardance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.DummyItem;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.renderable.BakedModelRenderable;

import java.awt.*;

public class WarCustomItemRender extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation BASE_SCROLL = new ResourceLocation(WarDance.MODID, "item/scroll");
    public static final WarCustomItemRender INSTANCE = new WarCustomItemRender(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    private static BakedModelRenderable base = null;

    public WarCustomItemRender(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext p_270899_, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay) {
        if (!stack.isEmpty() && stack.is(WarItems.DUMMY.get())) {
            pose.pushPose();
            ResourceLocation loc;
            TextureAtlasSprite tas;
            Color col;
            if(stack.getOrCreateTag().contains("style")){
                loc=new ResourceLocation(WarDance.MODID, "skill/categories/"+stack.getOrCreateTag().getString("style"));
                col = SkillCategory.fromString(stack.getOrCreateTag().getString("style")).getColor();
            }
            else {
                loc = new ResourceLocation(DummyItem.getSkill(stack).icon().toString().replace("textures/", "").replace(".png", ""));
                col = DummyItem.getSkill(stack).getColor();
            }
            tas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(loc);
            VertexConsumer consumer = bufferSource.getBuffer(Sheets.translucentItemSheet());
            final float u0 = tas.getU0();
            final float u1 = tas.getU1();
            final float v1 = tas.getV0();
            final float v0 = tas.getV1();
            consumer.vertex(pose.last().pose(), 0, 1, 0).color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pose.last().normal(), 0, 0, 1).endVertex();
            consumer.vertex(pose.last().pose(), 0, 0, 0).color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pose.last().normal(), 0, 0, 1).endVertex();
            consumer.vertex(pose.last().pose(), 1, 0, 0).color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pose.last().normal(), 0, 0, 1).endVertex();
            consumer.vertex(pose.last().pose(), 1, 1, 0).color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pose.last().normal(), 0, 0, 1).endVertex();
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
