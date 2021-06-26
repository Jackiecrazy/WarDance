package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Mikiri extends IronGuard {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "onParry", "passive", SkillTags.recharge_parry)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 4);
    }

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }
}
