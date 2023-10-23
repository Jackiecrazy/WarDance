package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jackiecrazy.wardance.client.RenderUtils.formatter;
import static jackiecrazy.wardance.client.RenderUtils.translateCoords;

public class MarkDisplay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
//render player marks
        if (ClientConfig.CONFIG.playerAfflict.enabled) {
            List<SkillData> afflict = new ArrayList<>();
            //marks and cooldowns
            afflict.addAll(Marks.getCap(player).getActiveMarks().values().stream().filter(a -> a.getSkill().showsMark(a, player)).collect(Collectors.toList()));
            //afflict.addAll(CasterData.getCap(player).getAllSkillData().values().stream().filter(a -> a.getState() == Skill.STATE.COOLING).collect(Collectors.toSet()));
            Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.playerAfflict, width, height);

            for (int index = 0; index < afflict.size(); index++) {
                SkillData s = afflict.get(index);
                RenderSystem.setShaderTexture(0, s.getSkill().icon());
                Color c = s.getSkill().getColor();
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                guiGraphics.blit(s.getSkill().icon(), pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);
                if (s.getMaxDuration() >= 1) {
                    String display = formatter.format(Math.round(s.getDuration()));
                    guiGraphics.drawString(mc.font, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond() - 2, 0xffffff);
                }
                if (s.getArbitraryFloat() != 0) {
                    String display = formatter.format(s.getArbitraryFloat());
                    guiGraphics.drawString(mc.font, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 + 4, pair.getSecond() + 8, 0xffffff);
                }

            }
        }
        Entity look = RenderUtils.getEntityLookedAt(player, 32);
        if (look instanceof LivingEntity looked) {
            List<SkillData> afflict = new ArrayList<>();
            final ISkillCapability skill = CasterData.getCap(player);
            if (ClientConfig.CONFIG.enemyAfflict.enabled) {
                //fake marks
                for (Skill s : skill.getEquippedSkillsAndStyle())
                    if (s!=null&&look != player && s.fakeMark(mc.player, looked, skill.getSkillData(s).orElse(SkillData.DUMMY))) {
                        afflict.add(new SkillData(s, 0, 0));
                    }
                //marks
                afflict.addAll(Marks.getCap(looked).getActiveMarks().values().stream().filter(a -> a.getSkill().showsMark(a, looked)).collect(Collectors.toList()));
                Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.enemyAfflict, width, height);
                for (int index = 0; index < afflict.size(); index++) {
                    //draw icon
                    SkillData s = afflict.get(index);
                    RenderSystem.setShaderTexture(0, s.getSkill().icon());
                    Color c = s.getSkill().getColor();
                    RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                    final int atX = pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8;
                    guiGraphics.blit(s.getSkill().icon(), atX - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);

                    //dark mask
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
                    RenderSystem.setShaderColor(1,1,1, 1);
                    if (s.getState() == Skill.STATE.ACTIVE)//inverted
                        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                    guiGraphics.blit(s.getSkill().icon(), atX - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);

                    //draw spin
                    if (s.getMaxDuration() >= 1) {
                        //cooldown/active spinny
                        float cd = s.getDuration();
                        float cdPerc = cd / s.getMaxDuration();
                        if (s.getState() == Skill.STATE.ACTIVE)
                            cdPerc = (s.getMaxDuration() - cd) / s.getMaxDuration();
                        RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                        //fixme only works to the halfway point???
                        RenderUtils.drawCooldownCircle(guiGraphics.pose(), atX, pair.getSecond(), 16, cdPerc, s.getState()== Skill.STATE.ACTIVE);
                        RenderSystem.disableBlend();

                        //cooldown number
                        String num = String.valueOf((int) cd);
                        if (Math.ceil(cd) != cd)
                            num = RenderUtils.formatter.format(cd);
                        guiGraphics.pose().pushPose();
                        RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                        guiGraphics.drawString(mc.font, String.valueOf(num), atX - mc.font.width(num) / 2, pair.getSecond(), 0xFFFFFF);
                        guiGraphics.pose().popPose();
                    }
                    if (s.getArbitraryFloat() != 0) {
                        String display = formatter.format(s.getArbitraryFloat());
                        guiGraphics.drawString(mc.font, display, atX + 4, pair.getSecond() + 8, 0xffffff);
                    }

                }
            }
        }
    }

    private void drawShadow() {

    }
}
