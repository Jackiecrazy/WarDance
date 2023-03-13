package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;
import java.awt.*;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Mikiri extends IronGuard {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((ParryEvent) procPoint).getEntityLiving() == caster && ((ParryEvent) procPoint).canParry() && state == STATE.COOLING) {
            parry(caster, (ParryEvent) procPoint, stats, target, state);
        }
        if (procPoint instanceof LivingAttackEvent && caster.getLastHurtMobTimestamp() != caster.tickCount && ((LivingAttackEvent) procPoint).getEntityLiving() == target && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
            stats.decrementDuration();
        }
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (state == STATE.COOLING) {
            stats.decrementDuration();
        } else {
            procPoint.setPostureConsumption(0);
            markUsed(caster);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 7);
        return passive(prev, from, to);
    }
}
