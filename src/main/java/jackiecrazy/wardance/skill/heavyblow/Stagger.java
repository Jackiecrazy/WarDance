package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class Stagger extends HeavyBlow {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof final MeleePostureEvent.Defense def && state != STATE.COOLING && stats.isCondition() && procPoint.getPhase() == EventPriority.LOWEST && def.getAttacker() == caster && def.getEntity()!=caster) {
            CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, 60);
            CombatData.getCap(target).setHandBind(InteractionHand.OFF_HAND, 60);
            def.setPostureConsumption(def.getPostureConsumption() * stats.getArbitraryFloat() * stats.getArbitraryFloat());
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent point) {
            if (isCrit(point) && (state == STATE.ACTIVE || cast(caster, 1)) && procPoint.getPhase() == EventPriority.LOWEST) {
                onCrit(point, stats, caster, target);
            } else if (state == STATE.COOLING && procPoint.getPhase() == EventPriority.HIGHEST) {
                stats.decrementDuration();
            }
        }
    }

    @Override
    protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {
        stats.setArbitraryFloat(proc.getDamageModifier());
        proc.setDamageModifier(1);
        EffectUtils.stackPot(target, new MobEffectInstance(FootworkEffects.WOUND.get(), 200, Math.round(7*stats.getEffectiveness())), EffectUtils.StackingMethod.MAX_DURATION);
        stats.flagCondition(true);
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }
}
