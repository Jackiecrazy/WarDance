package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class ShadowDive extends MementoMori {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingDamageEvent lde && state == STATE.INACTIVE && lde.getEntity() == caster && procPoint.getPhase() == EventPriority.LOWEST && lde.getAmount() > caster.getHealth() / 4) {
            activate(caster, 160);
            lde.setAmount(caster.getHealth() / 4);
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160));
            caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160));
            SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), 7, ParticleTypes.LARGE_SMOKE);
        }
        if (procPoint instanceof LivingAttackEvent lde && state == STATE.INACTIVE && lde.getEntity() == target && procPoint.getPhase() == EventPriority.HIGHEST && !stats.isCondition()) {
            stats.flagCondition(true);
            SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), 7, ParticleTypes.ANGRY_VILLAGER);
            for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(40), (a) -> TargetingUtils.isHostile(a, caster))) {
                e.setLastHurtByMob(target);
                if (e instanceof Mob mob) {
                    mob.setTarget(target);
                    GoalCapabilityProvider.getCap(e).ifPresent(a -> a.setForcedTarget(target));
                }

            }
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
            Entity tar = GeneralUtils.collidingEntity(caster);
            //collide entity, transfer aggression
            if (tar instanceof LivingEntity && !d.isCondition()) {
                d.flagCondition(true);
                SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), 7, ParticleTypes.ANGRY_VILLAGER);
                for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(40), (a) -> TargetingUtils.isHostile(a, caster))) {
                    e.setLastHurtByMob((LivingEntity) tar);
                    if (e instanceof Mob) {
                        ((Mob) e).setTarget((LivingEntity) tar);
                        GoalCapabilityProvider.getCap(e).ifPresent(a -> a.setForcedTarget((LivingEntity) tar));
                    }

                }
            }
            return true;
        }
        if (d.getState() == STATE.COOLING && caster.getHealth() == caster.getMaxHealth()) {
            d.setDuration(-10);
        }
        return super.equippedTick(caster, d);
    }
}
