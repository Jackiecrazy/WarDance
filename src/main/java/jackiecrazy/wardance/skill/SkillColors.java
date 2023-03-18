package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.ChatFormatting;

import java.awt.*;

public class SkillColors {
    public static final SkillCategory
            red = new SkillCategory("dominance",Color.RED, ChatFormatting.BOLD),
    green = new SkillCategory("resolution",WarColors.DARK_GREEN, ChatFormatting.BOLD),
    gray = new SkillCategory("subterfuge", Color.LIGHT_GRAY, ChatFormatting.BOLD),
    orange = new SkillCategory("fervor", Color.ORANGE, ChatFormatting.BOLD),
    cyan = new SkillCategory("perception", Color.CYAN, ChatFormatting.BOLD),
    purple = new SkillCategory("decay", WarColors.VIOLET),
    white = new SkillCategory("general", Color.WHITE),
    none = new SkillCategory("none", Color.WHITE);
}
