package jackiecrazy.wardance.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.client.GuiComponent;
import jackiecrazy.footwork.client.screen.dashboard.DashboardScreen;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.client.screen.scroll.ScrollScreen;
import jackiecrazy.wardance.client.screen.skill.SkillSelectionScreen;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.handlers.TwoHandingHandler;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class RenderEvents {
    private static final ResourceLocation timeslow = new ResourceLocation(WarDance.MODID, "textures/hud/stevetime.png");
    private static final ResourceLocation timeslow1 = new ResourceLocation(WarDance.MODID, "textures/hud/stevetimefill.png");
    private static final ResourceLocation crosshair = new ResourceLocation(WarDance.MODID, "textures/hud/throw_target.png");
    private static HashMap<String, Boolean> rotate;

    public static void updateList(List<? extends String> pos) {
        rotate = new HashMap<>();
        for (String s : pos) {
            try {
                String[] val = s.split(",");
                rotate.put(val[0], Boolean.parseBoolean(val[1]));
            } catch (Exception e) {
                if (GeneralConfig.debug)
                    WarDance.LOGGER.warn("improperly formatted custom rotation definition " + s + "!");
            }
        }
    }

    @SubscribeEvent
    public static void skillReading(RenderTooltipEvent.Color e) {
        if (e.getItemStack().isEmpty() && (Minecraft.getInstance().screen instanceof DashboardScreen || Minecraft.getInstance().screen instanceof SkillSelectionScreen || Minecraft.getInstance().screen instanceof ScrollScreen)) {
            e.setBorderEnd(0xffffffff);
            e.setBorderStart(0xffffffff);
        }
    }

    @SubscribeEvent
    public static void down(RenderLivingEvent.Pre event) {
        final LivingEntity e = event.getEntity();
        float width = e.getBbWidth();
        float height = e.getBbHeight();

        if (e.isAlive()) {
            if (CombatData.getCap(event.getEntity()).isKnockdown()) {
                PoseStack ms = event.getPoseStack();
                //ms.push();
                //tall bois become flat bois
                boolean reg = (ForgeRegistries.ENTITY_TYPES.getKey(e.getType()) != null && rotate.containsKey(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString()));
                boolean rot = reg ? rotate.getOrDefault(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString(), false) : width < height;
                if (rot) {
                    ms.mulPose(Axis.XN.rotationDegrees(90));
                    ms.mulPose(Axis.ZP.rotationDegrees(-e.yBodyRot));
                    ms.mulPose(Axis.YP.rotationDegrees(e.yBodyRot));
                    ms.translate(0, -e.getBbHeight() / 2, 0);
                }
                //cube bois become side bois
                //flat bois become flatter bois
                //multi bois do nothing
            }
            if (CombatData.getCap(e).isDodging() && e.getPose() == Pose.SLEEPING) {
                PoseStack ms = event.getPoseStack();
                ms.mulPose(Axis.YN.rotationDegrees(e.getYRot() - e.getBedOrientation().toYRot()));
//                ms.rotate(Vector3f.ZP.rotationDegrees(-e.renderYawOffset));
//                ms.rotate(Vector3f.YP.rotationDegrees(e.renderYawOffset));
            }
//            if(e.isPotionActive(FootworkEffects.PETRIFY.get())){
//                event.getRenderer()
//                Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
//            }
        }
    }

    @SubscribeEvent
    public static void eyes(RenderLevelStageEvent event) {
        //render things that show up on a mob
        Minecraft mc = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();
        Entity cameraEntity = camera.getEntity() != null ? camera.getEntity() : mc.player;
        Entity look = RenderUtils.getEntityLookedAt(Minecraft.getInstance().player, 32);

        Vec3 cameraPos = camera.getPosition();
        final Frustum frustum = new Frustum(poseStack.last().pose(), event.getProjectionMatrix());
        frustum.prepare(cameraPos.x(), cameraPos.y(), cameraPos.z());

        ClientLevel client = mc.level;
        if (client != null) {
            final boolean combatMode = StylishData.getCap(mc.player).isCombatMode();
            for (Entity entity : client.entitiesForRendering()) {
                if (entity != cameraEntity && entity.isAlive() &&
                        !entity.getIndirectPassengers().iterator().hasNext() &&
                        entity.shouldRender(cameraPos.x(), cameraPos.y(), cameraPos.z()) &&
                        !GeneralUtils.viewBlocked(mc.player, entity, false) &&
                        (entity.noCulling || frustum.isVisible(entity.getBoundingBox()))) {
                    if (TimeSlowData.getCap(entity).getEffectiveSpeed() < 1)
                        steveTime(entity, partialTicks, poseStack);
                    if (entity.getId() == ClientEvents.coyoteTimeID && combatMode)
                        crosshair(entity, partialTicks, poseStack);
                    if (entity instanceof LivingEntity le) {
                        if (!Marks.getCap(le).getActiveMarks().isEmpty() && look != entity)
                            renderMarks(le, partialTicks, poseStack);
                    }
                }
            }

        }

    }

    @SubscribeEvent
    public static void handRaisingThird(RenderPlayerEvent.Pre e) {
        //todo render two-handed weapons and hide weapons in third person
        //e.getRenderer().getModel()
    }

    @SubscribeEvent
    public static void handRaising(RenderHandEvent e) {
        //todo empty render on disarm
        AbstractClientPlayer p = Minecraft.getInstance().player;
        //cancel hand rendering when they are being swung, or when the player is guarding with that arm
        if (StylishData.getCap(p).isCombatMode() &&
                (CombatUtils.getCooledAttackStrength(p, e.getHand(), 0.1f) < 1 ||
                        (p.isShiftKeyDown() && (ClientEvents.lastUsedHandMain ? e.getHand() == InteractionHand.MAIN_HAND : e.getHand() == InteractionHand.OFF_HAND)))) {
            e.setCanceled(true);

//            HumanoidArm armToRender = (p.getMainArm() == HumanoidArm.RIGHT) == (e.getHand() == InteractionHand.MAIN_HAND)
//                    ? HumanoidArm.RIGHT
//                    : HumanoidArm.LEFT;
//            e.getPoseStack().pushPose();
//            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderPlayerArm(e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), e.getEquipProgress(), e.getSwingProgress(), armToRender);
//            e.getPoseStack().popPose();
//            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderPlayerArm(e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), e.getEquipProgress(), e.getSwingProgress(), HumanoidArm.RIGHT);
            return;
        }
        //specifically deals with left hand rendering
        if (e.getHand().equals(InteractionHand.MAIN_HAND) || !GeneralConfig.dual) return;
        if (p == null || p.isInvisible() || (!StylishData.getCap(p).isCombatMode() && (p.swingingArm != InteractionHand.OFF_HAND || !p.swinging)))
            return;
        if (CombatData.getCap(p).getHandBind(InteractionHand.OFF_HAND) > 0) {
            e.setCanceled(true);
            return;
        }
        if (!e.getItemStack().isEmpty()) return;
        if (TwoHandingHandler.suppressOffhand(p, p.getMainHandItem())) return;
        e.setCanceled(true);
        float cd = CombatUtils.getCooledAttackStrength(p, InteractionHand.OFF_HAND, e.getPartialTick());
        float f6 = 1 - (cd * cd * cd);
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderPlayerArm(e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), f6, e.getSwingProgress(), p.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
    }

    @SubscribeEvent
    public static void noFovChange(ComputeFovModifierEvent e) {
        if (CombatData.getCap(e.getPlayer()).isKnockdown())
            e.setNewFovModifier(0.7f);
    }

    private static void steveTime(Entity passedEntity, float partialTicks, PoseStack poseStack) {
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y()), (float) (z - renderPos.z()));
        //RenderSystem.setShaderTexture(0, expose);
        Vec3 offset = passedEntity.position().subtract(renderPos).normalize().scale(passedEntity.getBbWidth() * -1.2);
        poseStack.translate(offset.x, offset.y + passedEntity.getBbHeight() / 2, offset.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = passedEntity.getBbWidth() * 0.02f;
        poseStack.scale(-size, -size, size);
        GuiComponent.blit(poseStack, timeslow, -16, -16, 0, 0, 32, 32, 32, 32);
        int yAmnt = (int) (32 * ((TimeSlowData.getCap(passedEntity).getTimeRemaining() + partialTicks) / 60f));
        GuiComponent.blit(poseStack, timeslow1, -16, 16 - yAmnt, 0, 32 - yAmnt, 32, 32, 32, 32);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }

    private static void crosshair(Entity passedEntity, float partialTicks, PoseStack poseStack) {
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y()), (float) (z - renderPos.z()));
        //RenderSystem.setShaderTexture(0, expose);
        Vec3 offset = passedEntity.position().subtract(renderPos).normalize().scale(passedEntity.getBbWidth() * -1.2);
        poseStack.translate(offset.x, offset.y + passedEntity.getBbHeight() / 2, offset.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = passedEntity.getBbWidth() * 0.03f;
        poseStack.scale(-size, -size, size);
        GuiComponent.blit(poseStack, crosshair, -32, -32, 0, 0, 64, 64, 64, 64);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }

    private static void renderMarks(LivingEntity passedEntity, float partialTicks, PoseStack poseStack) {
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y()), (float) (z - renderPos.z()));
        //RenderSystem.setShaderTexture(0, expose);
        Vec3 offset = passedEntity.position().subtract(renderPos).normalize().scale(passedEntity.getBbWidth() * -1.1);
        poseStack.translate(offset.x, offset.y + passedEntity.getBbHeight(), offset.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = passedEntity.getBbWidth() * 0.02f;
        poseStack.scale(-size, -size, size);

        if (ClientConfig.CONFIG.enemyAfflict.enabled) {
            //marks
            List<SkillData> afflict = new ArrayList<>(Marks.getCap(passedEntity).getActiveMarks().values().stream().filter(a -> a.getSkill().showsMark(a, passedEntity)).collect(Collectors.toList()));
            for (int index = 0; index < afflict.size(); index++) {
                //draw icon
                SkillData s = afflict.get(index);
                RenderSystem.setShaderTexture(0, s.getSkill().icon());
                Color c = s.getSkill().getColor();
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                final int atX = 0 - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8;
                GuiComponent.blit(poseStack, s.getSkill().icon(), atX - 8, 0, 0, 0, 16, 16, 16, 16);

                //dark mask
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                if (s.getState() == Skill.STATE.ACTIVE)//inverted
                    RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                GuiComponent.blit(poseStack, s.getSkill().icon(), atX - 8, 0, 0, 0, 16, 16, 16, 16);

                //draw spin
                if (s.getMaxDuration() >= 1) {
                    //cooldown/active spinny
                    float cd = s.getDuration();
                    float cdPerc = cd / s.getMaxDuration();
                    if (s.getState() == Skill.STATE.ACTIVE)
                        cdPerc = (s.getMaxDuration() - cd) / s.getMaxDuration();
                    RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                    //fixme only works to the halfway point???
                    RenderUtils.drawCooldownCircle(poseStack, atX, 0, 16, cdPerc, s.getState() == Skill.STATE.ACTIVE);
                    RenderSystem.disableBlend();

                    //cooldown number
                    String num = String.valueOf((int) cd);
                    if (Math.ceil(cd) != cd)
                        num = RenderUtils.formatter.format(cd);
                    poseStack.pushPose();
                    RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                    //GuiComponent.drawString(mc.font, String.valueOf(num), atX - mc.font.width(num) / 2, pair.getSecond(), 0xFFFFFF);
                    poseStack.popPose();
                }
//                if (s.getArbitraryFloat() != 0) {
//                    String display = formatter.format(s.getArbitraryFloat());
//                    guiGraphics.drawString(mc.font, display, atX + 4, pair.getSecond() + 8, 0xffffff);
//                }

            }
        }

        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }
}
