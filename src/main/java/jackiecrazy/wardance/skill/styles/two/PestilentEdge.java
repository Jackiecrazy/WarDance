package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class PestilentEdge extends SkillStyle {

    public PestilentEdge() {
        super(2);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ParryEvent cpe && cpe.getEntity() == target && cpe.getPhase() == EventPriority.HIGHEST) {
            int debuffs = 0;
            for (MobEffectInstance mei : target.getActiveEffects().stream().toList()) {
                if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL) debuffs += mei.getAmplifier() + 1;
            }
            debuffs += Marks.getCap(target).getActiveMarks().size();
            cpe.setPostureConsumption(cpe.getPostureConsumption() + debuffs * SkillUtils.getSkillEffectiveness(caster));
            if (debuffs > 0 && caster.level instanceof ServerLevel server)
                server.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY(), target.getZ(), (int) debuffs * 5, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);
        } else if (procPoint instanceof LivingHurtEvent cpe && cpe.getPhase() == EventPriority.HIGHEST && DamageUtils.isMeleeAttack(cpe.getSource()) && cpe.getEntity() == target) {
            if (!cpe.isCanceled()) {
                if (cpe.getSource() instanceof CombatDamageSource cds) {
                    cds.setProcSkillEffects(true);
                    cds.setSkillUsed(this);
                }
                int debuffs = 0;
                for (MobEffectInstance mei : target.getActiveEffects().stream().toList()) {
                    if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL) debuffs += mei.getAmplifier() + 1;
                }
                debuffs += Marks.getCap(target).getActiveMarks().size();
                cpe.setAmount(cpe.getAmount() + debuffs * SkillUtils.getSkillEffectiveness(caster));
            }
        }
    }
}
