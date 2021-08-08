package jackiecrazy.wardance.skill.feint;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.StatusEffects;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Feint extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", "noDamage", SkillTags.melee, SkillTags.afflict_tick, "boundCast", SkillTags.countdown, SkillTags.recharge_normal, SkillTags.change_parry_result)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 50);
        CombatData.getCap(caster).consumeSpirit(5);
        return true;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 5;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 3);
        if (stats.getDuration() > 0 && getParentSkill() == null) {
            CombatUtils.setHandCooldown(caster, stats.isCondition() ? Hand.MAIN_HAND : Hand.OFF_HAND, 1, true);
        }
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && CombatUtils.getAwareness(caster, target) == CombatUtils.Awareness.ALERT && !StatusEffects.getCap(target).isStatusActive(this)) {
            Hand h = ((ParryEvent) procPoint).getAttackingHand();
            if (((ParryEvent) procPoint).canParry()) {
                CombatUtils.setHandCooldown(target, Hand.MAIN_HAND, 0, false);
                CombatUtils.setHandCooldown(target, Hand.OFF_HAND, 0, false);
            } else {
                float above = getParentSkill() == null ? 0 : 0.1f;
                CombatData.getCap(target).consumePosture(((ParryEvent) procPoint).getAttackDamage(), above);
                CombatData.getCap(target).consumePosture(((ParryEvent) procPoint).getPostureConsumption(), above);
                ((ParryEvent) procPoint).setPostureConsumption(0);
                procPoint.setResult(Event.Result.ALLOW);
            }
            stats.flagCondition(h == Hand.MAIN_HAND);
            afflict(caster, target, 1);
            markUsed(caster);
        }
    }

    @Override
    public boolean statusTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (CombatUtils.getAwareness(caster, target) != CombatUtils.Awareness.ALERT)
            endAffliction(target);
        return false;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Feint.class ? null : WarSkills.FEINT.get();
    }

    public static class LastSurprise extends Feint {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            target.addPotionEffect(new EffectInstance(WarEffects.DISTRACTION.get(), 60));
        }
    }

    public static class SmirkingShadow extends Feint {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
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
        protected void afflict(LivingEntity caster, LivingEntity target, float duration) {
            //no affliction, keep doing it!
        }

        @Override
        public void onEffectEnd(LivingEntity caster, SkillData stats) {
            setCooldown(caster, 6);
        }

    }

    public static class ScorpionSting extends Feint {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", "noDamage", SkillTags.melee, "boundCast", SkillTags.on_hurt, SkillTags.countdown, SkillTags.recharge_normal, SkillTags.change_parry_result)));
        private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public Tag<String> getTags(LivingEntity caster) {
            return tag;
        }

        @Override
        public Tag<String> getIncompatibleTags(LivingEntity caster) {
            return no;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof LivingHurtEvent) {
                ((LivingHurtEvent) procPoint).setAmount(0);
                markUsed(caster);
            }
            if (procPoint instanceof ParryEvent) {
                if (((ParryEvent) procPoint).canParry()) {
                    CombatUtils.setHandCooldown(target, Hand.MAIN_HAND, 0, false);
                    CombatUtils.setHandCooldown(target, Hand.OFF_HAND, 0, false);
                } else {
                    CombatData.getCap(target).consumePosture(((ParryEvent) procPoint).getAttackDamage(), 0.1f);
                }
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
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof LivingHurtEvent) {
                CombatData.getCap(caster).addPosture(((LivingHurtEvent) procPoint).getAmount());
            }
            super.onSuccessfulProc(caster, stats, target, procPoint);
        }
    }
}
