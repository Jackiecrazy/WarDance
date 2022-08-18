package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import java.awt.*;
import java.util.List;

public class FeverDream extends Judgment {
    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        super.performEffect(caster, target, stack, sd);
        if (stack != 3) return;
        hallucinate(caster, target, 3);
    }

    private void hallucinate(LivingEntity caster, LivingEntity target, int iterate) {
        if (iterate < 0) return;
        SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), 15, ParticleTypes.LARGE_SMOKE);
        final List<LivingEntity> list = caster.level.getLoadedEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(7), (a) -> TargetingUtils.isHostile(a, caster));
        Marks.getCap(target).removeMark(this);
        for (int i = 0; i < list.size(); i++) {
            LivingEntity enemy = list.get(i);
            if (GeneralUtils.getDistSqCompensated(target, enemy) < 49)
                enemy.addEffect(new EffectInstance(Effects.BLINDNESS, 140));
            if (list.size() <= 1) return;
            int shift = WarDance.rand.nextInt(list.size());
            final LivingEntity confuse = list.get(shift);
            enemy.setLastHurtByMob(confuse);
            if (enemy instanceof MobEntity) {
                ((MobEntity) enemy).setTarget(confuse);
                GoalCapabilityProvider.getCap(enemy).ifPresent(a->a.setForcedTarget(confuse));
            }
            if (Marks.getCap(enemy).isMarked(this)) {
                hallucinate(caster, enemy, iterate - 1);
                removeMark(enemy);
            }
        }
    }
}
