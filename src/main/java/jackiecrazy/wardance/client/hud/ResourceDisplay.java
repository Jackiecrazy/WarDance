package jackiecrazy.wardance.client.hud;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.IStyleCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.client.GuiComponent;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

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
    private static float currentAdrenaline = 0;
    private static float currentSpiritLevel = 0;
    private static float scurrentEvasion = 0, lcurrentEvasion = 0;
    private static boolean flip = false;
    private static int snewDarkAnimFrames = 0, lnewDarkAnimFrames = 0, spiritFrames = 0;

    private static void drawPostureBarAt(boolean you, GuiGraphics ms, LivingEntity elb, int width, int height) {
        drawNewDarkPostureBarAt(you, ms, elb, width, height);
        RenderSystem.defaultBlendFunc();
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
        //so we want the full size to be 240, 72 to be 125, and every 20 max posture changes this by 20 pixels
        //divvy by 2 for two-pronged approach
        int halfBarWidth = Math.min(240, (int) (Math.sqrt(itsc.getMaxPosture()) * 14.73)) / 2;
        int flexBarWidth = halfBarWidth + 3;
        final int barHeight = 12;
        //double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            //draw working bracket
            final int barY = atY - barHeight / 2;
            ms.blit(newdark, atX, barY, 243 - flexBarWidth, 0, flexBarWidth, barHeight);
            ms.blit(newdark, atX - flexBarWidth, barY, 0, 0, flexBarWidth, barHeight);
            //grayscale if staggered
            if (itsc.getStunTime() > 0) {
                //draw two bars, one for stun time and one for posture. The shorter one is drawn in front.
                int stunBarWidth = (int) ((itsc.getStunTime()) * flexBarWidth / (float) itsc.getMaxStunTime()) + 3;
                flexBarWidth = (int) ((itsc.getMaxPosture() - itsc.getPosture()) * halfBarWidth / itsc.getMaxPosture()) + 3;
                if (stunBarWidth > flexBarWidth) {
                    //stun bar is longer, draw it first
                    ms.blit(newdark, atX, barY, 243 - stunBarWidth, 24, stunBarWidth, barHeight);
                    ms.blit(newdark, atX - stunBarWidth, barY, 0, 24, stunBarWidth, barHeight);
                    //then layer posture bar
                    ms.blit(newdark, atX, barY, 243 - flexBarWidth, 12, flexBarWidth, barHeight);
                    ms.blit(newdark, atX - flexBarWidth, barY, 0, 12, flexBarWidth, barHeight);
                } else {
                    //posture bar is longer, draw it first
                    ms.blit(newdark, atX, barY, 243 - flexBarWidth, 12, flexBarWidth, barHeight);
                    ms.blit(newdark, atX - flexBarWidth, barY, 0, 12, flexBarWidth, barHeight);
                    //then layer stun bar
                    ms.blit(newdark, atX, barY, 243 - stunBarWidth, 24, stunBarWidth, barHeight);
                    ms.blit(newdark, atX - stunBarWidth, barY, 0, 24, stunBarWidth, barHeight);
                }
            } else {
                //otherwise draw posture and rally
                //draw the rally bar first
                int rally = (int) (Math.min(itsc.getMaxPosture(), itsc.getMaxPosture() - itsc.getPosture()) * halfBarWidth / itsc.getMaxPosture()) + 3;
                ms.blit(newdark, atX, barY, 243 - rally, 12, rally, barHeight);
                ms.blit(newdark, atX - rally, barY, 0, 12, rally, barHeight);

                //then layer the gray portion on
                flexBarWidth = (int) ((itsc.getMaxPosture() - itsc.getPosture() - itsc.getRally()) * halfBarWidth / itsc.getMaxPosture()) + 3;
                ms.blit(newdark, atX, barY, 243 - flexBarWidth, 24, flexBarWidth, barHeight);
                ms.blit(newdark, atX - flexBarWidth, barY, 0, 24, flexBarWidth, barHeight);
            }
            // render steve time frames
//            if (itsc.isIframe()) {
//                float otemp = (float) Math.min(1, itsc.getIframe() / 40f);
//                int fini = (int) (otemp * halfBarWidth);
//                int shatterV = 48;
//                //gold that stretches out to the edges before disappearing
//                ms.blit(newdark, atX + 5, atY - barHeight / 2, 243 - fini, shatterV, fini, barHeight);
//                ms.blit(newdark, atX - fini - 5, atY - barHeight / 2, 0, shatterV, fini, barHeight);
//                RenderSystem.setShaderColor(1, 1, 1, 1);
//            }
            if (itsc.getRecordedDamage() > 0) {
                //internal damage as a percentage of max health
                float otemp = Mth.clamp(itsc.getRecordedDamage() / (elb.getMaxHealth()),0,1);
                int fini = (int) (otemp * halfBarWidth);
                int shatterV = Math.min(36 + (int) (otemp * 2.8) * 12, 60);
                //gold that stretches out to the edges before disappearing
                ms.blit(newdark, atX + 5, atY - barHeight / 2, 243 - fini, shatterV, fini, barHeight);
                ms.blit(newdark, atX - fini - 5, atY - barHeight / 2, 0, shatterV, fini, barHeight);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            //render insignia
            if(!itsc.canBlock()||elb.isBlocking()||itsc.isParrying()||itsc.isIframe()||itsc.isStunned()){
                int insigniaWH = 24;
                int iconW = 12, iconH = 10;
                //normal, use green
                int statusU = 0, statusV = 83;
                //default icon location, it's a shield
                int iconU = -1, iconV = 72;
                //block breach override
                if (!itsc.canBlock())
                    iconU = 12;
                else if (elb.isBlocking() || itsc.isParrying()) iconU = 0;
                //danger, use red status square
                if (itsc.isStunned()) {
                    statusU = 48;
                    iconU = 24;
                }
                //iframes, use yellow square
                else if (itsc.getIframe() > 0) {
                    statusU = 24;
                    iconU = 0;
                }
                //draw status color
                ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                //draw status icon
                if (iconU >= 0)
                    ms.blit(newdark, atX - iconW / 2, atY - iconH / 2, iconU, iconV, iconW, iconH);

                //draw parry CD
                double parryPerc = 1 - itsc.getParryCooldownPerc();
                statusU = 72;
                //draws a blue rectangle filling from the bottom up
                int parryCDHeight = (int) (insigniaWH * Math.min(1, parryPerc));
                ms.blit(newdark, atX - insigniaWH / 2, atY + insigniaWH / 2 - parryCDHeight, statusU, statusV + insigniaWH - parryCDHeight, insigniaWH, parryCDHeight);
                //draw evasion animation when needed
                if (you) {
                    //filled up, start animation frames
                    if (scurrentEvasion != (float) parryPerc) {
                        //refill to max, play max anim
                        if (parryPerc == 1)
                            snewDarkAnimFrames = 12;
                            //consumed, play popping anim
                        else if (parryPerc > 1) {
                            snewDarkAnimFrames = -16;
                        }
                    }
                    scurrentEvasion = (float) parryPerc;
                    statusV = 107;
                    if (snewDarkAnimFrames > 0) {
                        //drawing parry cooldown
                        statusU = (4 - (snewDarkAnimFrames / 3)) * 24;
                        ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        snewDarkAnimFrames--;
                    } else if (snewDarkAnimFrames < 0) {
                        //drawing parry popping
                        statusU = (8 + (snewDarkAnimFrames / 2)) * 24;
                        statusV = 131;
                        ms.blit(newdark, atX - insigniaWH / 2, atY - insigniaWH / 2, statusU, statusV, insigniaWH, insigniaWH);
                        snewDarkAnimFrames++;
                    }
                } else {
                    //filled up, start animation frames
                    if (lcurrentEvasion != (float) parryPerc && (parryPerc == 1 || parryPerc < lcurrentEvasion)) {
                        lnewDarkAnimFrames = (int) (56 * (parryPerc - 0.5));
                    }
                    lcurrentEvasion = (float) parryPerc;
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

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() instanceof Player) {
            LocalPlayer player = mc.player;
            ICombatCapability cap = CombatData.getCap(player);
            IStyleCapability style = StylishData.getCap(player);
            RenderSystem.setShaderTexture(0, raihud);
            float prev = currentSpiritLevel;
            currentSpiritLevel = updateValue(currentSpiritLevel, cap.getSpirit());
            if ((int) prev < (int) currentSpiritLevel)//advance up 1
                spiritFrames = 10;
            currentAdrenaline = updateValue(currentAdrenaline, style.getAdrenaline());
            //yourCurrentPostureLevel = updateValue(yourCurrentPostureLevel, cap.getPosture());
            PoseStack stack = graphics.pose();
            if (style.isCombatMode()) {
                stack.pushPose();
                RenderSystem.enableBlend();
                //RenderSystem.enableAlphaTest();
                Pair<Integer, Integer> pair = RenderUtils.translateCoords(ClientConfig.CONFIG.adrenalineBar, width, height);
                int x = Math.max(pair.getFirst(), 0);
                int y = Math.min(pair.getSecond(), height - 5);
                int fillHeight = (int) (currentAdrenaline * 32);
                //adrenaline bar
                if (ClientConfig.CONFIG.adrenalineBar.enabled) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    drawAdrenalineBar(stack, x, y, currentAdrenaline, currentAdrenaline == 1);
                    RenderSystem.setShaderTexture(0, raihud);
                }


                if (ClientConfig.CONFIG.adrenalineCircle.enabled) {
                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.adrenalineCircle, width, height);
                    x = Mth.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = Mth.clamp(pair.getSecond() - 16, 0, height - 32);
                    RenderSystem.setShaderTexture(0, raihud);
                    //adrenaline circle
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    stack.pushPose();
                    {
                        stack.pushPose();
                        graphics.blit(raihud, x, y, 0, 64, 32, 32);
                        stack.popPose();
                    }
                    //adrenaline circle filling
                    {
                        stack.pushPose();
                        graphics.blit(raihud, x, y + 32 - fillHeight, 32, 96 - fillHeight, 32, fillHeight);
                        stack.popPose();
                    }
                    fillHeight += Math.min(fillHeight, 3);
                    fillHeight = Math.min(fillHeight, 32);
                    //adrenaline crown plus pro ultra, rendered at max adrenaline
                    if (currentAdrenaline == 1) {
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
                if (ClientConfig.CONFIG.spiritCircle.enabled) {
                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.spiritCircle, width, height);
                    x = Mth.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = Mth.clamp(pair.getSecond() - 16, 0, height - 32);
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
                    if (ClientConfig.CONFIG.adrenalineNumber.enabled) {
                        pair = RenderUtils.translateCoords(ClientConfig.CONFIG.adrenalineNumber, width, height);
                        display = RenderUtils.formatter.format(currentAdrenaline) + "/" + RenderUtils.formatter.format(1);
                        graphics.drawString(gui.getFont(), display, pair.getFirst() - mc.font.width(display) / 2, pair.getSecond() - 2, ClientConfig.adrenalineColor);
                    }
                    stack.popPose();
                }


                currentComboLevel = style.getCombo() - 1;
                //RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                stack.popPose();
                //combo bar at 224,20 to 229, 121. Grace at 222,95 to 224, 121
                //initial bar
                RenderSystem.enableBlend();
                stack.pushPose();
                if (ClientConfig.CONFIG.combo.enabled && currentComboLevel > 0) {
                    RenderSystem.setShaderTexture(0, raihud);
                    int combowidth = 32;
                    float workingCombo = currentComboLevel * 3.4f;
                    int comboU = (int) (Mth.clamp(Math.floor(workingCombo), 0, 4)) * 32;
                    if (workingCombo >= 5) {//SS
                        combowidth = 33;
                        comboU = 159;
                    }
                    if (workingCombo >= 7) {//SSS
                        combowidth = 64;
                        comboU = 192;
                    }
                    pair = RenderUtils.translateCoords(ClientConfig.CONFIG.combo, width, height);
                    x = Mth.clamp(pair.getFirst() - combowidth / 2, 0, width - combowidth);
                    y = Mth.clamp(pair.getSecond() - 23, 0, height - 46);
                    graphics.blit(raihud, x, y, comboU, 32, combowidth, 32);

                    //draw combo string
                    display = RenderUtils.formatter.format(style.getCombo()) + "X";
                    graphics.drawString(gui.getFont(), display, pair.getFirst() - mc.font.width(display) / 2, pair.getSecond() + 32, ClientConfig.adrenalineColor);
                }

                stack.popPose();
                RenderSystem.disableBlend();
            }
            RenderSystem.setShaderTexture(0, amo);
            //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
            if (style.isCombatMode() || cap.getPosture() < cap.getMaxPosture() || cap.isStunned())
                drawPostureBarAt(true, graphics, player, width, height);


            Entity look = RenderUtils.getEntityLookedAt(player, 32);
            if (look instanceof LivingEntity looked) {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                final ICombatCapability loocap = CombatData.getCap((LivingEntity) look);
                if (ClientConfig.CONFIG.enemyPosture.enabled && (style.isCombatMode() || loocap.getPosture() < loocap.getMaxPosture() || loocap.isStunned()))
                    drawPostureBarAt(false, graphics, looked, width, height);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
            }
        }
    }

    private void drawAdrenalineBarInternal(PoseStack stack, int x, int y, int index, int to, int from) {
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

    private void drawAdrenalineBar(PoseStack stack, int x, int y, float prog, boolean maxed) {
        final int length = 92;
        int index = (int) prog;
        this.drawAdrenalineBarInternal(stack, x, y, index, length - 1, 0);
        int i = (int) ((prog - (int) prog) * length);
        if (prog == index && prog != 0)//maxed
            i = length;
        if (i > 0) {
            this.drawAdrenalineBarInternal(stack, x, y, index, i, 5);
        }
        if (maxed) {
            //gold covering
            GuiComponent.blit(stack, might, x, y - 1, -90, 0, 70, length, 5, 256, 256);
            //gold cap
            if (prog != (int) prog)
                GuiComponent.blit(stack, might, x + i - 1, y - 1, -90, 88, 70, 4, 5, 256, 256);
        }

    }
}
