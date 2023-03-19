package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;

public class ComboBreaker extends Judgment {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof SkillCastEvent && hasMark(target) && target == ((SkillCastEvent) procPoint).getTarget() && procPoint.getPhase() == EventPriority.HIGHEST) {
            mark(caster, target, 5, 1);
        }
        else super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
    }

    private void detonate(LivingEntity caster, LivingEntity target, SkillData sd) {
        float amount = (float) (sd.getArbitraryFloat() * 0.04 * target.getHealth());
        target.hurt(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).bypassArmor().bypassMagic(), amount);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 10);
            return true;
        }
        return super.onStateChange(caster, prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            if (existing.getDuration() < 0) return null;
            sd.addArbitraryFloat(existing.getArbitraryFloat());
            if (sd.getArbitraryFloat() > 10) {
                removeMark(target);
            }
        }
        sd.setDuration(5);
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        detonate(caster, target, sd);
    }
}
