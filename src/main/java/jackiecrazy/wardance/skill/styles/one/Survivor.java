package jackiecrazy.wardance.skill.styles.one;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.footwork.event.GainMightEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillStyle;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;

public class Survivor extends SkillStyle {
    public Survivor() {
        super(1);
    }

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return none;
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

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof LivingDamageEvent e && e.getEntity() == caster && ((e.getAmount() > caster.getHealth()  && CombatData.getCap(caster).consumeMight(2)) || stats.isCondition())) {
            if (!stats.isCondition())
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            onStateChange(caster, stats, stats.getState(), STATE.ACTIVE);
            stats.setArbitraryFloat(0);
            procPoint.setCanceled(true);
        }
        if (procPoint instanceof ConsumePostureEvent && state == STATE.ACTIVE) {
            procPoint.setResult(Event.Result.ALLOW);
            procPoint.setCanceled(true);
            ((ConsumePostureEvent) procPoint).setAmount(0);
            ((ConsumePostureEvent) procPoint).setResetCooldown(false);
        }
        if (procPoint instanceof GainMightEvent e && state == STATE.ACTIVE) {
            e.setQuantity(0);
        }
        if (procPoint instanceof LivingHealEvent && state == STATE.ACTIVE) {
            ((LivingHealEvent) procPoint).setAmount(0);
            procPoint.setCanceled(true);
        }
        if (procPoint instanceof LivingDeathEvent die && procPoint.getPhase() == EventPriority.HIGHEST) {
            stats.addArbitraryFloat(die.getEntity() instanceof Player ? 10 : 1);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            return true;
        }
        if (from != STATE.ACTIVE && to == STATE.ACTIVE) {
            prev.setDuration(200);
            prev.setState(STATE.ACTIVE);
        }
        return passive(prev, from, to);
    }
}