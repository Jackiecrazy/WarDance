package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class WalkOfDionysus extends SkillStyle {

    public WalkOfDionysus() {
        super(3);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (!CombatData.getCap(caster).isStunned() && StylishData.getCap(caster).canTrigger()) {
            CombatData.getCap(caster).knockdown(CombatConfig.knockdownDurationPlayer);
            StylishData.getCap(caster).resetTriggerBar();
            CombatData.getCap(caster).setSpirit(CombatData.getCap(caster).getMaxSpirit());
            //fall(caster);
            CombatData.getCap(caster).knockdown(0);
        }
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof StunEvent se && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (se.getEntity() == caster) {
                se.setKnockdown(true);
                fall(caster);
            }
        }
    }

    private void fall(LivingEntity caster) {
        //area damage
        final float radius = 5 * SkillUtils.getSkillEffectiveness(caster);
        SkillUtils.createCloud(caster.level(), caster, caster.getX(), caster.getY(), caster.getZ(), radius, ParticleTypes.LARGE_SMOKE);
        for (LivingEntity entity : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBoxForCulling().inflate(radius), a -> !TargetingUtils.isAlly(a, caster))) {
            CombatData.getCap(entity).consumePosture(caster, 10);
            entity.hurt(new CombatDamageSource(caster).setDamageTyping(FootworkDamageArchetype.PHYSICAL).setSkillUsed(this).setProcSkillEffects(true).setProcAttackEffects(true), 10);
            entity.addDeltaMovement(new Vec3(0,1,0));
            entity.hurtMarked=true;

        }
    }
}
