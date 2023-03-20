package jackiecrazy.wardance.client.hud;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.client.RenderEvents;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ResourceDisplay implements IGuiOverlay {

    static final DecimalFormat formatter = new DecimalFormat("#.#");
    private static final Cache<LivingEntity, Tuple<StealthUtils.Awareness, Double>> cache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private static final ResourceLocation amo = new ResourceLocation(WarDance.MODID, "textures/hud/amo.png");
    private static final ResourceLocation darkmega = new ResourceLocation(WarDance.MODID, "textures/hud/dark.png");
    private static final ResourceLocation raihud = new ResourceLocation(WarDance.MODID, "textures/hud/thanksrai.png");
    private static final ResourceLocation stealth = new ResourceLocation(WarDance.MODID, "textures/hud/stealth.png");
    static float currentComboLevel = 0;
    private static float currentMightLevel = 0;
    private static float currentSpiritLevel = 0;
    private static float currentPostureLevel = 0;
    private static boolean flip = false;

    private static void drawPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        ClientConfig.BarType b = ClientConfig.CONFIG.enemyPosture.bar;
        if (you) {
            b = ClientConfig.CONFIG.playerPosture.bar;
        }
        switch (b) {
            case AMO -> drawAmoPostureBarAt(you, ms, elb, width, height);
            case DARKMEGA -> drawDarkPostureBarAt(you, ms, elb, width, height);
            case CLASSIC -> drawOldPostureBarAt(you, ms, elb, width, height);
        }
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawOldPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, amo);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
        float cap = itsc.getMaxPosture();
        short barWidth = 182;
        int left = atX - (barWidth / 2);
        float posture = itsc.getPosture();
        final float trueMaxPosture = Float.isFinite(itsc.getMaxPosture()) ? itsc.getMaxPosture() : 1f;
        float posPerc = posture / Math.max(0.1f, trueMaxPosture);
        posPerc = Float.isFinite(posPerc) ? posPerc : 0;
        posPerc = Mth.clamp(posPerc, 0, 1);
        //double shatter = Mth.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            //shatter ticks
//            if (shatter <= 0) {
//                shatter = (double) (ResourceConfig.shatterCooldown + itsc.getShatterCooldown()) / ResourceConfig.shatterCooldown;
//                RenderSystem.color3f(0, 0, 0);
//            }
            int filled;// (int) (shatter * (barWidth + 2));
//            mc.gui.blit(ms, left - 1, atY - 1, 0, 74, filled, 7);
//            RenderSystem.color3f(1, 1, 1);
            //base
            mc.gui.blit(ms, left, atY, 0, 64, barWidth, 5);
            filled = (int) (posPerc * (float) (barWidth));
            RenderSystem.setShaderColor(1 - posPerc, posPerc, 30f / 255, 1);
            //bar on top
            mc.gui.blit(ms, left, atY, 0, 69, filled, 5);
            //fatigue
            //float fatigue = itsc.getMaxPosture() / trueMaxPosture;
            //fatigue = Float.isFinite(fatigue) ? fatigue : 0;
//            filled = (int) (fatigue * (float) (barWidth));
//            RenderSystem.setShaderColor(1, 0.1f, 0.1f, 1);
//            mc.gui.blit(ms, left + filled, atY, filled, 69, barWidth - filled, 5);
            if (itsc.getStaggerTime() > 0) {
                int invulTime = (int) (Mth.clamp((float) itsc.getStaggerTime() / (float) CombatConfig.staggerDuration, 0, 1) * (float) (barWidth));//apparently this is synced to the client?
                RenderSystem.setShaderColor(0, 0, 0, 1);//, ((float) itsc.getPosInvulTime()) / (float) CombatConfig.ssptime);
                mc.gui.blit(ms, left, atY, 0, 69, invulTime, 5);
            }

        }
        mc.getProfiler().pop();
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        //RenderSystem.enableLighting();
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawAmoPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, amo);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
        float cap = itsc.getMaxPosture();
        //182
        //so we want the full size to be 240, 25 to be 125, and every 5 max posture changes this by 20 pixels
        int halfBarWidth = Math.min(240, (int) (Math.sqrt(itsc.getMaxPosture()) * 25)) / 2;
        //divvy by 2 for two-pronged approach
        int flexBarWidth = halfBarWidth;
        final int barHeight = 7;
        //double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            final int shatter = itsc.getShatterCooldown();
            //final int mshatter = (int) GeneralUtils.getAttributeValueSafe(elb, FootworkAttributes.SHATTER.get());
            //take a two-way approach to draw this:
            //first draw the cap brackets
            mc.gui.blit(ms, atX + flexBarWidth, atY - 1, 235, 0, 5, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 5, atY - 1, 0, 0, 5, barHeight);
            //reduce length by fatigue and draw working bracket
            //flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            mc.gui.blit(ms, atX + flexBarWidth, atY - 1, 235, 40, 5, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 5, atY - 1, 0, 40, 5, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStaggerTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStaggerTime() - itsc.getStaggerTime()) * flexBarWidth / (float) itsc.getMaxStaggerTime()) + 3;
                flexBarWidth = time;//Math.max(count, time);
                mc.gui.blit(ms, atX, atY, 238 - flexBarWidth, 13, flexBarWidth, barHeight - 1);
                mc.gui.blit(ms, atX - flexBarWidth, atY, 0, 13, flexBarWidth, barHeight - 1);
            } else {
                //reduce length by posture percentage
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getMaxPosture()) + 4;
                mc.gui.blit(ms, atX, atY, 238 - flexBarWidth, 7, flexBarWidth, barHeight - 1);
                mc.gui.blit(ms, atX - flexBarWidth - 1, atY, 0, 7, flexBarWidth + 1, barHeight - 1);
                //determine bar color by shatter
//                if (shatter > 0 && shatter != mshatter) {//gold that rapidly fades
//                    RenderSystem.setShaderColor(1, 1, 1, (float) shatter / mshatter);
//                    mc.gui.blit(ms, atX, atY, 238 - flexBarWidth, 54, flexBarWidth, barHeight - 1);
//                    mc.gui.blit(ms, atX - flexBarWidth - 1, atY, 0, 54, flexBarWidth + 1, barHeight - 1);
//                    RenderSystem.setShaderColor(1, 1, 1, 1);
//                }
                // draw barrier if eligible
//                final float barrier = itsc.getBarrier();
//                flexBarWidth = (int) (barrier * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
//                int vOff = itsc.consumeBarrier(flip ? -.0001f : 0.0001f) == 0 ? 26 : 19;
//                mc.gui.blit(ms, atX, atY - 1, 239 - flexBarWidth, vOff, flexBarWidth + 1, barHeight);
//                mc.gui.blit(ms, atX - flexBarWidth, atY - 1, 0, vOff, flexBarWidth + 1, barHeight);
                //otherwise draw nothing
            }
            //draw the center bar as the last step
        }
        mc.getProfiler().pop();
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawDarkPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, darkmega);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
        float cap = itsc.getMaxPosture();
        //182
        //so we want the full size to be 240, 25 to be 125, and every 5 max posture changes this by 20 pixels
        int halfBarWidth = Math.min(240, (int) (Math.sqrt(itsc.getMaxPosture()) * 25)) / 2;
        //divvy by 2 for two-pronged approach
        int flexBarWidth = halfBarWidth;
        final int barHeight = 10;
        //double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            //final int shatter = itsc.getShatterCooldown();
            //final int mshatter = (int) GeneralUtils.getAttributeValueSafe(elb, FootworkAttributes.SHATTER.get());
            //take a two-way approach to draw this:

            //first draw the ending brackets
            mc.gui.blit(ms, atX, atY - 2, 240 - flexBarWidth - 10, 0, flexBarWidth + 10, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 10, atY - 2, 0, 0, flexBarWidth + 10, barHeight);
            //reduce length by fatigue and draw working bracket
            //flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            int temp = flexBarWidth;
            mc.gui.blit(ms, atX + flexBarWidth, atY - 2, 232, 50, 10, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 9, atY - 2, 0, 50, 12, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStaggerTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStaggerTime() - itsc.getStaggerTime()) * flexBarWidth / (float) itsc.getMaxStaggerTime()) + 3;
                flexBarWidth = time;//Math.max(count, time);
                mc.gui.blit(ms, atX, atY - 2, 238 - flexBarWidth, 20, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 0, 20, flexBarWidth, barHeight);
            } else if (itsc.getExposeTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxExposeTime() - itsc.getExposeTime()) * flexBarWidth / (float) itsc.getMaxExposeTime()) + 3;
                flexBarWidth = time;//Math.max(count, time);
                mc.gui.blit(ms, atX, atY - 2, 238 - flexBarWidth, 20, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 0, 20, flexBarWidth, barHeight);
            } else {
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getMaxPosture()) + 4;
                //reduce length by posture percentage
                mc.gui.blit(ms, atX, atY - 2, 238 - flexBarWidth, 10, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 2, 10, flexBarWidth, barHeight);
                // draw barrier if eligible
//                flexBarWidth = (int) (itsc.getBarrier() * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
                // hacky flip for usage
//                flip = !flip;
//                final int vOffset = itsc.consumeBarrier(flip ? -0.001f : 0.001f) == 0 ? 40 : 30;
//                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 0, vOffset, flexBarWidth + 1, barHeight);
//                mc.gui.blit(ms, atX, atY - 2, 239 - flexBarWidth, vOffset, flexBarWidth + 1, barHeight);
            }
            // render shatter overlay if present
            if (itsc.getMaxFracture() > 0) {
                int insigniaU = 115, insigniaV = 80, insigniaW = 11;
                if (itsc.getFractureCount() > 0) {//shattering
                    float otemp = (float) itsc.getFractureCount() / itsc.getMaxFracture();
                    //otemp *= 2;//will go over 1 if shatter is less than half
                    if (otemp > 1) {
                        RenderSystem.setShaderColor(1, 1, 1, -(otemp - 2));
                        otemp = 1;
                    }
                    int fini = (int) (otemp * temp);
                    //gold that stretches out to the edges before disappearing
                    mc.gui.blit(ms, atX, atY - 2, 240 - fini - 10, 70, fini + 10, barHeight);
                    mc.gui.blit(ms, atX - fini - 9, atY - 2, 0, 70, fini + 10, barHeight);
                    RenderSystem.setShaderColor(1, 1, 1, -(otemp - 2));
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    insigniaV = 90;
                }
                //insignia
                mc.gui.blit(ms, atX - insigniaW / 2, atY - 2, insigniaU, insigniaV, insigniaW, barHeight - 1);
                //shatter bracket
                mc.gui.blit(ms, atX - temp - 8, atY - 2, 0, 80, 10, barHeight);
                //shatter bracket
                mc.gui.blit(ms, atX + temp, atY - 2, 232, 80, 10, barHeight);
            }
        }
        mc.getProfiler().pop();
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static float updateValue(float f, float to) {
        if (f == -1) return to;
        boolean close = true;
        float temp = f;
        if (to > f) {
            f += Mth.clamp((to - temp) / 20, 0.01, 0.1);
            close = false;
        }
        if (to < f) {
            f += Mth.clamp((to - temp) / 20, -0.1, -0.01);
            close = !close;
        }
        if (close) f = to;
        return f;
    }

    private static Tuple<StealthUtils.Awareness, Double> stealthInfo(LivingEntity at) {
        try {
            return cache.get(at, () -> {
                StealthUtils.Awareness a = StealthUtils.INSTANCE.getAwareness(Minecraft.getInstance().player, at);
                double mult = Minecraft.getInstance().player.getVisibilityPercent(at);
                return new Tuple<>(a, mult * CombatData.getCap(at).visionRange());
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Tuple<>(StealthUtils.Awareness.ALERT, 1d);
    }

    private static Pair<Integer, Integer> translateCoords(DisplayConfigUtils.DisplayData dd, int width, int height) {
        return translateCoords(dd.anchorPoint, dd.numberX, dd.numberY, width, height);
    }

    private static void renderEye(LivingEntity passedEntity, float partialTicks, PoseStack poseStack) {
        final Tuple<StealthUtils.Awareness, Double> info = stealthInfo(passedEntity);
        double dist = info.getB();
        int shift = 0;
        switch (info.getA()) {
            case ALERT:
                return;
            case DISTRACTED:
                shift = 1;
                break;
            case UNAWARE:
                if (Minecraft.getInstance().player != null)
                    shift = passedEntity.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                break;
        }
        if (info.getB() < 0) shift = 0;
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y() + passedEntity.getBbHeight()), (float) (z - renderPos.z()));
        RenderSystem.setShaderTexture(0, stealth);
        poseStack.translate(0.0D, (double) 0.5, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = Mth.clamp(0.002F * CombatData.getCap(passedEntity).getMaxPosture(), 0.015f, 0.1f);
        poseStack.scale(-size, -size, size);
        GuiComponent.blit(poseStack, -16, -8, 0, shift * 16, 32, 16, 64, 64);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
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

    @Override
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        final Minecraft mc = Minecraft.getInstance();
        //TODO update in accordance with new might
        if (mc.getCameraEntity() instanceof Player) {
            LocalPlayer player = mc.player;
            ICombatCapability cap = CombatData.getCap(player);
            RenderSystem.setShaderTexture(0, raihud);
            currentSpiritLevel = updateValue(currentSpiritLevel, cap.getSpirit());
            currentMightLevel = updateValue(currentMightLevel, cap.getMight());
            currentComboLevel = cap.getRank() > currentComboLevel ? updateValue(currentComboLevel, cap.getRank()) : cap.getRank();
            //yourCurrentPostureLevel = updateValue(yourCurrentPostureLevel, cap.getPosture());
            if (cap.isCombatMode()) {
                stack.pushPose();
                RenderSystem.enableBlend();
                //RenderSystem.enableAlphaTest();
                Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.might, width, height);
                int x = Math.max(pair.getFirst() - 16, 0);
                int y = Math.min(pair.getSecond() - 16, height - 32);
                int fillHeight = (int) (Math.min(1, currentMightLevel % 1) * 32);
                if (ClientConfig.CONFIG.might.enabled) {
                    //might circle
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    stack.pushPose();
                    stack.pushPose();
                    mc.gui.blit(stack, x, y, 0, 64, 32, 32);
                    stack.popPose();
                    //might circle filling
                    stack.pushPose();
                    mc.gui.blit(stack, x, y + 32 - fillHeight, 0, 96 - fillHeight, 32, fillHeight);
                    stack.popPose();
                    fillHeight += Math.min(fillHeight, 3);
                    fillHeight = Math.min(fillHeight, 32);
                    //might crown, rendered when above half might
                    if (currentMightLevel >= cap.getMaxMight() / 2) {
                        stack.pushPose();
                        mc.gui.blit(stack, x, y, 32, 64, 32, 32);
                        stack.popPose();
                    }
                    //might crown plus pro ultra, rendered at max might
                    if (currentMightLevel == cap.getMaxMight()) {
                        stack.pushPose();
                        mc.gui.blit(stack, x, y, 64, 64, 32, 32);
                        stack.popPose();
                        stack.popPose();
                    }
                }
                pair = translateCoords(ClientConfig.CONFIG.spirit, width, height);
                x = Mth.clamp(pair.getFirst() - 16, 0, width - 32);
                y = Mth.clamp(pair.getSecond() - 16, 0, height - 32);
                fillHeight = (int) (Math.min(1, currentSpiritLevel / cap.getMaxSpirit()) * 32);
                String display = formatter.format(currentSpiritLevel) + "/" + formatter.format(cap.getMaxSpirit());
                //spirit circle
                stack.pushPose();
                if (ClientConfig.CONFIG.spirit.enabled) {
                    stack.pushPose();
                    mc.gui.blit(stack, x, y, 0, 96, 32, 32);
                    stack.popPose();
                    //spirit circle filling
                    stack.pushPose();
                    mc.gui.blit(stack, x, y + 32 - fillHeight, 0, 128 - fillHeight, 32, fillHeight);
                    stack.popPose();
                    fillHeight += Math.min(fillHeight, 3);
                    fillHeight = Math.min(fillHeight, 32);
                    //spirit base
                    stack.pushPose();
                    mc.gui.blit(stack, x, y + 1, 32, 96, 32, 32);
                    stack.popPose();
                    //spirit illumination
                    stack.pushPose();
                    mc.gui.blit(stack, x, y + 33 - fillHeight, 64, 128 - fillHeight, 32, fillHeight);
                    stack.popPose();
                }
                if (ClientConfig.CONFIG.spiritNumber.enabled) {
                    pair = translateCoords(ClientConfig.CONFIG.spiritNumber, width, height);
                    mc.font.drawShadow(stack, display, pair.getFirst() - mc.font.width(display) / 2f, pair.getSecond() - 2, ClientConfig.spiritColor);
                }
                if (ClientConfig.CONFIG.mightNumber.enabled) {
                    pair = translateCoords(ClientConfig.CONFIG.mightNumber, width, height);
                    display = formatter.format(currentMightLevel) + "/" + formatter.format(cap.getMaxMight());
                    mc.font.drawShadow(stack, display, pair.getFirst() - mc.font.width(display) / 2f, pair.getSecond() - 2, ClientConfig.mightColor);
                }
                stack.popPose();

                //RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                stack.popPose();
                //combo bar at 224,20 to 229, 121. Grace at 222,95 to 224, 121
                //initial bar
                RenderSystem.enableBlend();
                stack.pushPose();
                if (ClientConfig.CONFIG.combo.enabled) {
                    RenderSystem.setShaderTexture(0, raihud);
                    int combowidth = 32;
                    float workingCombo = currentComboLevel;
                    int comboU = (int) (Mth.clamp(Math.floor(workingCombo), 0, 4)) * 32;
                    int divisor = 1;
                    if (workingCombo >= 4)//S
                        divisor = 2;
                    if (workingCombo >= 6) {//SS
                        combowidth = 33;
                        comboU = 159;
                        divisor = 3;
                    }
                    if (workingCombo >= 9) {//SSS
                        combowidth = 64;
                        comboU = 192;
                        fillHeight = (int) ((workingCombo - 9) * 32f);
                    } else if (divisor > 1) fillHeight = (int) ((workingCombo - divisor * 2) / divisor * 32f);
                    else fillHeight = (int) ((workingCombo - Math.floor(workingCombo)) * 32f);
                    pair = translateCoords(ClientConfig.CONFIG.combo, width, height);
                    x = Mth.clamp(pair.getFirst() - combowidth / 2, 0, width - combowidth);
                    y = Mth.clamp(pair.getSecond() - 23, 0, height - 46);
                    mc.gui.blit(stack, x, y, comboU, 0, combowidth, 32);
                    //fancy fill percentage
                    mc.gui.blit(stack, x, y + 33 - fillHeight, comboU, 65 - fillHeight, combowidth, fillHeight - 2);
                }

                stack.popPose();
                RenderSystem.disableBlend();
            }
            RenderSystem.setShaderTexture(0, amo);
            //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
            if (cap.isCombatMode() || cap.getPosture() < cap.getMaxPosture() || cap.getStaggerTime() > 0)
                drawPostureBarAt(true, stack, player, width, height);
            Entity look = RenderEvents.getEntityLookedAt(player, 32);
            if (look instanceof LivingEntity) {
                LivingEntity looked = (LivingEntity) look;
                List<SkillData> afflict = new ArrayList<>();
                final ISkillCapability skill = CasterData.getCap(player);
                if (ClientConfig.CONFIG.enemyAfflict.enabled) {
                    //coup de grace
                    final Skill variant = skill.getEquippedVariation(SkillArchetypes.coup_de_grace);
                    if (look != player && variant instanceof CoupDeGrace && skill.isSkillUsable(variant)) {
                        CoupDeGrace cdg = (CoupDeGrace) variant;
                        if (cdg.willKillOnCast(player, looked)) {
                            afflict.add(new SkillData(cdg, 0, 0));
                        }
                    }
                    //marks
                    afflict.addAll(Marks.getCap(looked).getActiveMarks().values().stream().filter(a -> a.getSkill().showsMark(a, looked)).collect(Collectors.toList()));
                    Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.enemyAfflict, width, height);
                    for (int index = 0; index < afflict.size(); index++) {
                        SkillData s = afflict.get(index);
                        RenderSystem.setShaderTexture(0, s.getSkill().icon());
                        Color c = s.getSkill().getColor();
                        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                        GuiComponent.blit(stack, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);
                        if (s.getMaxDuration() != 0) {
                            String display = formatter.format(s.getDuration());
                            mc.font.drawShadow(stack, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond() - 2, 0);
                        }

                    }
                }
                RenderSystem.setShaderColor(1, 1, 1, 1);
                stealth:
                {
                    if (ClientConfig.CONFIG.stealth.enabled && cap.isCombatMode()) {
                        Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.stealth, width, height);
                        final Tuple<StealthUtils.Awareness, Double> info = stealthInfo(looked);
                        double dist = info.getB();
                        int shift = 0;
                        switch (info.getA()) {
                            case ALERT:
                                break stealth;
                            case DISTRACTED:
                                shift = 1;
                                break;
                            case UNAWARE:
                                if (Minecraft.getInstance().player != null)
                                    shift = looked.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                                break;
                        }
                        if (info.getB() < 0) shift = 0;
                        RenderSystem.setShaderTexture(0, stealth);
                        GuiComponent.blit(stack, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, 32, 16, 64, 64);
                    }
                }
                final ICombatCapability loocap = CombatData.getCap((LivingEntity) look);
                if (ClientConfig.CONFIG.enemyPosture.enabled && (cap.isCombatMode() || loocap.getPosture() < loocap.getMaxPosture() || loocap.isStaggered() || loocap.isExposed()))
                    drawPostureBarAt(false, stack, looked, width, height);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
            }
        }
    }


}
