package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class Throw extends Grapple {


    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.HOLSTERED && isUnarmed(caster)) {
            if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntity() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
                if (stats.isCondition() && caster.getLastHurtMob() == target && caster.tickCount - caster.getLastHurtMobTimestamp() < 40 && cast(caster, target, -999)) {
                    performEffect(caster, target, stats);
                } else {
                    stats.flagCondition(true);
                    caster.setLastHurtMob(target);
                }
            } else if (procPoint instanceof StunEvent se && se.getEntity() == target && se.getPhase() == EventPriority.LOWEST)
                performEffect(caster, target, stats);
        }
        attackCooldown(procPoint, caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.ACTIVE && caster.getFirstPassenger() != null && cast(caster)) {
            //yeet!
            Entity yeet = caster.getFirstPassenger();
            yeet.stopRiding();
            yeet.setDeltaMovement(caster.getLookAngle().scale(1.5));
            if (yeet instanceof LivingEntity e) mark(caster, e, 5, 0, true);
        }
        if (to == STATE.COOLING) {
            if (prev.isCondition())
                prev.setState(STATE.HOLSTERED);
            else
                setCooldown(caster, prev, 7);
        }
        return boundCast(prev, from, to);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, SkillData stats) {
        caster.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        if (CombatData.getCap(target).consumePosture(caster, 7, 0, true) < 0 || CombatData.getCap(target).isVulnerable()) {
            target.startRiding(caster, true);
            mark(caster, target, CombatConfig.knockdownDuration * 2);
            stats.flagCondition(true);
        } else {
            stats.flagCondition(false);
            markUsed(caster);
        }
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration(0.05f);
        Entity collide = GeneralUtils.collidingEntity(target);
        if (sd.isCondition() && (target.horizontalCollision || target.verticalCollision || collide != null)) {
            removeMark(target);
            CombatData.getCap(target).consumePosture(7);
            if (caster != null) {
                target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 3);
                if (collide instanceof LivingEntity elb) {
                    CombatData.getCap(elb).consumePosture(7);
                    elb.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 3);
                }
            }
        }
        if (!sd.isCondition()) {
            if (!CombatData.getCap(target).isVulnerable()) {
                removeMark(target);
                target.stopRiding();
                setCooldown(caster, CasterData.getCap(caster).getSkillData(this).get(), 7);
            }
        }
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (target.isPassenger()) target.stopRiding();
    }
}
