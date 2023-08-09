package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;

public class Afterimage extends IronGuard {
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof final ParryEvent pe && procPoint.getPhase() == EventPriority.HIGHEST && state!=STATE.COOLING && pe.getEntity() == caster && pe.canParry() && pe.getPostureConsumption() > 0) {
            parry(caster, pe, stats, target, state);
        }
    }
    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (!caster.isShiftKeyDown() || state == STATE.COOLING || !cast(caster, target, -999)) return;
        final float cost = procPoint.getPostureConsumption() * stats.getEffectiveness();
        SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), cost, ParticleTypes.LARGE_SMOKE);
        for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(cost))) {
            if (e.distanceToSqr(caster) < cost * cost) {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
            }
        }
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20));
        markUsed(caster);
        procPoint.setPostureConsumption(0);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 10);
        return passive(prev, from, to);
    }
}
