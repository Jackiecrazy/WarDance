package jackiecrazy.wardance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import jackiecrazy.wardance.entity.GrappleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;

public class GrappleRenderer extends EntityRenderer<GrappleEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final Vec3 SIDE = new Vec3(1, 0, 0);
    private static final Color[] RAINBOW = {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA
    };
    private BakedModel chain = null;

    public GrappleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    private static float fraction(int p_114691_, int p_114692_) {
        return (float) p_114691_ / (float) p_114692_;
    }

    private static void vertex(VertexConsumer p_254464_,
                               Matrix4f p_254085_,
                               Matrix3f p_253962_,
                               int p_254296_,
                               float p_253632_,
                               int p_254132_,
                               int p_254171_,
                               int p_254026_) {
        p_254464_.vertex(p_254085_, p_253632_ - 0.5F, (float) p_254132_ - 0.5F, 0.0F).color(255, 255, 255, 255).uv((float) p_254171_, (float) p_254026_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_254296_).normal(p_253962_, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private static void stringVertex(float p_174119_,
                                     float p_174120_,
                                     float p_174121_,
                                     VertexConsumer p_174122_,
                                     PoseStack.Pose p_174123_,
                                     float p_174124_,
                                     float p_174125_) {
        float f = p_174119_ * p_174124_;
        float f1 = p_174120_ * (p_174124_ * p_174124_ + p_174124_) * 0.5F + 0.25F;
        float f2 = p_174121_ * p_174124_;
        float f3 = p_174119_ * p_174125_ - f;
        float f4 = p_174120_ * (p_174125_ * p_174125_ + p_174125_) * 0.5F + 0.25F - f1;
        float f5 = p_174121_ * p_174125_ - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        p_174122_.vertex(p_174123_.pose(), f, f1, f2).color(0, 0, 0, 255).normal(p_174123_.normal(), f3, f4, f5).endVertex();
    }

    private static Vec3 cubicBezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float u = 1f - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        Vec3 result = p0.scale(uuu)               // (1−t)^3 * P0
                .add(p1.scale(3 * uu * t))        // 3(1−t)^2 t * P1
                .add(p2.scale(3 * u * tt))        // 3(1−t) t^2 * P2
                .add(p3.scale(ttt));              // t^3 * P3

        return result;
    }

    private static void lineVertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            Vec3 a,
            Vec3 b,
            Color c,
            int light
    ) {

        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();

        consumer.vertex(mat, (float) a.x, (float) a.y, (float) a.z)
                .color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha())
                .normal(norm, 0, 1, 0)
                .uv2(light)
                .endVertex();

        consumer.vertex(mat, (float) b.x, (float) b.y, (float) b.z)
                .color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha())
                .normal(norm, 0, 1, 0)
                .uv2(light)
                .endVertex();
    }

    @Override
    public void render(GrappleEntity hook,
                       float yaw,
                       float partialtick,
                       PoseStack stack,
                       MultiBufferSource buffer,
                       int light) {

        LivingEntity player = hook.getOwner();
        if (player != null) {
            if (chain == null) {
                BlockRenderDispatcher dispatcher =
                        Minecraft.getInstance().getBlockRenderer();

                chain =
                        dispatcher.getBlockModel(Blocks.CHAIN.defaultBlockState());
            }
            stack.pushPose();
            int armShift = player.getMainArm() == HumanoidArm.RIGHT ? -1 : 1;
            float circlePhase = 40;
            float handOffset = Mth.sin(Mth.sqrt(circlePhase) * (float) Math.PI);
            float rotationOffset = Mth.lerp(partialtick, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
            double xcomponent = (double) Mth.sin(rotationOffset);
            double ycomponent = (double) Mth.cos(rotationOffset);
            double realArmShift = (double) armShift * 0.35D;
            double d4;
            double d5;
            double d6;
            float f3;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && player == Minecraft.getInstance().player) {
                double d7 = 960.0D / (double) this.entityRenderDispatcher.options.fov().get().intValue();
                Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) armShift * 0.525F, -0.1F);
                vec3 = vec3.scale(d7);
                vec3 = vec3.yRot(handOffset * 0.5F);
                vec3 = vec3.xRot(-handOffset * 0.7F);
                d4 = Mth.lerp((double) partialtick, player.xo, player.getX()) + vec3.x;
                d5 = Mth.lerp((double) partialtick, player.yo, player.getY()) + vec3.y;
                d6 = Mth.lerp((double) partialtick, player.zo, player.getZ()) + vec3.z;
                f3 = player.getEyeHeight();
            } else {
                d4 = Mth.lerp((double) partialtick, player.xo, player.getX()) - ycomponent * realArmShift - xcomponent * 0.8D;
                d5 = player.yo + (double) player.getEyeHeight() + (player.getY() - player.yo) * (double) partialtick - 0.45D;
                d6 = Mth.lerp((double) partialtick, player.zo, player.getZ()) - xcomponent * realArmShift + ycomponent * 0.8D;
                f3 = player.isCrouching() ? -0.1875F : 0.0F;
            }

            double d9 = Mth.lerp((double) partialtick, hook.xo, hook.getX());
            double d10 = Mth.lerp((double) partialtick, hook.yo, hook.getY()) + 0.25D;
            double d8 = Mth.lerp((double) partialtick, hook.zo, hook.getZ());
            float f4 = (float) (d4 - d9);
            float f5 = (float) (d5 - d10) + f3;
            float f6 = (float) (d6 - d8);
            VertexConsumer vertexconsumer1 = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
            PoseStack.Pose posestack$pose1 = stack.last();

            // Convert to Vec3 for easier math
            Vec3 P0 = new Vec3(f4, f5, f6);      // Player hand → hook offset
            Vec3 P3 = Vec3.ZERO;                 // Hook is rendered at origin in this pose stack

            double sag = hook.renderLag / (hook.distanceToSqr(player));
            //hook.distanceToSqr(player);
            Vec3 perpendicular = hook.position().subtract(player.position()).cross(UP);
            if (perpendicular.lengthSqr() < 0.001) perpendicular = SIDE;
            Vec3 P1 = P0.scale(0.70).add(-sag * perpendicular.x, -sag * perpendicular.y, -sag * perpendicular.z); // Pull down near the player
            Vec3 P2 = P0.scale(0.80).add(sag * perpendicular.x, sag * perpendicular.y, sag * perpendicular.z); // Pull down near the hook

            int segments = 25; // smoother curve
            for (int i = 0; i <= segments; i++) {
                float t1 = i / (float) segments;
                float t2 = (i + 1f) / (float) segments;

                Vec3 pA = cubicBezier(P0, P1, P2, P3, t1);
                Vec3 pB = cubicBezier(P0, P1, P2, P3, t2);

                renderChain(stack, vertexconsumer1, pA, pB, light);
                //lineVertex(vertexconsumer1, posestack$pose1, pA, pB, i % 2 == 0 ? Color.WHITE : Color.DARK_GRAY, light);
            }


            stack.popPose();
            super.render(hook, yaw, partialtick, stack, buffer, light);
        }
    }

    private void renderChain(PoseStack pose, VertexConsumer buffer, Vec3 start, Vec3 pB, int light) {
        float spacing = 0.25F;
        Vec3 dir = pB.subtract(start);
        double length = dir.length();
        dir=dir.normalize();
        Quaternionf rotation = new Quaternionf()
                .rotationTo(
                        0, 1, 0,                       // model points along +Y
                        (float)dir.x,
                        (float)dir.y,
                        (float)dir.z);
        for (float d = 0; d < length; d += spacing) {

            Vec3 pos = start.add(dir.scale(d));

            pose.pushPose();

            pose.translate(pos.x, pos.y, pos.z);

            pose.mulPose(rotation);
            pose.scale(spacing,spacing,spacing);
            pose.translate(-0.25, -0.25, -0.25);

//            if (((int)(d / spacing) & 1) == 1)
//                pose.mulPose(Axis.YP.rotationDegrees(90));

            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    pose.last(),
                    buffer,
                    Blocks.CHAIN.defaultBlockState(),
                    chain,
                    1,1,1,
                    light,
                    OverlayTexture.NO_OVERLAY
            );

            pose.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(GrappleEntity p_114482_) {
        return TEXTURE_LOCATION;
    }
}
