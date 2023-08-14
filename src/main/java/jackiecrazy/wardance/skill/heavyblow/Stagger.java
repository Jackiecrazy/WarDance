package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class Stagger extends HeavyBlow {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof ParryEvent && state != STATE.COOLING && stats.isCondition() && procPoint.getPhase() == EventPriority.LOWEST && ((ParryEvent) procPoint).getAttacker() == caster && ((ParryEvent) procPoint).getEntity()!=caster&& cast(caster, target, -999)) {
            CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, 60);
            CombatData.getCap(target).setHandBind(InteractionHand.OFF_HAND, 60);
            ((ParryEvent) procPoint).setPostureConsumption(((ParryEvent) procPoint).getPostureConsumption() * stats.getArbitraryFloat() * stats.getArbitraryFloat());
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            if (isCrit((CriticalHitEvent) procPoint) && state != STATE.COOLING && procPoint.getPhase() == EventPriority.LOWEST) {
                onCrit((CriticalHitEvent) procPoint, stats, caster, target);
            } else if (state == STATE.COOLING && procPoint.getPhase() == EventPriority.HIGHEST) {
                stats.decrementDuration();
            }
        }
    }

    @Override
    protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {
        stats.setArbitraryFloat(proc.getDamageModifier());
        proc.setDamageModifier(stats.getEffectiveness());
        stats.flagCondition(true);
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }
}
