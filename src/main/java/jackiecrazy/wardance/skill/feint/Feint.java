package jackiecrazy.wardance.skill.feint;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Feint extends Skill {
    private final Tag<String> proc = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", "noDamage", ProcPoints.melee, ProcPoints.afflict_tick, "boundCast", ProcPoints.countdown, ProcPoints.recharge_normal, ProcPoints.change_parry_result)));
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList(SkillTags.physical, SkillTags.offensive, "disableShield", "noDamage")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 4;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.HOLSTERED && CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster)) && ((ParryEvent) procPoint).getAttacker() == caster && CombatUtils.getAwareness(caster, target) == CombatUtils.Awareness.ALERT && !Marks.getCap(target).isMarked(this)) {
            Hand h = ((ParryEvent) procPoint).getAttackingHand();
            if (((ParryEvent) procPoint).canParry()) {
                CombatUtils.setHandCooldown(target, Hand.MAIN_HAND, 0, false);
                CombatUtils.setHandCooldown(target, Hand.OFF_HAND, 0, false);
            } else {
                float above = this == WarSkills.FOLLOWUP.get() ? 0 : 0.1f;
                CombatData.getCap(target).consumePosture(caster, ((ParryEvent) procPoint).getAttackDamage(), above);
                CombatData.getCap(target).consumePosture(caster, ((ParryEvent) procPoint).getPostureConsumption(), above);
                ((ParryEvent) procPoint).setPostureConsumption(0);
                procPoint.setResult(Event.Result.ALLOW);
            }
            stats.flagCondition(h == Hand.MAIN_HAND);
            mark(caster, target, 1);
            markUsed(caster);
        }
        attackCooldown(procPoint, caster, stats);
    }

    //ignores all attempt to activate it
    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, 3);
            if (this == WarSkills.FOLLOWUP.get()) {
                CombatUtils.setHandCooldown(caster, prev.isCondition() ? Hand.MAIN_HAND : Hand.OFF_HAND, 1, true);
            }
        }
        return boundCast(prev, from, to);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (CombatUtils.getAwareness(caster, target) == CombatUtils.Awareness.UNAWARE)
            removeMark(target);
        return false;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.feint;
    }

    public static class LastSurprise extends Feint {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            super.onProc(caster, procPoint, state, stats, target);
            target.addPotionEffect(new EffectInstance(WarEffects.DISTRACTION.get(), 60));
        }
    }

    public static class SmirkingShadow extends Feint {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            super.onProc(caster, procPoint, state, stats, target);
            Vector3d tp = GeneralUtils.getPointInFrontOf(target, caster, -2);
            caster.setPositionAndRotation(tp.x, tp.y, tp.z, -caster.rotationYaw, -caster.rotationPitch);
        }
    }

    public static class CapriciousStrike extends Feint {
        @Override
        public Color getColor() {
            return Color.orange;
        }

        @Override
        protected void mark(LivingEntity caster, LivingEntity target, float duration) {
            //no affliction, keep doing it!
        }

        @Override
        public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
            if (to == STATE.COOLING) {
                setCooldown(caster, 6);
            }
            return boundCast(prev, from, to);
        }

    }

    public static class ScorpionSting extends Feint {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", "noDamage", ProcPoints.melee, "boundCast", ProcPoints.on_hurt, ProcPoints.countdown, ProcPoints.recharge_normal, ProcPoints.change_parry_result)));
        private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof LivingHurtEvent) {
                ((LivingHurtEvent) procPoint).setAmount(0);
                markUsed(caster);
            }
            if (procPoint instanceof ParryEvent) {
                if (((ParryEvent) procPoint).canParry()) {
                    CombatUtils.setHandCooldown(target, Hand.MAIN_HAND, 0, false);
                    CombatUtils.setHandCooldown(target, Hand.OFF_HAND, 0, false);
                } else {
                    CombatData.getCap(target).consumePosture(caster, ((ParryEvent) procPoint).getAttackDamage(), 0.1f);
                }
                procPoint.setResult(Event.Result.DENY);
                markUsed(caster);
            }
        }
    }

    public static class UpperHand extends Feint {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof LivingHurtEvent) {
                CombatData.getCap(caster).addPosture(((LivingHurtEvent) procPoint).getAmount());
            }
            super.onProc(caster, procPoint, state, stats, target);
        }
    }
}
