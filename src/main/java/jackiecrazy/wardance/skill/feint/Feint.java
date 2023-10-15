package jackiecrazy.wardance.skill.feint;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.SweepEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Feint extends Skill {
    private final HashSet<String> proc = makeTag("physical", "disableShield", "noDamage", ProcPoints.melee, ProcPoints.afflict_tick, "boundCast", ProcPoints.countdown, ProcPoints.recharge_normal, ProcPoints.change_parry_result);
    private final HashSet<String> tag = makeTag(SkillTags.physical, SkillTags.offensive, "noDamage");

    @SubscribeEvent()
    public static void hurt(LivingAttackEvent e) {
        Entity seme = e.getSource().getEntity();
        LivingEntity uke = e.getEntity();
        //reduce mark "cooldown", trigger capricious strike
        if (seme instanceof LivingEntity && DamageUtils.isMeleeAttack(e.getSource())) {
            final LivingEntity caster = (LivingEntity) seme;
            final ISkillCapability cap = CasterData.getCap(caster);
            for (Skill feint : cap.getEquippedVariations(SkillArchetypes.feint))
                if (feint != null && Marks.getCap(uke).isMarked(SkillArchetypes.feint)) {
                    Marks.getCap(uke).getActiveMark(feint).ifPresent(a -> a.addArbitraryFloat(-1));
                }
        }
        //spirit bomb damage amplification
        Marks.getCap(uke).getActiveMark(WarSkills.SPIRIT_RESONANCE.get()).ifPresent((a) -> {
            if (a.getDuration() <= 0.1 && e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).canProcSkillEffects()) {
                if(seme!=null) {
                    ParticleUtils.playBonkParticle(uke.level, seme.getEyePosition().add(seme.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(seme, uke)))), 1.5, 0, 12, WarColors.AZURE);
                    seme.level.playSound(null, seme.getX(), seme.getY(), seme.getZ(), SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, 0.5f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                }
                a.flagCondition(true);
                a.addArbitraryFloat(-1);
                a.setDuration(1.1f);
                CombatData.getCap(uke).consumePosture(2);
            }
        });
    }

    @SubscribeEvent()
    public static void spiritBomb(LivingHurtEvent e) {
        LivingEntity uke = e.getEntity();
        Marks.getCap(uke).getActiveMark(WarSkills.SPIRIT_RESONANCE.get()).ifPresent((a) -> {
            if (a.isCondition() && e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).canProcSkillEffects()) {
                e.setAmount(e.getAmount() + 2);
            }
            a.flagCondition(false);
        });
    }

    @SubscribeEvent()
    public static void aware(EntityAwarenessEvent e) {
        LivingEntity seme = e.getAttacker();
        LivingEntity uke = e.getEntity();
        if (seme != null) {
            if (Marks.getCap(uke).isMarked(WarSkills.SMIRKING_SHADOW.get()) && Marks.getCap(uke).getActiveMark(WarSkills.SMIRKING_SHADOW.get()).get().getDuration() > 0.1 && CasterData.getCap(seme).getEquippedSkills().contains(WarSkills.SMIRKING_SHADOW.get())) {
                e.setAwareness(StealthUtils.Awareness.UNAWARE);
            }
        }
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.feint;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 2;
    }

    @Override
    public HashSet<String> getTags() {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.getDuration() > 0.1 || sd.getArbitraryFloat() <= 0) {
            sd.decrementDuration(0.05f);
        }
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof SweepEvent se && procPoint.getPhase() == EventPriority.LOWEST && (state == STATE.HOLSTERED || state == STATE.ACTIVE)) {
            se.setCanceled(true);
        }
        if (procPoint instanceof LivingAttackEvent e && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.HOLSTERED && DamageUtils.isMeleeAttack(e.getSource()) && e.getEntity() == target && cast(caster, target, -999)) {
            int dur = 20;
            if (Marks.getCap(target).isMarked(this)) {
                SkillData a = Marks.getCap(target).getActiveMark(this).get();
                dur -= a.getArbitraryFloat();
            }
            CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, dur);
            CombatData.getCap(target).setHandBind(InteractionHand.OFF_HAND, dur);
            CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, this == WarSkills.FOLLOWUP.get() ? 1 : (0.5f * stats.getEffectiveness()), true);
            mark(caster, target, dur / 20f + 0.1f, 6);
            procPoint.setCanceled(true);
            if (caster.level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(), target.getZ(), 20, target.getBbWidth(), target.getBbHeight(), target.getBbWidth(), 0f);
                ParticleUtils.playSweepParticle(FootworkParticles.SWEEP.get(), caster, caster.getEyePosition().add(caster.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) * 0.9)), 0, 0.5, getColor(), 0);
            }
            markUsed(caster);
        }
        if (procPoint instanceof LivingAttackEvent e && this == WarSkills.FOLLOWUP.get() && procPoint.getPhase() == EventPriority.LOWEST && DamageUtils.isMeleeAttack(e.getSource()) && e.getEntity() == target &&
                CombatData.getCap(target).getHandBind(InteractionHand.MAIN_HAND) > 0 && CombatData.getCap(target).getHandBind(InteractionHand.OFF_HAND) > 0) {
            CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, 0.5f * SkillUtils.getSkillEffectiveness(caster), true);
        }
    }

    //ignores all attempt to activate it
    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
        }
        return boundCast(prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null)
            sd.addArbitraryFloat(existing.getArbitraryFloat());
        return sd;
    }

    public static class ScorpionSting extends Feint {
        private final HashSet<String> tag = makeTag("physical", "disableShield", "noDamage", ProcPoints.melee, "boundCast", ProcPoints.on_hurt, ProcPoints.countdown, ProcPoints.recharge_normal, ProcPoints.change_parry_result);
        private final HashSet<String> no = makeTag("normalAttack");

        @Override
        public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
            for (MobEffectInstance ei : new ArrayList<>(target.getActiveEffects())) {
                MobEffectInstance override = new MobEffectInstance(ei.getEffect(), ei.getDuration(), ei.getAmplifier() + 1, ei.isAmbient(), ei.isVisible(), ei.showIcon());
                EffectUtils.stackPot(target, override, EffectUtils.StackingMethod.NONE);
            }
            return super.onMarked(caster, target, sd, existing);
        }
    }

    public static class UpperHand extends Feint {
        static final UUID UPPER = UUID.fromString("67fe7ef6-a398-4c62-9fb1-42edaa80e7c1");

        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            if (caster == target) {
                return true;
            }
            return markTickDown(sd);
        }

        @Override
        public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            if (existing != null) {
                sd.setDuration(10);
                sd.addArbitraryFloat(existing.getArbitraryFloat());
                if (caster == target) {
                    SkillUtils.modifyAttribute(caster, Attributes.ARMOR, UPPER, sd.getArbitraryFloat() * 2, AttributeModifier.Operation.ADDITION);
                } else {
                    target.addEffect(new MobEffectInstance(FootworkEffects.CORROSION.get(), 200));
                }
            }
            return super.onMarked(caster, target, sd, existing);
        }

        @Override
        public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
            if (caster == target && caster != null)
                caster.getAttribute(Attributes.ARMOR).removeModifier(UPPER);
            super.onMarkEnd(caster, target, sd);
        }
    }
}
