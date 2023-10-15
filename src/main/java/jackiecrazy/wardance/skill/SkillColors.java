package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.ChatFormatting;

import java.awt.*;

public class SkillColors {
    public static final SkillCategory
            red = new SkillCategory("dominance", Color.RED, ChatFormatting.RED),
            green = new SkillCategory("resolution", WarColors.DARK_GREEN, ChatFormatting.GREEN),
            gray = new SkillCategory("subterfuge", Color.LIGHT_GRAY, ChatFormatting.GRAY),
            azure = new SkillCategory("fervor", WarColors.AZURE, ChatFormatting.BLUE),
            gold = new SkillCategory("generosity_avarice", WarColors.GOLD, ChatFormatting.GOLD),
            cyan = new SkillCategory("perception", Color.CYAN, ChatFormatting.AQUA),
            purple = new SkillCategory("decay", WarColors.VIOLET, ChatFormatting.DARK_PURPLE),
            white = new SkillCategory("general", Color.WHITE, ChatFormatting.WHITE),
            none = new SkillCategory("none", Color.WHITE);
}
