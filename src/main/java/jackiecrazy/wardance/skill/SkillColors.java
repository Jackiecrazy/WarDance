package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.utils.WarColors;

import java.awt.*;

public class SkillColors {
    public static final SkillCategory
            red = new SkillCategory("dominance",Color.RED),
    green = new SkillCategory("resolution",WarColors.DARK_GREEN),
    gray = new SkillCategory("subterfuge", Color.LIGHT_GRAY),
    orange = new SkillCategory("fervor", Color.ORANGE),
    cyan = new SkillCategory("perception", Color.CYAN),
    purple = new SkillCategory("decay", WarColors.VIOLET),
    white = new SkillCategory("general", Color.WHITE),
    none = new SkillCategory("none", Color.WHITE);
}
