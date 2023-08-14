package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
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
                GuiComponent.blit(stack, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);
                if (s.getMaxDuration() >= 1) {
                    String display = formatter.format(Math.round(s.getDuration()));
                    mc.font.drawShadow(stack, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond() - 2, 0xffffff);
                }
                if (s.getArbitraryFloat() != 0) {
                    String display = formatter.format(s.getArbitraryFloat());
                    mc.font.drawShadow(stack, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 + 4, pair.getSecond() + 8, 0xffffff);
                }

            }
        }
        Entity look = RenderUtils.getEntityLookedAt(player, 32);
        if (look instanceof LivingEntity) {
            LivingEntity looked = (LivingEntity) look;
            List<SkillData> afflict = new ArrayList<>();
            final ISkillCapability skill = CasterData.getCap(player);
            if (ClientConfig.CONFIG.enemyAfflict.enabled) {
                //coup de grace
                for (Skill variant : skill.getEquippedVariations(SkillArchetypes.coup_de_grace))
                    if (look != player && variant instanceof CoupDeGrace cdg && skill.isSkillUsable(variant)) {
                        if (cdg.willKillOnCast(player, looked, skill.getSkillData(variant).orElse(SkillData.DUMMY))) {
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
                    if (s.getMaxDuration() >= 1) {
                        String display = formatter.format(Math.round(s.getDuration()));
                        mc.font.drawShadow(stack, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond() - 2, 0xffffff);
                    }
                    if (s.getArbitraryFloat() != 0) {
                        String display = formatter.format(s.getArbitraryFloat());
                        mc.font.drawShadow(stack, display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 + 4, pair.getSecond() + 8, 0xffffff);
                    }

                }
            }
        }
    }

    private void drawShadow() {

    }
}
