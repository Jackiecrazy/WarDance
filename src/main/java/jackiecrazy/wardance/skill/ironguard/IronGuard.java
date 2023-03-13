package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.SetTag;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jackiecrazy.wardance.skill.Skill.STATE;

public abstract class IronGuard extends Skill {
    @Override
    public SetTag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public SetTag<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.iron_guard;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && state!=STATE.COOLING && ((ParryEvent) procPoint).getEntityLiving() == caster && ((ParryEvent) procPoint).canParry() && ((ParryEvent) procPoint).getPostureConsumption() > 0 && cast(caster, target, -999)) {
            parry(caster, (ParryEvent) procPoint, stats, target, state);
        }
    }

    protected abstract void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state);

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 4);
        return passive(prev, from, to);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return cooldownTick(stats);
    }
}
