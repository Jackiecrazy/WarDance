package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.EffectUtils;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Brutalize extends Judgment {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 5;
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        super.performEffect(caster, target, stack, sd);
        if (stack == 3) {
            if(target.level instanceof ServerWorld)
            ((ServerWorld) target.level).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 20, target.getBbWidth() / 4, target.getBbHeight() / 4, target.getBbWidth() / 4, 0.5f);
            CombatData.getCap(target).setStaggerTime(0);
            CombatData.getCap(target).consumePosture(caster, Float.MAX_VALUE, 0, true);
        }
        final List<LivingEntity> list = caster.level.getLoadedEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(10), (a) -> TargetingUtils.isHostile(a, caster));
        for (LivingEntity enemy : list) {
            enemy.addEffect(new EffectInstance(WarEffects.ENFEEBLE.get(), 200));
            if (stack == 3 && target.getMaxHealth() > enemy.getMaxHealth())
                EffectUtils.causeFear(enemy, caster, 200);
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof StaggerEvent && state == STATE.ACTIVE && !stats.isCondition() && procPoint.getPhase() == EventPriority.HIGHEST && ((StaggerEvent) procPoint).getAttacker() == caster) {
            ((StaggerEvent) procPoint).setCount(((StaggerEvent) procPoint).getCount() * 2);
            ((StaggerEvent) procPoint).setLength(((StaggerEvent) procPoint).getLength() * 2);
        }
    }
}