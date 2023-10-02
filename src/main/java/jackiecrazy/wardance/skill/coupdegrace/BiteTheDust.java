package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class BiteTheDust extends CoupDeGrace {
    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.coup_de_grace;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 0;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof StunEvent && ((StunEvent) procPoint).getAttacker() == caster && procPoint.getPhase() == EventPriority.HIGHEST) {
            float damage = 1;
            float currentMark = getExistingMark(target).getDuration();
            while (currentMark > 0) {
                damage *= 2;
                currentMark--;
            }
            damage *= stats.getEffectiveness();
            target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
            target.hurt(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setProcSkillEffects(true).setSkillUsed(this).setKnockbackPercentage(0).bypassArmor().bypassMagic(), damage);
            mark(caster, target, 1);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

    @Override
    public boolean willKillOnCast(LivingEntity caster, LivingEntity target, SkillData stats) {
        float damage = 2;
        float currentMark = getExistingMark(target).getDuration();
        while (currentMark > 0) {
            damage *= 2;
            currentMark--;
        }
        damage *= stats.getEffectiveness();
        return damage > target.getHealth();
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }
}
