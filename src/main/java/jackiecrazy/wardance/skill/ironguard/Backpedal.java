package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;

public class Backpedal extends IronGuard {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof MeleePostureEvent.Defense && procPoint.getPhase() == EventPriority.HIGHEST && state!=STATE.COOLING && ((MeleePostureEvent.Defense) procPoint).getEntity() == caster && ((MeleePostureEvent.Defense) procPoint).success() && ((MeleePostureEvent.Defense) procPoint).getPostureConsumption() > 0) {
            parry(caster, (MeleePostureEvent.Defense) procPoint, stats, target, state);
        }
    }
    @Override
    protected void parry(LivingEntity caster, MeleePostureEvent.Defense procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (!caster.onGround() && state != STATE.COOLING && cast(caster, target, -999)) {
            float str = -Math.min(procPoint.getPostureConsumption() / 2, 2) * stats.getEffectiveness();
            caster.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).normalize().scale(str)));
            caster.hurtMarked = true;
            procPoint.setPostureConsumption(0);
            markUsed(caster);
        }
    }
}
