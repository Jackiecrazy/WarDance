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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static jackiecrazy.wardance.client.RenderUtils.formatter;
import static jackiecrazy.wardance.client.RenderUtils.translateCoords;

public class SkillCoolDisplay implements IGuiOverlay {
    private static void drawSkills(PoseStack stack, Minecraft mc, List<SkillData> skill, int x, int y) {
        for (int index = 0; index < skill.size(); index++) {
            stack.pushPose();
            stack.pushPose();
            //skill icon
            SkillData s = skill.get(index);
            RenderSystem.setShaderTexture(0, s.getSkill().icon());
            Color c = s.getSkill().getColor();
            RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            final int centerX = x - (skill.size() - 1 - index) * 18 + (skill.size() - 1) * 8;
            GuiComponent.blit(stack, centerX - 8, y, 0, 0, 16, 16, 16, 16);
//            if (s.getState() == Skill.STATE.ACTIVE && s.getMaxDuration() >= 0) {
//                String display = formatter.format(Math.round(s.getDuration()));
//                mc.font.drawShadow(stack, display, centerX - 8, y - 2, 0xffffff);
//            }
            if (s.getArbitraryFloat() != 0) {
                formatter.setMinimumFractionDigits(0);
                formatter.setMaximumFractionDigits(1);
                String display = formatter.format(s.getArbitraryFloat());
                mc.font.drawShadow(stack, display, centerX + 8-mc.font.width(display)/2, y + 8, 0xffffff);
            }
            stack.popPose();

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
            if (s.getMaxDuration() == 0) continue;
            //dark mask
            if (s.getState() == Skill.STATE.ACTIVE)//inverted
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            GuiComponent.blit(stack, centerX - 8, y, 0, 0, 16, 16, 16, 16);
            if (s.getMaxDuration() >= 0) {
                //cooldown/active spinny
                float cd = s.getDuration();
                float cdPerc = cd / s.getMaxDuration();
                if (s.getState() == Skill.STATE.ACTIVE)
                    cdPerc = (s.getMaxDuration() - cd) / s.getMaxDuration();
                RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                RenderUtils.drawCooldownCircle(stack, centerX - 8, y, 16, cdPerc);
                RenderSystem.disableBlend();

                //cooldown number
                String num = String.valueOf((int) cd);
                if (Math.ceil(cd) != cd)
                    num = formatter.format(cd);
                stack.pushPose();
                RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                //AbstractGui.blit(matrixStack, x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                mc.font.draw(stack, num, centerX - 8 - mc.font.width(num) / 2f, y, 0xFFFFFF);
                stack.popPose();
            }
            stack.popPose();
        }
    }

    @Override
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (!CombatData.getCap(player).isCombatMode()) return;
//render skill cooldown
        if (ClientConfig.CONFIG.skillCD.enabled) {
            List<SkillData> skill = new ArrayList<>();
            //actives
            final ISkillCapability cap = CasterData.getCap(player);
            skill.addAll(cap.getAllSkillData().values().stream().filter(a -> a != null && cap.isSkillEquipped(a.getSkill()) && !a.getSkill().isPassive(player) && a.getDuration() >= 0 && (a.getState() == Skill.STATE.COOLING || a.getState() == Skill.STATE.ACTIVE || a.getSkill().displaysInactive(player, a))).toList());
            Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.skillCD, width, height);
            drawSkills(stack, mc, skill, pair.getFirst(), pair.getSecond());
            skill.clear();
            skill.addAll(cap.getAllSkillData().values().stream().filter(a -> a != null && cap.isSkillEquipped(a.getSkill()) && a.getSkill().isPassive(player) && a.getDuration() >= 0 && (a.getState() == Skill.STATE.COOLING || a.getState() == Skill.STATE.ACTIVE || a.getSkill().displaysInactive(player, a))).toList());
            drawSkills(stack, mc, skill, pair.getFirst(), pair.getSecond() + 18);
        }
    }
}
