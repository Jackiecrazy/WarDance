package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.ChatFormatting;

import java.awt.*;

public class SkillColors {
    public static final SkillCategory
            red = new SkillCategory("dominance", Color.RED, ChatFormatting.BOLD),
            green = new SkillCategory("resolution", WarColors.DARK_GREEN, ChatFormatting.BOLD),
            gray = new SkillCategory("subterfuge", Color.LIGHT_GRAY, ChatFormatting.BOLD),
            azure = new SkillCategory("fervor", WarColors.AZURE, ChatFormatting.BOLD),
            gold = new SkillCategory("generosity_avarice", WarColors.GOLD, ChatFormatting.BOLD),
            cyan = new SkillCategory("perception", Color.CYAN, ChatFormatting.BOLD),
            purple = new SkillCategory("decay", WarColors.VIOLET, ChatFormatting.BOLD),
            white = new SkillCategory("general", Color.WHITE, ChatFormatting.BOLD),
            none = new SkillCategory("none", Color.WHITE);
}
