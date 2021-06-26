package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class DeathDenial extends MementoMori {
    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 100);
        return true;
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        onCast(caster);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 10);
    }

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof LivingDamageEvent && (((LivingDamageEvent) procPoint).getAmount() > target.getHealth() || stats.isCondition())) {
            stats.flagCondition(true);
            procPoint.setCanceled(true);
        }
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (d.isCondition()) {
            d.decrementDuration();
            //marking and sweeping is automatically done by the capability. Thanks, me!
            return true;
        }
        return super.activeTick(caster, d);
    }
}
