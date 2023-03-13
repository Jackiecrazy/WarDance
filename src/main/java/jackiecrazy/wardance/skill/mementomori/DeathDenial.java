package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.SetTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

import jackiecrazy.wardance.skill.Skill.STATE;

public class DeathDenial extends MementoMori {
    private final SetTag<String> tag = SetTag.create(new HashSet<>(Arrays.asList("passive", ProcPoints.recharge_sleep, ProcPoints.change_heals, ProcPoints.change_parry_result, ProcPoints.on_being_damaged)));


    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof LivingDamageEvent && ((LivingDamageEvent) procPoint).getEntityLiving() == caster && state!=STATE.COOLING && (((LivingDamageEvent) procPoint).getAmount() > caster.getHealth() || stats.isCondition())) {
            if (!stats.isCondition())
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            onStateChange(caster, stats, stats.getState(), STATE.ACTIVE);
            procPoint.setCanceled(true);
        }
        if (procPoint instanceof ConsumePostureEvent && state == STATE.ACTIVE) {
            procPoint.setResult(Event.Result.ALLOW);
            procPoint.setCanceled(true);
            ((ConsumePostureEvent) procPoint).setAmount(0);
            ((ConsumePostureEvent) procPoint).setResetCooldown(false);
        }
        if (procPoint instanceof LivingHealEvent && state == STATE.ACTIVE) {
            ((LivingHealEvent) procPoint).setAmount(0);
            procPoint.setCanceled(true);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 10);
            return true;
        }
        if (from != STATE.ACTIVE && to == STATE.ACTIVE) {
            prev.setDuration(100);
            prev.setState(STATE.ACTIVE);
        }
        return passive(prev, from, to);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData d) {
        if (d.getState() == STATE.ACTIVE) {
            d.decrementDuration();
            //marking and sweeping is automatically done by the capability. Thanks, me!
            return true;
        }
        return super.equippedTick(caster, d);
    }
}
