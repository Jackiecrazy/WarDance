package jackiecrazy.wardance.skill.feint;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Feint extends Skill {
    private final Tag<String> proc = Tag.create(new HashSet<>(Arrays.asList("physical", "disableShield", "noDamage", ProcPoints.melee, ProcPoints.afflict_tick, "boundCast", ProcPoints.countdown, ProcPoints.recharge_normal, ProcPoints.change_parry_result)));
    private final Tag<String> tag = Tag.create(new HashSet<>(Arrays.asList(SkillTags.physical, SkillTags.offensive, "noDamage")));

    @SubscribeEvent()
    public static void hurt(LivingAttackEvent e) {
        Entity seme = e.getSource().getEntity();
        LivingEntity uke = e.getEntityLiving();
        //reduce mark "cooldown", trigger capricious strike
        if (seme instanceof LivingEntity) {
            final LivingEntity caster = (LivingEntity) seme;
            final ISkillCapability cap = CasterData.getCap(caster);
            final Skill feint = cap.getEquippedVariation(SkillCategories.feint);
            if (feint != null && Marks.getCap(uke).isMarked(SkillCategories.feint)) {
                Marks.getCap(uke).getActiveMark(feint).ifPresent(a -> a.setArbitraryFloat(a.getArbitraryFloat() - 1));
                if (cap.getEquippedSkills().contains(WarSkills.CAPRICIOUS_STRIKE.get()))
                    for (Skill s : cap.getEquippedSkills())
                        if (s != null && s.getTags(caster).contains(SkillTags.physical) && cap.getSkillState(s) == STATE.COOLING)
                            cap.getSkillData(s).ifPresent(SkillData::decrementDuration);
            }
        }
        //spirit bomb damage amplification
        Marks.getCap(uke).getActiveMark(WarSkills.SPIRIT_RESONANCE.get()).ifPresent((a) -> {
            if (a.getDuration() <= 0.1 && e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).canProcSkillEffects()) {
                a.flagCondition(true);
                a.setArbitraryFloat(a.getArbitraryFloat() - 1);
                a.setDuration(1.1f);
                CombatData.getCap(uke).consumePosture(2);
            }
        });
    }

    @SubscribeEvent()
    public static void spiritBomb(LivingHurtEvent e) {
        LivingEntity uke = e.getEntityLiving();
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
        LivingEntity uke = e.getEntityLiving();
        if (seme != null) {
            if (Marks.getCap(uke).isMarked(WarSkills.SMIRKING_SHADOW.get()) && Marks.getCap(uke).getActiveMark(WarSkills.SMIRKING_SHADOW.get()).get().getDuration() > 0.1 && CasterData.getCap(seme).getEquippedSkills().contains(WarSkills.SMIRKING_SHADOW.get())) {
                e.setAwareness(StealthUtils.Awareness.UNAWARE);
            }
        }
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Nonnull
    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 3;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.HOLSTERED && ((LivingAttackEvent) procPoint).getEntityLiving() == target && cast(caster, target, -999)) {
            int dur = 20;
            if (Marks.getCap(target).isMarked(this)) {
                SkillData a = Marks.getCap(target).getActiveMark(this).get();
                dur -= a.getArbitraryFloat();
            }
            CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, dur);
            CombatData.getCap(target).setHandBind(Hand.OFF_HAND, dur);
            CombatUtils.setHandCooldown(caster, Hand.MAIN_HAND, this == WarSkills.FOLLOWUP.get() ? 1 : 0.5f, true);
            mark(caster, target, dur / 20f + 0.1f, 6);
            procPoint.setCanceled(true);
            markUsed(caster);
        }
        if (procPoint instanceof LivingAttackEvent && this == WarSkills.FOLLOWUP.get() && procPoint.getPhase() == EventPriority.LOWEST && ((LivingAttackEvent) procPoint).getEntityLiving() == target &&
                CombatData.getCap(target).getHandBind(Hand.MAIN_HAND) > 0 && CombatData.getCap(target).getHandBind(Hand.OFF_HAND) > 0) {
            CombatUtils.setHandCooldown(caster, Hand.MAIN_HAND, 0.5f, true);
        }
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null)
            sd.setArbitraryFloat(sd.getArbitraryFloat() + existing.getArbitraryFloat());
        return sd;
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
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.getDuration() > 0.1 || sd.getArbitraryFloat() <= 0) {
            sd.decrementDuration(0.05f);
        }
        return super.markTick(caster, target, sd);
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.feint;
    }

    public static class SpiritBomb extends Feint {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

    }

    public static class SmirkingShadow extends Feint {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public static class CapriciousStrike extends Feint {
        @Override
        public Color getColor() {
            return Color.orange;
        }

    }

    public static class ScorpionSting extends Feint {
        private final Tag<String> tag = Tag.create(new HashSet<>(Arrays.asList("physical", "disableShield", "noDamage", ProcPoints.melee, "boundCast", ProcPoints.on_hurt, ProcPoints.countdown, ProcPoints.recharge_normal, ProcPoints.change_parry_result)));
        private final Tag<String> no = Tag.create(new HashSet<>(Arrays.asList("normalAttack")));

        @Override
        public Color getColor() {
            return WarColors.VIOLET;
        }

        @Override
        public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            target.addEffect(new EffectInstance(Effects.POISON, 200));
            for (EffectInstance ei : new ArrayList<>(target.getActiveEffects())) {
                EffectInstance override = new EffectInstance(ei.getEffect(), ei.getDuration(), ei.getAmplifier() + 1, ei.isAmbient(), ei.isVisible(), ei.showIcon());
                EffectUtils.stackPot(target, override, EffectUtils.StackingMethod.NONE);
            }
            return super.onMarked(caster, target, sd, existing);
        }
    }

    public static class UpperHand extends Feint {
        static final UUID UPPER = UUID.fromString("67fe7ef6-a398-4c62-9fb1-42edaa80e7c1");

        @Override
        public Color getColor() {
            return Color.GREEN;
        }


        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            if (caster == target) {
                sd.decrementDuration();
                return true;
            }
            return super.markTick(caster, target, sd);
        }

        @Override
        public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            if (existing != null) {
                sd.setDuration(200);
                sd.setArbitraryFloat(sd.getArbitraryFloat() + existing.getArbitraryFloat());
                if (caster == target) {
                    SkillUtils.modifyAttribute(caster, Attributes.ARMOR, UPPER, sd.getArbitraryFloat() * 2, AttributeModifier.Operation.ADDITION);
                } else {
                    target.addEffect(new EffectInstance(FootworkEffects.CORROSION.get(), 200));
                }
            }
            return super.onMarked(caster, target, sd, existing);
        }

        @Override
        public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
            caster.getAttribute(Attributes.ARMOR).removeModifier(UPPER);
            super.onMarkEnd(caster, target, sd);
        }
    }
}
