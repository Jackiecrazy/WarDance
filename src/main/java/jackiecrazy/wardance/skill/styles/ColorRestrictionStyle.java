package jackiecrazy.wardance.skill.styles;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ColorRestrictionStyle extends SkillStyle {
    private SkillCategory[] reqs = {};
    private List<SkillCategory> r = new ArrayList<>();
    private SkillCategory[] bans = {};

    public ColorRestrictionStyle(int max, boolean ban, SkillCategory... cats) {
        super(max - (ban ? 0 : cats.length));
        if (ban)
            bans = cats;
        else {
            reqs = cats;
            r = List.of(reqs);
        }
    }

    public int getMaxColorsForSorting() {
        return this.getMaxColors() + reqs.length + bans.length;
    }

    @Override
    public boolean isEquippableWith(Skill s, List<Skill> existing) {
        if (s == null) return true;
        if (Arrays.stream(bans).anyMatch(a -> a == s.getCategory()))
            return false;
        if (Arrays.stream(reqs).anyMatch(a -> a == s.getCategory()))
            return true;
        return super.isEquippableWith(s, existing.stream().filter(a -> a == null || Arrays.stream(reqs).anyMatch(b -> a.getCategory() != b)).toList());
    }
}
