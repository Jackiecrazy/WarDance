package jackiecrazy.wardance.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.wardance.WarDance;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

//@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class RenderUtils {
    public static final ResourceLocation cooldown = new ResourceLocation(WarDance.MODID, "textures/skill/blank.png");
    public static DecimalFormat formatter = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));


    /**
     * @author Vazkii
     */
    public static Entity getEntityLookedAt(Entity e, double finalDistance) {
        Entity foundEntity = null;
        double distance = finalDistance;
        HitResult pos = raycast(e, finalDistance);
        Vec3 positionVector = e.position();

        if (e instanceof Player) positionVector = positionVector.add(0, e.getEyeHeight(e.getPose()), 0);

        if (pos != null) distance = pos.getLocation().distanceTo(positionVector);

        Vec3 lookVector = e.getLookAngle();
        Vec3 reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getCommandSenderWorld().getEntities(e, e.getBoundingBox().inflate(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expandTowards(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox) {
            if (entity.isPickable()) {
                AABB collisionBox = entity.getBoundingBoxForCulling();
                Optional<Vec3> interceptPosition = collisionBox.clip(positionVector, reachVector);

                if (collisionBox.contains(positionVector)) {
                    if (0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                } else if (interceptPosition.isPresent()) {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.get());

                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }

            if (lookedEntity != null && (minDistance < distance || pos == null)) foundEntity = lookedEntity;
        }

        return foundEntity;
    }

    public static HitResult raycast(Entity e, double len) {
        Vec3 vec = new Vec3(e.getX(), e.getY(), e.getZ());
        if (e instanceof Player) vec = vec.add(new Vec3(0, e.getEyeHeight(e.getPose()), 0));

        Vec3 look = e.getLookAngle();
        if (look == null) return null;

        return raycast(vec, look, e, len);
    }

    public static HitResult raycast(Vec3 origin, Vec3 ray, Entity e, double len) {
        Vec3 next = origin.add(ray.normalize().scale(len));
        return e.level.clip(new ClipContext(origin, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
    }

    public static void drawCooldownCircle(PoseStack ms, int x, int y, int size, float v) {
        if (v == 1) v = 0.95f;
        ms.pushPose();
        //RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
        RenderSystem.setShaderTexture(0, cooldown);
        if (v <= 0) return; // nothing to be drawn
        int x2 = x + size, y2 = y + size; // bottom-right corner
        if (v >= 1) {
            RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 0.1F);
            GuiComponent.blit(ms, x, y, 0, 0, size, size, size, size);
            ms.popPose();
            // entirely filled
            return;
        }
        int xm = (x + x2) / 2, ym = (y + y2) / 2; // middle point
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);
        drawVertex(bufferbuilder, xm, ym);
        drawVertex(bufferbuilder, xm, y);
// draw corners:
        if (v >= 0.125) drawVertex(bufferbuilder, x, y);
        if (v >= 0.375) drawVertex(bufferbuilder, x, y2);
        if (v >= 0.625) drawVertex(bufferbuilder, x2, y2);
        //if (v >= 0.875) drawVertex(bufferbuilder, x2, y);
// calculate angle & vector from value:
        double vd = Math.PI * (v * 2 - 0.5);
        double vx = -Math.cos(vd);
        double vy = Math.sin(vd);
// normalize the vector, so it hits -1+1 at either side:
        double vl = Math.max(Math.abs(vx), Math.abs(vy));
        if (vl < 1) {
            vx /= vl;
            vy /= vl;
        }
        drawVertex(bufferbuilder, (int) (xm + vx * (x2 - x) / 2), (int) (ym + vy * (y2 - y) / 2));
        Tesselator.getInstance().end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1F);
        ms.popPose();
    }

    private static void drawVertex(BufferBuilder bufferbuilder, int x, int y) {
        bufferbuilder.vertex(x, y, 0.0D).uv(0, 0).color(32, 32, 32, 205).endVertex();
    }

    public static Pair<Integer, Integer> translateCoords(DisplayConfigUtils.DisplayData dd, int width, int height) {
        return translateCoords(dd.anchorPoint, dd.numberX, dd.numberY, width, height);
    }

    private static Pair<Integer, Integer> translateCoords(DisplayConfigUtils.AnchorPoint ap, int x, int y, int width, int height) {
        int retx, rety;
        switch (ap) {
            case TOPLEFT:
                retx = 0;
                rety = 0;
                break;
            case TOPRIGHT:
                retx = 0;
                rety = width;
                break;
            case CROSSHAIR:
                retx = width / 2;
                rety = height / 2;
                break;
            case TOPCENTER:
                retx = width / 2;
                rety = 0;
                break;
            case BOTTOMLEFT:
                retx = 0;
                rety = height;
                break;
            case MIDDLELEFT:
                retx = 0;
                rety = height / 2;
                break;
            case BOTTOMRIGHT:
                retx = width;
                rety = height;
                break;
            case MIDDLERIGHT:
                retx = width;
                rety = height / 2;
                break;
            case BOTTOMCENTER:
                retx = width / 2;
                rety = height;
                break;
            default:
                retx = rety = 0;
        }
        retx = Mth.clamp(retx + x, 0, width);
        rety = Mth.clamp(rety + y, 0, height);
        return Pair.of(retx, rety);
    }
}
