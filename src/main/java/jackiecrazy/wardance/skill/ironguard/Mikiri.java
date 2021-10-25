package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Mikiri extends IronGuard {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "onParry", "passive", ProcPoints.recharge_parry)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 6);
    }

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }
}
