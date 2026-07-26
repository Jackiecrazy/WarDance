package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;

public class Mikiri extends IronGuard {

    @Override
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof MeleePostureEvent.Defense.Block b && procPoint.getPhase() == EventPriority.HIGHEST && ((MeleePostureEvent.Defense) procPoint).getEntity() == caster && ((MeleePostureEvent.Defense) procPoint).success() && state == STATE.COOLING) {
            parry(caster, b, stats, target, state);
        }
        if (procPoint instanceof LivingAttackEvent && caster.getLastHurtMobTimestamp() != caster.tickCount && ((LivingAttackEvent) procPoint).getEntity() == target && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
            stats.decrementDuration();
        }
    }

    @Override
    protected void parry(LivingEntity caster,
                         MeleePostureEvent.Defense procPoint,
                         SkillData stats,
                         LivingEntity target,
                         STATE state) {
        if (state == STATE.COOLING) {
            if (procPoint.success())
                stats.decrementDuration();
        } else {
            procPoint.setPostureConsumption(0);
            procPoint.setResult(Event.Result.ALLOW);
            markUsed(caster);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 7 / SkillUtils.getSkillEffectiveness(caster));
        return passive(prev, from, to);
    }
}
