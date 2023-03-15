package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.awt.*;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Afterimage extends IronGuard {
    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 10);
        return passive(prev, from, to);
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (!caster.isShiftKeyDown()||state==STATE.COOLING) return;
        final float cost = ((ParryEvent) procPoint).getPostureConsumption();
        SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), cost, ParticleTypes.LARGE_SMOKE);
        for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(cost))) {
            if (e.distanceToSqr(caster) > cost * cost) {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
            }
        }
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20));
        markUsed(caster);
        ((ParryEvent) procPoint).setPostureConsumption(0);
    }
}
