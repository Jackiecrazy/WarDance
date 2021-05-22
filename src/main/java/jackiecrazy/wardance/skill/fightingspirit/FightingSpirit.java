package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.util.Collections;
import java.util.HashSet;

public class FightingSpirit extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Collections.singletonList("passive")));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public boolean tick(LivingEntity caster, SkillData d) {
        return super.tick(caster, d);
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public boolean isPassive(LivingEntity caster) {
        return false;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        return false;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {

    }
}
