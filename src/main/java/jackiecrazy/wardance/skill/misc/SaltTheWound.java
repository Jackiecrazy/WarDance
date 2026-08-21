package jackiecrazy.wardance.skill.misc;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageTypeTags;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.mixin.MobEffectInstanceAccessor;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;
import java.util.HashSet;

public class SaltTheWound extends Skill {
    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {
        if (procPoint instanceof MeleePostureEvent.Pre cpe && cpe.getEntity() == target && cpe.getPhase() == EventPriority.HIGHEST) {
            int debuffs = 0;
            for (MobEffectInstance mei : target.getActiveEffects().stream().toList()) {
                if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL) debuffs += mei.getAmplifier() + 1;
            }
            debuffs += Marks.getCap(target).getActiveMarks().size();
            cpe.setPostureConsumption(cpe.getPostureConsumption() + debuffs * SkillUtils.getSkillEffectiveness(caster));
            if (debuffs > 0 && caster.level() instanceof ServerLevel server)
                server.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY(), target.getZ(), (int) debuffs * 5, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);
        } else if (procPoint instanceof LivingHurtEvent cpe && cpe.getPhase() == EventPriority.HIGHEST && cpe.getEntity() == target) {
            if (!cpe.isCanceled()) {
                if (cpe.getSource().is(FootworkDamageTypeTags.SKILL)) {
                    if (cpe.getSource() instanceof CombatDamageSource cds) {
                        cds.setProcSkillEffects(true);
                        cds.setSkillUsed(this);
                    }
                    int debuffs = 0;
                    for (MobEffectInstance mei : target.getActiveEffects().stream().toList()) {
                        if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                            debuffs += mei.getAmplifier() + 1;
                    }
                    debuffs += Marks.getCap(target).getActiveMarks().size();
                    cpe.setAmount(cpe.getAmount() + debuffs * SkillUtils.getSkillEffectiveness(caster));
                }
                if (cpe.getSource().isIndirect()) {
                    for (MobEffectInstance mei : target.getActiveEffects().stream().toList()) {
                        if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                            ((MobEffectInstanceAccessor) mei).setDuration((int) (mei.getDuration() + 10 * cpe.getAmount()));
                    }
                }
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return true;
    }
}
