package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.utils.WarColors;

import java.awt.*;

public class SkillColors {
    public static final SkillCategory
    dominance = new SkillCategory("dominance",Color.RED),
    resolution = new SkillCategory("resolution",WarColors.DARK_GREEN),
    subterfuge = new SkillCategory("subterfuge", Color.LIGHT_GRAY),
    fervor = new SkillCategory("fervor", Color.ORANGE),
    perception = new SkillCategory("perception", Color.CYAN),
    decay = new SkillCategory("decay", WarColors.VIOLET),
    none = new SkillCategory("general", Color.WHITE);
}
