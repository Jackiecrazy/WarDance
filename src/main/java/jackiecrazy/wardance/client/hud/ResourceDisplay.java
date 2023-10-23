package jackiecrazy.wardance.client.hud;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.client.GuiComponent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatCapability;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    private static void drawPostureBarAt(boolean you, GuiGraphics ms, LivingEntity elb, int width, int height) {
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
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawOldPostureBarAt(boolean you, GuiGraphics ms, LivingEntity elb, int width, int height) {
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
//            ms.blit(newdark, left - 1, atY - 1, 0, 74, filled, 7);
//            RenderSystem.color3f(1, 1, 1);
            //base
            ms.blit(amo, left, atY, 0, 64, barWidth, 5);
            filled = (int) (posPerc * (float) (barWidth));
            RenderSystem.setShaderColor(1 - posPerc, posPerc, 30f / 255, 1);
            //bar on top
            ms.blit(amo, left, atY, 0, 69, filled, 5);
            //fatigue
            //float fatigue = itsc.getMaxPosture() / trueMaxPosture;
            //fatigue = Float.isFinite(fatigue) ? fatigue : 0;
//            filled = (int) (fatigue * (float) (barWidth));
//            RenderSystem.setShaderColor(1, 0.1f, 0.1f, 1);
//            ms.blit(newdark, left + filled, atY, filled, 69, barWidth - filled, 5);
            if (itsc.getStunTime() > 0) {
                int invulTime = (int) (Mth.clamp((float) itsc.getStunTime() / (float) CombatConfig.staggerDuration, 0, 1) * (float) (barWidth));//apparently this is synced to the client?
                RenderSystem.setShaderColor(0, 0, 0, 1);//, ((float) itsc.getPosInvulTime()) / (float) CombatConfig.ssptime);
                ms.blit(amo, left, atY, 0, 69, invulTime, 5);
            }

        }
        mc.getProfiler().pop();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        //RenderSystem.enableLighting();
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawAmoPostureBarAt(boolean you, GuiGraphics ms, LivingEntity elb, int width, int height) {
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
            ms.blit(amo, atX + flexBarWidth, atY - 1, 235, 0, 5, barHeight);
            ms.blit(amo, atX - flexBarWidth - 5, atY - 1, 0, 0, 5, barHeight);
            //reduce length by fatigue and draw working bracket
            //flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            ms.blit(amo, atX + flexBarWidth, atY - 1, 235, 40, 5, barHeight);
            ms.blit(amo, atX - flexBarWidth - 5, atY - 1, 0, 40, 5, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStunTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStunTime() - itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
                flexBarWidth = time;//Math.max(count, time);
                ms.blit(amo, atX, atY, 238 - flexBarWidth, 13, flexBarWidth, barHeight - 1);
                ms.blit(amo, atX - flexBarWidth, atY, 0, 13, flexBarWidth, barHeight - 1);
            } else {
                //reduce length by posture percentage
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getMaxPosture()) + 4;
                ms.blit(amo, atX, atY, 238 - flexBarWidth, 7, flexBarWidth, barHeight - 1);
                ms.blit(amo, atX - flexBarWidth - 1, atY, 0, 7, flexBarWidth + 1, barHeight - 1);
                //determine bar color by shatter
//                if (shatter > 0 && shatter != mshatter) {//gold that rapidly fades
//                    RenderSystem.setShaderColor(1, 1, 1, (float) shatter / mshatter);
//                    ms.blit(newdark, atX, atY, 238 - flexBarWidth, 54, flexBarWidth, barHeight - 1);
//                    ms.blit(newdark, atX - flexBarWidth - 1, atY, 0, 54, flexBarWidth + 1, barHeight - 1);
//                    RenderSystem.setShaderColor(1, 1, 1, 1);
//                }
                // draw barrier if eligible
//                final float barrier = itsc.getBarrier();
//                flexBarWidth = (int) (barrier * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
//                int vOff = itsc.consumeBarrier(flip ? -.0001f : 0.0001f) == 0 ? 26 : 19;
//                ms.blit(newdark, atX, atY - 1, 239 - flexBarWidth, vOff, flexBarWidth + 1, barHeight);
//                ms.blit(newdark, atX - flexBarWidth, atY - 1, 0, vOff, flexBarWidth + 1, barHeight);
                //otherwise draw nothing
            }
            //draw the center bar as the last step
        }
        mc.getProfiler().pop();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawDarkPostureBarAt(boolean you, GuiGraphics ms, LivingEntity elb, int width, int height) {
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
            ms.blit(darkmega, atX, atY - 2, 240 - flexBarWidth - 10, 0, flexBarWidth + 10, barHeight);
            ms.blit(darkmega, atX - flexBarWidth - 10, atY - 2, 0, 0, flexBarWidth + 10, barHeight);
            //reduce length by fatigue and draw working bracket
            //flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            int temp = flexBarWidth;
            ms.blit(darkmega, atX + flexBarWidth, atY - 2, 232, 50, 10, barHeight);
            ms.blit(darkmega, atX - flexBarWidth - 9, atY - 2, 0, 50, 12, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStunTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStunTime() - itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
                flexBarWidth = time;//Math.max(count, time);
                ms.blit(darkmega, atX, atY - 2, 238 - flexBarWidth, 20, flexBarWidth, barHeight);
                ms.blit(darkmega, atX - flexBarWidth, atY - 2, 0, 20, flexBarWidth, barHeight);
            } else if (itsc.getExposeTime() > 0) {
                //int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxExposeTime() - itsc.getExposeTime()) * flexBarWidth / (float) itsc.getMaxExposeTime()) + 3;
                flexBarWidth = time;//Math.max(count, time);
                ms.blit(darkmega, atX, atY - 2, 238 - flexBarWidth, 20, flexBarWidth, barHeight);
                ms.blit(darkmega, atX - flexBarWidth, atY - 2, 0, 20, flexBarWidth, barHeight);
            } else {
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getMaxPosture()) + 4;
                //reduce length by posture percentage
                ms.blit(darkmega, atX, atY - 2, 238 - flexBarWidth, 10, flexBarWidth, barHeight);
                ms.blit(darkmega, atX - flexBarWidth, atY - 2, 2, 10, flexBarWidth, barHeight);
                // draw barrier if eligible
//                flexBarWidth = (int) (itsc.getBarrier() * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
                // hacky flip for usage
//                flip = !flip;
//                final int vOffset = itsc.consumeBarrier(flip ? -0.001f : 0.001f) == 0 ? 40 : 30;
//                ms.blit(newdark, atX - flexBarWidth, atY - 2, 0, vOffset, flexBarWidth + 1, barHeight);
//                ms.blit(newdark, atX, atY - 2, 239 - flexBarWidth, vOffset, flexBarWidth + 1, barHeight);
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
                    ms.blit(darkmega, atX, atY - 2, 240 - fini - 10, 70, fini + 10, barHeight);
                    ms.blit(darkmega, atX - fini - 9, atY - 2, 0, 70, fini + 10, barHeight);
                    RenderSystem.setShaderColor(1, 1, 1, -(otemp - 2));
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    insigniaV = 90;
                }
                //insignia
                ms.blit(darkmega, atX - insigniaW / 2, atY - 2, insigniaU, insigniaV, insigniaW, barHeight - 1);
                //shatter bracket
                ms.blit(darkmega, atX - temp - 8, atY - 2, 0, 80, 10, barHeight);
                //shatter bracket
                ms.blit(darkmega, atX + temp, atY - 2, 232, 80, 10, barHeight);
            }
        }
        mc.getProfiler().pop();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawNewDarkPostureBarAt(boolean you, GuiGraphics ms, LivingEntity elb, int width, int height) {
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
            ms.blit(newdark, atX, barY, 243 - flexBarWidth, 0, flexBarWidth, barHeight);
            ms.blit(newdark, atX - flexBarWidth, barY, 0, 0, flexBarWidth, barHeight);
            //grayscale and change width if staggered
            if (itsc.getExposeTime() > 0) {
                flexBarWidth = (int) ((itsc.getExposeTime()) * flexBarWidth / (float) itsc.getMaxExposeTime()) + 3;
                ms.blit(newdark, atX, barY, 243 - flexBarWidth, 24, flexBarWidth, barHeight);
                ms.blit(newdark, atX - flexBarWidth, barY, 0, 24, flexBarWidth, barHeight);
            }else if (itsc.getStunTime() > 0) {
                flexBarWidth = (int) ((itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
                ms.blit(newdark, atX, barY, 243 - flexBarWidth, 24, flexBarWidth, barHeight);
                ms.blit(newdark, atX - flexBarWidth, barY, 0, 24, flexBarWidth, barHeight);
            }  else {
                //otherwise draw normal posture
                flexBarWidth = (int) ((itsc.getMaxPosture()-itsc.getPosture()) * halfBarWidth / itsc.getMaxPosture()) + 3;
                ms.blit(newdark, atX, barY, 243 - flexBarWidth, 12, flexBarWidth, barHeight);
                ms.blit(newdark, atX - flexBarWidth, barY, 0, 12, flexBarWidth, barHeight);
            }
            // render fracture if present
            if (itsc.getMaxFracture() > 0) {
                if (itsc.getFractureCount() > 0) {//shattering
                    float otemp = (float) itsc.getFractureCount() / itsc.getMaxFracture();
                    int fini = (int) (otemp * halfBarWidth);
                    int shatterV = Math.min(36 + (int) (otemp * 2.8) * 12, 60);
                    //gold that stretches out to the edges before disappearing
                    ms.blit(newdark, atX + 5, atY - barHeight / 2, 243 - fini, shatterV, fini, barHeight);
                    ms.blit(newdark, atX - fini - 5, atY - barHeight / 2, 0, shatterV, fini, barHeight);
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
                    if (itsc.isStunned())
                        iconU = 24;
                    if (itsc.isExposed())
                        iconU = 36;
                }
                //recharge, use yellow
                else if (itsc.getStunTime() != 0 || itsc.getExposeTime() != 0) {
                    statusU = 48;
                    iconU = 12;
                }
                //draw status color
                ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                double evasionPerc = Math.min(1, itsc.getEvade() * 1d / CombatCapability.EVADE_CHARGE);
                //draw status icon
                ms.blit(newdark, atX - iconW / 2, atY - iconH / 2, iconU, iconV, iconW, iconH);
                //draw evasion
                statusU = 72;
                ms.blit(newdark, atX - insigniaWH / 2, atY + insigniaWH / 2 - (int) (insigniaWH * evasionPerc), statusU, statusV + insigniaWH - (int) (insigniaWH * evasionPerc), insigniaWH, (int) (insigniaWH * evasionPerc));
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
                        ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        snewDarkAnimFrames--;
                    } else if (snewDarkAnimFrames < 0) {
                        statusU = (7 + (snewDarkAnimFrames / 4)) * 24;
                        statusV = 131;
                        ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
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
                        ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        lnewDarkAnimFrames--;
                    } else if (lnewDarkAnimFrames < 0) {
                        statusU = (7 + (lnewDarkAnimFrames / 4)) * 24;
                        statusV = 131;
                        ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        lnewDarkAnimFrames++;
                    }
                }
            }
        }
        mc.getProfiler().pop();
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
        GuiComponent.blit(poseStack, stealth, -16, -8, 0, shift * 16, 32, 16, 64, 64);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        final Minecraft mc = Minecraft.getInstance();
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
            PoseStack stack=graphics.pose();
            if (cap.isCombatMode()) {
                {
                    stack.pushPose();
                    RenderSystem.enableBlend();
                    //RenderSystem.enableAlphaTest();
                    Pair<Integer, Integer> pair = RenderUtils.translateCoords(ClientConfig.CONFIG.mightBar, width, height);
                    int x = Math.max(pair.getFirst(), 0);
                    int y = Math.min(pair.getSecond(), height - 5);
                    int fillHeight = (int) (currentMightLevel * 32 / cap.getMaxMight());
                    if (ClientConfig.CONFIG.mightBar.enabled) {
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        drawMightBar(stack, x, y, currentMightLevel, currentMightLevel == cap.getMaxMight());
                        RenderSystem.setShaderTexture(0, raihud);
                    }

                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.mightCircle, width, height);
                    x = Mth.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = Mth.clamp(pair.getSecond() - 16, 0, height - 32);

                    if (ClientConfig.CONFIG.mightCircle.enabled) {
                        RenderSystem.setShaderTexture(0, raihud);
                        //might circle
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                        stack.pushPose();
                        {
                            stack.pushPose();
                            graphics.blit(raihud, x, y, 0, 64, 32, 32);
                            stack.popPose();
                        }
                        //might circle filling
                        {
                            stack.pushPose();
                            graphics.blit(raihud, x, y + 32 - fillHeight, 32, 96 - fillHeight, 32, fillHeight);
                            stack.popPose();
                        }
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //might crown plus pro ultra, rendered at max might
                        if (currentMightLevel == cap.getMaxMight()) {
                            {
                                stack.pushPose();
                                graphics.blit(raihud, x, y, 64, 64, 32, 32);
                                stack.popPose();
                            }
                        }
                        stack.popPose();
                    }


                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.spiritBar, width, height);
                    x = Mth.clamp(pair.getFirst(), 0, width - 32);
                    y = Mth.clamp(pair.getSecond(), 0, height - 5);
                    fillHeight = (int) (Math.min(1, currentSpiritLevel / cap.getMaxSpirit()) * 32);
                    String display = RenderUtils.formatter.format(currentSpiritLevel) + "/" + RenderUtils.formatter.format(cap.getMaxSpirit());
                    //spirit bar
                    stack.pushPose();
                    if (ClientConfig.CONFIG.spiritBar.enabled) {
                        //here be the discrete spirit bar
                        drawSpiritBar(stack, x, y, currentSpiritLevel, cap.getMaxSpirit());
                        RenderSystem.setShaderTexture(0, raihud);
                    }

                    //spirit circle
                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.spiritCircle, width, height);
                    x = Mth.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = Mth.clamp(pair.getSecond() - 16, 0, height - 32);
                    if (ClientConfig.CONFIG.spiritCircle.enabled) {
                        RenderSystem.setShaderTexture(0, raihud);
                        {
                            stack.pushPose();
                            graphics.blit(raihud, x, y, 0, 96, 32, 32);
                            stack.popPose();
                        }
                        //spirit circle filling
                        {
                            stack.pushPose();
                            graphics.blit(raihud, x, y + 32 - fillHeight, 0, 128 - fillHeight, 32, fillHeight);
                            stack.popPose();
                        }
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //spirit base
                        {
                            stack.pushPose();
                            graphics.blit(raihud, x, y + 1, 32, 96, 32, 32);
                            stack.popPose();
                        }
                        //spirit illumination
                        {
                            stack.pushPose();
                            graphics.blit(raihud, x, y + 33 - fillHeight, 64, 128 - fillHeight, 32, fillHeight);
                            stack.popPose();
                        }
                    }
                    //numbers
                    {
                        if (ClientConfig.CONFIG.spiritNumber.enabled) {
                            pair = RenderUtils.translateCoords(ClientConfig.CONFIG.spiritNumber, width, height);
                            graphics.drawString(gui.getFont(), display, pair.getFirst() - mc.font.width(display) / 2, pair.getSecond() - 2, ClientConfig.spiritColor);
                        }
                        if (ClientConfig.CONFIG.mightNumber.enabled) {
                            pair = RenderUtils.translateCoords(ClientConfig.CONFIG.mightNumber, width, height);
                            display = RenderUtils.formatter.format(currentMightLevel) + "/" + RenderUtils.formatter.format(cap.getMaxMight());
                            graphics.drawString(gui.getFont(), display, pair.getFirst() - mc.font.width(display) / 2, pair.getSecond() - 2, ClientConfig.mightColor);
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
                        graphics.blit(raihud, x, y, comboU, 0, combowidth, 32);
                        //fancy fill percentage
                        graphics.blit(raihud, x, y + 33 - fillHeight, comboU, 65 - fillHeight, combowidth, fillHeight - 2);
                    }

                    stack.popPose();
                }
                RenderSystem.disableBlend();
            }
            RenderSystem.setShaderTexture(0, amo);
            //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
            if (cap.isCombatMode() || cap.getPosture() < cap.getMaxPosture() || cap.getStunTime() > 0)
                drawPostureBarAt(true, graphics, player, width, height);


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
                        graphics.blit(stealth, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, 32, 16, 64, 64);
                    }
                }
                final ICombatCapability loocap = CombatData.getCap((LivingEntity) look);
                if (ClientConfig.CONFIG.enemyPosture.enabled && (cap.isCombatMode() || loocap.getPosture() < loocap.getMaxPosture() || loocap.isVulnerable()))
                    drawPostureBarAt(false, graphics, looked, width, height);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
            }
        }
    }

    private void drawMightBarInternal(PoseStack stack, int x, int y, int index, int to, int from) {
        index %= 7;
        GuiComponent.blit(stack, might, x, y, -90, 0, Math.max(0, index * 5 * 2 - 5), to, 5, 256, 256);
        if (from != 0) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiComponent.blit(stack, might, x, y, -90, 0, index * 5 * 2 + 5, to, 5, 256, 256);
            RenderSystem.disableBlend();
        }

    }

    private void drawSpiritBar(PoseStack stack, int x, int y, float prog, float max) {
        final int length = 92;//you only have this many pixels
        int perBar = (int) (length / max) - 2;
        //draw from the center outwards
        //draw one side of the bar
        int firstbar = perBar;
        for (int i = 0; i < max; i++) {
            int workingPerBar = perBar;
            if (i == 0 && (max - (int) max) != 0) {
                workingPerBar = (int) ((max - (int) max) * perBar);
                firstbar = workingPerBar;
            }
            GuiComponent.blit(stack, might, x + (workingPerBar + 2) * (i - 1) + firstbar, y, -90, 0, 40, workingPerBar - 2, 5, 256, 256);
            //draw cap
            GuiComponent.blit(stack, might, x + (i) * (workingPerBar + 2) + firstbar - 7, y, -90, 85, 40, 6, 5, 256, 256);

            int remainder = (int) ((prog - (int) prog) * workingPerBar);
            if (prog == max - i || i > max - prog)//filling up
                remainder = workingPerBar;
            if (max - i > Mth.ceil(prog))//not there yet
                remainder = 0;
            if (remainder > 0) {
                GuiComponent.blit(stack, might, x + (i) * (workingPerBar + 2) + firstbar - remainder, y, -90, 92 - remainder - 2, 45, remainder - 2, 5, 256, 256);
                //this.drawBar(stack, x + (i) * perBar+remainder-2, y, 4, 92, 92-remainder-2);
            }
        }
    }

    private void drawMightBar(PoseStack stack, int x, int y, float prog, boolean maxed) {
        final int length = 92;
        int index = (int) prog;
        this.drawMightBarInternal(stack, x, y, index, length - 1, 0);
        int i = (int) ((prog - (int) prog) * length);
        if (prog == index && prog != 0)//maxed
            i = length;
        if (i > 0) {
            this.drawMightBarInternal(stack, x, y, index, i, 5);
        }
        if (maxed) {
            //gold covering
            GuiComponent.blit(stack, might, x, y, -90, 0, 70, length, 5, 256, 256);
            //gold cap
            if (prog != (int) prog)
                GuiComponent.blit(stack, might, x + i - 1, y, -90, 88, 70, 4, 5, 256, 256);
        }

    }
}
