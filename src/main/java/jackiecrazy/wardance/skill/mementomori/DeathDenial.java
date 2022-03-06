package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class DeathDenial extends MementoMori {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", ProcPoints.recharge_sleep, ProcPoints.change_heals, ProcPoints.change_parry_result, ProcPoints.on_being_damaged)));


    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof LivingDamageEvent && ((LivingDamageEvent) procPoint).getEntityLiving() == caster && (((LivingDamageEvent) procPoint).getAmount() > caster.getHealth() || stats.isCondition())) {
            if (!stats.isCondition())
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            onStateChange(caster, stats, stats.getState(), STATE.ACTIVE);
            procPoint.setCanceled(true);
        }
        if (procPoint instanceof ParryEvent && state == STATE.ACTIVE) {
            procPoint.setResult(Event.Result.ALLOW);
            ((ParryEvent) procPoint).setPostureConsumption(0);
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
