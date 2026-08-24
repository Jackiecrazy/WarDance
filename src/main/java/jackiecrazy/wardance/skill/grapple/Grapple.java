package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.MobilityUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class Grapple extends Skill {
    private final HashSet<String> unarm = makeTag(SkillTags.offensive, SkillTags.physical, SkillTags.unarmed);

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.grapple;
    }

    @Override
    public float spiritGain(LivingEntity caster) {
        return 14;
    }

    @Override
    public HashSet<String> getTags() {
        return unarm;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.HOLSTERED && isUnarmed(caster)) {
            if(procPoint instanceof PlayInteractionEvent.Pre pie)
                pie.setInteraction(WeaponStats.getSweepInfo(ItemStack.EMPTY, caster, WeaponStats.AttackState.STANDING, true, InteractionHand.MAIN_HAND));
            if (procPoint instanceof LivingAttackEvent la && la.getEntity() == target && DamageUtils.isMeleeAttack(la.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
                if (stats.isCondition() && caster.getLastHurtMob() == target && caster.tickCount - caster.getLastHurtMobTimestamp() < 40) {
                    performEffect(caster, target, stats);
                } else {
                    stats.flagCondition(true);
                    caster.setLastHurtMob(target);
                }
            } else if (procPoint instanceof StunEvent se && se.getEntity() == target && se.getPhase() == EventPriority.LOWEST)
                performEffect(caster, target, stats);
        }
        attackCooldown(procPoint, caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 7);
        }
        prev.flagCondition(false);
        return boundCast(prev, from, to);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, SkillData stats) {
        if (!cast(caster, target)) return;
        caster.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        CombatData.getCap(target).consumePosture(caster, 7 * stats.getEffectiveness() * stats.getEffectiveness(), ICombatCapability.BreachLevel.STUN);
        ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, caster.position(), 0, 1, getColor(), 0);
    }

    protected boolean isUnarmed(LivingEntity caster) {
        return WeaponStats.isUnarmed(caster.getMainHandItem(), caster);
    }

    public static class Suplex extends Grapple {

        protected void performEffect(LivingEntity caster, LivingEntity target, SkillData stats) {
            if (!cast(caster, target)) return;
            caster.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            final ICombatCapability casterCap = CombatData.getCap(caster);
            float posture = casterCap.getPosture();
            getExistingData(caster).addTarget(target);
            target.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).scale(-0.3)));
            target.hurtMarked = true;
            float overflow = CombatData.getCap(target).consumePosture(caster, posture * 1.5f, ICombatCapability.BreachLevel.KNOCKDOWN);
            if (overflow < 0) {
                //suplex shockwave
                ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, target.position(), 0, 7 * stats.getEffectiveness(), getColor(), 0);
                //SkillUtils.createCloud(caster.level(), caster, target.getX(), target.getY(), target.getZ(), 7 * stats.getEffectiveness(), ParticleTypes.LARGE_SMOKE);
                for (LivingEntity entity : target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBoxForCulling().inflate(7 * stats.getEffectiveness()), a -> !TargetingUtils.isAlly(a, caster))) {
                    entity.addEffect(new MobEffectInstance(FootworkEffects.UNSTEADY.get(), 40));
                    MobilityUtils.knockBack(entity, target, 0.6f, true, false);
                    CombatData.getCap(entity).consumePosture(caster, overflow / -2);
                }
            }
            casterCap.consumePosture(posture, 0.7f);
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            super.onProc(caster, procPoint, state, stats, target);
        }
    }
}
