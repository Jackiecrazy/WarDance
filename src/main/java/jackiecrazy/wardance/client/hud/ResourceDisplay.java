package jackiecrazy.wardance.client.hud;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatCapability;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ResourceDisplay implements IGuiOverlay {

    private static final Cache<LivingEntity, Tuple<StealthUtils.Awareness, Double>> cache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private static final ResourceLocation amo = new ResourceLocation(WarDance.MODID, "textures/hud/amo.png");
    private static final ResourceLocation darkmega = new ResourceLocation(WarDance.MODID, "textures/hud/dark.png");
    private static final ResourceLocation newdark = new ResourceLocation(WarDance.MODID, "textures/hud/mega.png");
    private static final ResourceLocation raihud = new ResourceLocation(WarDance.MODID, "textures/hud/thanksrai.png");
    private static final ResourceLocation stealth = new ResourceLocation(WarDance.MODID, "textures/hud/stealth.png");
    private static final ResourceLocation might = new ResourceLocation(WarDance.MODID, "textures/hud/bars.png");
    static float currentComboLevel = 0;
    private static float currentMightLevel = 0;
    private static float currentSpiritLevel = 0;
    private static float scurrentEvasion = 0, lcurrentEvasion = 0;
    private static boolean flip = false;
    private static int snewDarkAnimFrames = 0, lnewDarkAnimFrames = 0, spiritFrames = 0;

    private static void drawPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        ClientConfig.BarType b = ClientConfig.CONFIG.enemyPosture.bar;
        if (you) {
            b = ClientConfig.CONFIG.playerPosture.bar;
        }
        switch (b) {
            case AMO -> drawAmoPostureBarAt(you, ms, elb, width, height);
            case DARKMEGA -> drawDarkPostureBarAt(you, ms, elb, width, height);
            case NEWDARK -> drawNewDarkPostureBarAt(you, ms, elb, width, height);
            case CLASSIC -> drawOldPostureBarAt(you, ms, elb, width, height);
        }
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawOldPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? RenderUtils.translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : RenderUtils.translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
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
            if (itsc.getStunTime() > 0) {
                int invulTime = (int) (Mth.clamp((float) itsc.getStunTime() / (float) CombatConfig.staggerDuration, 0, 1) * (float) (barWidth));//apparently this is synced to the client?
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
        Pair<Integer, Integer> pair = you ? RenderUtils.translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : RenderUtils.translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
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
            final int shatter = itsc.getEvade();
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
            if (itsc.getStunTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStunTime() - itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
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
        Pair<Integer, Integer> pair = you ? RenderUtils.translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : RenderUtils.translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
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
            if (itsc.getStunTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStunTime() - itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
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

    /**
     * Draws it with the coord as its center
     */
    private static void drawNewDarkPostureBarAt(boolean you, PoseStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? RenderUtils.translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : RenderUtils.translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, newdark);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
        float cap = itsc.getMaxPosture();
        //182
        //so we want the full size to be 240, 25 to be 125, and every 5 max posture changes this by 20 pixels
        int halfBarWidth = Math.min(240, (int) (Math.sqrt(itsc.getMaxPosture()) * 25)) / 2;
        //divvy by 2 for two-pronged approach
        int flexBarWidth = halfBarWidth + 3;
        final int barHeight = 12;
        //double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            //draw working bracket
            final int barY = atY - barHeight / 2;
            mc.gui.blit(ms, atX, barY, 243 - flexBarWidth, 0, flexBarWidth, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth, barY, 0, 0, flexBarWidth, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStunTime() > 0) {
                flexBarWidth = (int) ((itsc.getMaxStunTime() - itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
                mc.gui.blit(ms, atX, barY, 243 - flexBarWidth, 24, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, barY, 0, 24, flexBarWidth, barHeight);
            } else if (itsc.getExposeTime() > 0) {
                flexBarWidth = (int) ((itsc.getMaxExposeTime() - itsc.getExposeTime()) * flexBarWidth / (float) itsc.getMaxExposeTime()) + 3;
                mc.gui.blit(ms, atX, barY, 243 - flexBarWidth, 24, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, barY, 0, 24, flexBarWidth, barHeight);
            } else {
                //otherwise draw normal posture
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getMaxPosture()) + 3;
                mc.gui.blit(ms, atX, barY, 243 - flexBarWidth, 12, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, barY, 0, 12, flexBarWidth, barHeight);
            }
            // render fracture if present
            if (itsc.getMaxFracture() > 0) {
                if (itsc.getFractureCount() > 0) {//shattering
                    float otemp = (float) itsc.getFractureCount() / itsc.getMaxFracture();
                    int fini = (int) (otemp * halfBarWidth);
                    int shatterV = Math.min(36 + (int) (otemp * 2.8) * 12, 60);
                    //gold that stretches out to the edges before disappearing
                    mc.gui.blit(ms, atX + 5, atY - barHeight / 2, 243 - fini, shatterV, fini, barHeight);
                    mc.gui.blit(ms, atX - fini - 5, atY - barHeight / 2, 0, shatterV, fini, barHeight);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }
            }
            //render insignia
            {
                int insigniaWH = 24;
                int iconW = 12, iconH = 10;
                //normal, use green
                int statusU = 0, statusV = 83;
                int iconU = 0, iconV = 72;
                //unsteady override
                if (elb.hasEffect(FootworkEffects.UNSTEADY.get()))
                    iconU = 12;
                //danger, use red
                if (itsc.isVulnerable()) {
                    statusU = 48;
                    if (itsc.isExposed())
                        iconU = 36;
                    if (itsc.isStunned())
                        iconU = 24;
                }
                //recharge, use yellow
                else if (itsc.getStunTime() != 0 || itsc.getExposeTime() != 0) {
                    statusU = 48;
                    iconU = 12;
                }
                //draw status color
                mc.gui.blit(ms, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                double evasionPerc = Math.min(1, itsc.getEvade() * 1d / CombatCapability.EVADE_CHARGE);
                //draw status icon
                mc.gui.blit(ms, atX - iconW / 2, atY - iconH / 2, iconU, iconV, iconW, iconH);
                //draw evasion
                statusU = 72;
                mc.gui.blit(ms, atX - insigniaWH / 2, atY + insigniaWH / 2 - (int) (insigniaWH * evasionPerc), statusU, statusV + insigniaWH - (int) (insigniaWH * evasionPerc), insigniaWH, (int) (insigniaWH * evasionPerc));
                //draw evasion animation when needed
                if (you) {
                    //filled up, start animation frames
                    if (scurrentEvasion != (float) evasionPerc && (evasionPerc == 1 || evasionPerc < scurrentEvasion)) {
                        snewDarkAnimFrames = (int) (56 * (evasionPerc - 0.5));
                    }
                    scurrentEvasion = (float) evasionPerc;
                    statusV = 107;
                    if (snewDarkAnimFrames > 0) {
                        statusU = (4 - (snewDarkAnimFrames / 3)) * 24;
                        mc.gui.blit(ms, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        snewDarkAnimFrames--;
                    } else if (snewDarkAnimFrames < 0) {
                        statusU = (7 + (snewDarkAnimFrames / 4)) * 24;
                        statusV = 131;
                        mc.gui.blit(ms, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        snewDarkAnimFrames++;
                    }
                } else {
                    //filled up, start animation frames
                    if (lcurrentEvasion != (float) evasionPerc && (evasionPerc == 1 || evasionPerc < lcurrentEvasion)) {
                        lnewDarkAnimFrames = (int) (2000 * (evasionPerc - 0.5));
                    }
                    lcurrentEvasion = (float) evasionPerc;
                    statusV = 107;
                    if (lnewDarkAnimFrames > 0) {
                        statusU = (4 - (lnewDarkAnimFrames / 3)) * 24;
                        mc.gui.blit(ms, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        lnewDarkAnimFrames--;
                    } else if (lnewDarkAnimFrames < 0) {
                        statusU = (7 + (lnewDarkAnimFrames / 4)) * 24;
                        statusV = 131;
                        mc.gui.blit(ms, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        lnewDarkAnimFrames++;
                    }
                }
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

    @Override
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        final Minecraft mc = Minecraft.getInstance();
        //TODO update in accordance with new might
        if (mc.getCameraEntity() instanceof Player) {
            LocalPlayer player = mc.player;
            ICombatCapability cap = CombatData.getCap(player);
            RenderSystem.setShaderTexture(0, raihud);
            float prev = currentSpiritLevel;
            currentSpiritLevel = updateValue(currentSpiritLevel, cap.getSpirit());
            if ((int) prev < (int) currentSpiritLevel)//advance up 1
                spiritFrames = 10;
            currentMightLevel = updateValue(currentMightLevel, cap.getMight());
            currentComboLevel = cap.getRank() > currentComboLevel ? updateValue(currentComboLevel, cap.getRank()) : cap.getRank();
            //yourCurrentPostureLevel = updateValue(yourCurrentPostureLevel, cap.getPosture());
            if (cap.isCombatMode()) {
                {
                    stack.pushPose();
                    RenderSystem.enableBlend();
                    //RenderSystem.enableAlphaTest();
                    Pair<Integer, Integer> pair = RenderUtils.translateCoords(ClientConfig.CONFIG.might, width, height);
                    int x = Math.max(pair.getFirst() - 16, 0);
                    int y = Math.min(pair.getSecond() - 16, height - 32);
                    int fillHeight = (int) (currentMightLevel * 32 / cap.getMaxMight());
                    if (ClientConfig.CONFIG.might.enabled) {
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.setShaderTexture(0, might);
                        drawBar(stack, width / 2, height / 2, currentMightLevel, currentMightLevel == cap.getMaxMight());
                        RenderSystem.setShaderTexture(0, raihud);
                        //might circle
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                        stack.pushPose();
                        {
                            stack.pushPose();
                            mc.gui.blit(stack, x, y, 0, 64, 32, 32);
                            stack.popPose();
                        }
                        //might circle filling
                        {
                            stack.pushPose();
                            mc.gui.blit(stack, x, y + 32 - fillHeight, 32, 96 - fillHeight, 32, fillHeight);
                            stack.popPose();
                        }
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //might crown plus pro ultra, rendered at max might
                        if (currentMightLevel == cap.getMaxMight()) {
                            {
                                stack.pushPose();
                                mc.gui.blit(stack, x, y, 64, 64, 32, 32);
                                stack.popPose();
                            }
                        }
                        stack.popPose();
                    }


                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.spirit, width, height);
                    x = Mth.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = Mth.clamp(pair.getSecond() - 16, 0, height - 32);
                    fillHeight = (int) (Math.min(1, currentSpiritLevel / cap.getMaxSpirit()) * 32);
                    String display = RenderUtils.formatter.format(currentSpiritLevel) + "/" + RenderUtils.formatter.format(cap.getMaxSpirit());
                    //spirit circle
                    {
                        stack.pushPose();
                        if (ClientConfig.CONFIG.spirit.enabled) {
                            RenderSystem.setShaderTexture(0, might);
                            //determine whether we are on the left side
                            boolean invert = true;//x>width/2;
                            int count = Mth.ceil(cap.getMaxSpirit());
                            int spiritWidth = 13;
                            int spiritHeight = 21;
                            int spiritV = 80;
                            int startX = width / 2 + 7;//x - spiritWidth * count / 2;
                            int startY = height / 2 - 4;//y - spiritHeight / 2;
                            //draw empty/full spirits
                            if (invert)
                                //fixme doesn't work
                                for (int i = 0; i < count; i++) {
                                    int inverted = count - i;
                                    //if inverted (on left side), draw empty first
                                    GuiComponent.blit(stack, startX + inverted * spiritWidth, startY, cap.getMaxSpirit()-(int) currentSpiritLevel <= i? 13 : 0, spiritV, spiritWidth, spiritHeight, 256, 256);
                                    if ((int) currentSpiritLevel == inverted + 1 && spiritFrames > 0) {
                                        spiritFrames--;
                                        GuiComponent.blit(stack, startX + i * spiritWidth, startY, 26, spiritV, spiritWidth, spiritHeight, 256, 256);
                                    }

                                }
                            else
                                for (int i = 0; i < count; i++) {
                                    //if not inverted (on left side), draw full first
                                    GuiComponent.blit(stack, startX + i * spiritWidth, startY, ((int) currentSpiritLevel < i + 1)? 13 : 0, spiritV, spiritWidth, spiritHeight, 256, 256);
                                    if ((int) currentSpiritLevel == i + 1 && spiritFrames > 0) {
                                        spiritFrames--;
                                        GuiComponent.blit(stack, startX + i * spiritWidth, startY, 26, spiritV, spiritWidth, spiritHeight, 256, 256);
                                    }

                                }
                            //draw a new spirit that was made just now
                            //draw full/empty spirits
                            RenderSystem.setShaderTexture(0, raihud);

                            {
                                stack.pushPose();
                                mc.gui.blit(stack, x, y, 0, 96, 32, 32);
                                stack.popPose();
                            }
                            //spirit circle filling
                            {
                                stack.pushPose();
                                mc.gui.blit(stack, x, y + 32 - fillHeight, 0, 128 - fillHeight, 32, fillHeight);
                                stack.popPose();
                            }
                            fillHeight += Math.min(fillHeight, 3);
                            fillHeight = Math.min(fillHeight, 32);
                            //spirit base
                            {
                                stack.pushPose();
                                mc.gui.blit(stack, x, y + 1, 32, 96, 32, 32);
                                stack.popPose();
                            }
                            //spirit illumination
                            {
                                stack.pushPose();
                                mc.gui.blit(stack, x, y + 33 - fillHeight, 64, 128 - fillHeight, 32, fillHeight);
                                stack.popPose();
                            }
                        }
                    }
                    //numbers
                    {
                        if (ClientConfig.CONFIG.spiritNumber.enabled) {
                            pair = RenderUtils.translateCoords(ClientConfig.CONFIG.spiritNumber, width, height);
                            mc.font.drawShadow(stack, display, pair.getFirst() - mc.font.width(display) / 2f, pair.getSecond() - 2, ClientConfig.spiritColor);
                        }
                        if (ClientConfig.CONFIG.mightNumber.enabled) {
                            pair = RenderUtils.translateCoords(ClientConfig.CONFIG.mightNumber, width, height);
                            display = RenderUtils.formatter.format(currentMightLevel) + "/" + RenderUtils.formatter.format(cap.getMaxMight());
                            mc.font.drawShadow(stack, display, pair.getFirst() - mc.font.width(display) / 2f, pair.getSecond() - 2, ClientConfig.mightColor);
                        }
                        stack.popPose();
                    }

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
                        pair = RenderUtils.translateCoords(ClientConfig.CONFIG.combo, width, height);
                        x = Mth.clamp(pair.getFirst() - combowidth / 2, 0, width - combowidth);
                        y = Mth.clamp(pair.getSecond() - 23, 0, height - 46);
                        mc.gui.blit(stack, x, y, comboU, 0, combowidth, 32);
                        //fancy fill percentage
                        mc.gui.blit(stack, x, y + 33 - fillHeight, comboU, 65 - fillHeight, combowidth, fillHeight - 2);
                    }

                    stack.popPose();
                }
                RenderSystem.disableBlend();
            }
            RenderSystem.setShaderTexture(0, amo);
            //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
            if (cap.isCombatMode() || cap.getPosture() < cap.getMaxPosture() || cap.getStunTime() > 0)
                drawPostureBarAt(true, stack, player, width, height);


            Entity look = RenderUtils.getEntityLookedAt(player, 32);
            if (look instanceof LivingEntity looked) {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                stealth:
                {
                    if (ClientConfig.CONFIG.stealth.enabled && cap.isCombatMode()) {
                        Pair<Integer, Integer> pair = RenderUtils.translateCoords(ClientConfig.CONFIG.stealth, width, height);
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
                if (ClientConfig.CONFIG.enemyPosture.enabled && (cap.isCombatMode() || loocap.getPosture() < loocap.getMaxPosture() || loocap.isVulnerable()))
                    drawPostureBarAt(false, stack, looked, width, height);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
            }
        }
    }

    private void drawBar(PoseStack stack, int x, int y, int index, int to, int from) {
        index %= 7;
        GuiComponent.blit(stack, x, y, -90, 0, Math.max(0, index * 5 * 2 - 5), to, 5, 256, 256);
        if (from != 0) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiComponent.blit(stack, x, y, -90, 0, index * 5 * 2 + 5, to, 5, 256, 256);
            RenderSystem.disableBlend();
        }

    }

    private void drawBar(PoseStack stack, int x, int y, float prog, boolean maxed) {
        final int length = 183;
        int index = (int) prog;
        this.drawBar(stack, x, y, index, length - 1, 0);
        int i = (int) ((prog - (int) prog) * length);
        if (prog == index && prog != 0)//maxed
            i = length;
        if (i > 0) {
            this.drawBar(stack, x, y, index, i, 5);
        }
        if (maxed) {
            //gold covering
            GuiComponent.blit(stack, x, y, -90, 0, 70, 183, 5, 256, 256);
            //gold cap
            if (prog != (int) prog)
                GuiComponent.blit(stack, x + i - 1, y, -90, 179, 70, 4, 5, 256, 256);
        }

    }
}
