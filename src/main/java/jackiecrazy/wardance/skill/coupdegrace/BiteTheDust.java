package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;

public class BiteTheDust extends Skill {
    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.coup_de_grace;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof StaggerEvent && ((StaggerEvent) procPoint).getAttacker() == caster && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE) {
            float damage = 2;
            float currentMark = getExistingMark(target).getDuration();
            while (currentMark > 0) {
                damage *= 2;
                currentMark--;
            }
            target.hurt(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).setKnockbackPercentage(0).bypassArmor().bypassMagic(), damage);
            mark(caster, target, 1);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
