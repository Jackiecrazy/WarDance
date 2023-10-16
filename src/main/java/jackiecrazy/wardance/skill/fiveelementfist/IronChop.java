package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class IronChop extends FiveElementFist {
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ParryEvent e && e.getEntity() == caster && e.canParry() && procPoint.getPhase() == EventPriority.HIGHEST && stats.isCondition()) {
            e.setPostureConsumption(0);
            if (caster.level instanceof ServerLevel s)
                for (int reps = 0; reps < 40; reps++) {
                    Vec3 startAt = caster.position().add((((caster.tickCount+reps) * 5) % caster.getBbWidth()) - caster.getBbWidth() / 2, (((caster.tickCount+reps) * 31) % caster.getBbHeight()), (((caster.tickCount+reps) * 17) % caster.getBbWidth()) - caster.getBbWidth() / 2);
                    Vec3 move = startAt.subtract(caster.position()).normalize();
                    s.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()).setPos(caster.blockPosition()), startAt.x, startAt.y, startAt.z, 0, move.x, move.y, move.z, 1);
                }
            stats.flagCondition(false);
        }
        if (procPoint instanceof LivingHurtEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() != caster && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            mark(caster, target, 1, SkillUtils.getSkillEffectiveness(caster));
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)//swap in
            prev.flagCondition(true);
        return super.onStateChange(caster, prev, from, to);
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        target.setDeltaMovement(target.getDeltaMovement().add(0, -2*SkillUtils.getSkillEffectiveness(caster), 0));
        target.fallDistance += 5 * SkillUtils.getSkillEffectiveness(caster);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        removeMark(target);
        target.setDeltaMovement(target.getDeltaMovement().add(0, -1 * SkillUtils.getSkillEffectiveness(caster), 0));
        target.hurtMarked = true;
        return super.markTick(caster, target, sd);
    }
}
