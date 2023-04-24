package jackiecrazy.wardance.skill.styles;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;

public abstract class ColorRestrictionStyle extends SkillStyle {
    private SkillCategory[] reqs = {};
    private SkillCategory[] bans = {};

    public ColorRestrictionStyle(int max, boolean ban, SkillCategory... cats) {
        super(max - (ban ? 0 : cats.length));
        if (ban)
            bans = cats;
        else reqs = cats;
    }

    @Override
    public boolean isEquippableWith(Skill s, LivingEntity caster) {
        if (s == null) return true;
        if (Arrays.stream(bans).anyMatch(a -> a == s.getCategory()))
            return false;
        if (Arrays.stream(reqs).anyMatch(a -> a == s.getCategory()))
            return true;
        return super.isEquippableWith(s, caster);
    }

    public int getMaxColorsForSorting() {
        return this.getMaxColors() + reqs.length + bans.length;
    }
}
