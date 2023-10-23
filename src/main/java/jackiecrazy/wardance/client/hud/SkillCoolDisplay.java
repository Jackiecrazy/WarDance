package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SkillCoolDisplay implements IGuiOverlay {
    private static void drawSkills(GuiGraphics graphics, Minecraft mc, List<SkillData> skill, int x, int y) {
        PoseStack stack=graphics.pose();
        stack.pushPose();
        for (int index = 0; index < skill.size(); index++) {

            stack.pushPose();
            //skill icon
            SkillData s = skill.get(index);
            RenderSystem.setShaderTexture(0, s.getSkill().icon());
            Color c = s.getSkill().getColor();
            RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            final int centerX = x - (skill.size() - 1 - index) * 18 + (skill.size() - 1) * 8;
            graphics.blit(s.getSkill().icon(), centerX - 8, y, 0, 0, 16, 16, 16, 16);
//            if (s.getState() == Skill.STATE.ACTIVE && s.getMaxDuration() >= 0) {
//                String display = formatter.format(Math.round(s.getDuration()));
//                mc.font.drawShadow(stack, display, centerX - 8, y - 2, 0xffffff);
//            }
            if (s.getArbitraryFloat() != 0) {
                RenderUtils.formatter.setMinimumFractionDigits(0);
                RenderUtils.formatter.setMaximumFractionDigits(1);
                String display = RenderUtils.formatter.format(s.getArbitraryFloat());
                graphics.drawString(mc.font, display, centerX + 8 - mc.font.width(display) / 2, y + 8, 0xffffff);
            }
            stack.popPose();

            if (s.getMaxDuration() == 0) continue;
            //dark mask
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
            if (s.getState() == Skill.STATE.ACTIVE)//inverted
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            graphics.blit(s.getSkill().icon(), centerX - 8, y, 0, 0, 16, 16, 16, 16);
            if (s.getMaxDuration() >= 0) {
                //cooldown/active spinny
                float cd = s.getDuration();
                float cdPerc = cd / s.getMaxDuration();
                if (s.getState() == Skill.STATE.ACTIVE)
                    cdPerc = (s.getMaxDuration() - cd) / s.getMaxDuration();
                RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                RenderUtils.drawCooldownCircle(stack, centerX - 8, y, 16, cdPerc, s.getState()== Skill.STATE.ACTIVE);
                RenderSystem.disableBlend();

                //cooldown number
                String num = String.valueOf((int) cd);
                if (Math.ceil(cd) != cd)
                    num = RenderUtils.formatter.format(cd);
                stack.pushPose();
                RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                graphics.drawString(mc.font, num, centerX - 8 - mc.font.width(num) / 2, y, 0xFFFFFF);
                stack.popPose();
            }
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        stack.popPose();
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (!CombatData.getCap(player).isCombatMode()) return;
        PoseStack stack=guiGraphics.pose();
        stack.pushPose();
//render skill cooldown
        if (ClientConfig.CONFIG.skillCD.enabled) {
            stack.pushPose();
            List<SkillData> skill = new ArrayList<>();
            //actives
            final ISkillCapability cap = CasterData.getCap(player);
            skill.addAll(cap.getAllSkillData().values().stream().filter(a -> a != null && cap.isSkillEquipped(a.getSkill()) && !a.getSkill().isPassive(player) && a.getDuration() >= 0 && (a.getState() == Skill.STATE.COOLING || a.getState() == Skill.STATE.ACTIVE || a.getSkill().displaysInactive(player, a))).toList());
            Pair<Integer, Integer> pair = RenderUtils.translateCoords(ClientConfig.CONFIG.skillCD, width, height);
            drawSkills(guiGraphics, mc, skill, pair.getFirst(), pair.getSecond());
            skill.clear();
            skill.addAll(cap.getAllSkillData().values().stream().filter(a -> a != null && cap.isSkillEquipped(a.getSkill()) && a.getSkill().isPassive(player) && a.getDuration() >= 0 && (a.getState() == Skill.STATE.COOLING || a.getState() == Skill.STATE.ACTIVE || a.getSkill().displaysInactive(player, a))).toList());
            drawSkills(guiGraphics, mc, skill, pair.getFirst(), pair.getSecond() + 18);
            stack.popPose();
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        stack.popPose();
    }
}
