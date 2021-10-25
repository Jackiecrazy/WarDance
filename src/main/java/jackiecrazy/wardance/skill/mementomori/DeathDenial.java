package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.ProcPoints;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class DeathDenial extends MementoMori {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", ProcPoints.recharge_sleep, ProcPoints.change_heals, ProcPoints.change_parry_result, ProcPoints.on_being_damaged)));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

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
        if(procPoint instanceof ParryEvent&&stats.isCondition()){
            procPoint.setResult(Event.Result.ALLOW);
            ((ParryEvent) procPoint).setPostureConsumption(0);
        }
        if(procPoint instanceof LivingHealEvent &&stats.isCondition()){
            ((LivingHealEvent) procPoint).setAmount(0);
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
