package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;

public class ShadowDive extends MementoMori {
    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingDamageEvent && state == STATE.INACTIVE && ((LivingDamageEvent) procPoint).getEntityLiving() == caster && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingDamageEvent) procPoint).getAmount() > caster.getHealth()) {
            activate(caster, 160);
            caster.addEffect(new EffectInstance(Effects.INVISIBILITY, 160));
            caster.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 160));
            SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), 7, ParticleTypes.LARGE_SMOKE);

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
                for (LivingEntity e : caster.level.getLoadedEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(40), (a) -> TargetingUtils.isHostile(a, caster))) {
                    e.setLastHurtByMob((LivingEntity) tar);
                    if (e instanceof MobEntity) {
                        ((MobEntity) e).setTarget((LivingEntity) tar);
                        GoalCapabilityProvider.getCap(e).ifPresent(a->a.setForcedTarget((LivingEntity) tar));
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
