package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Grapple extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "normalAttack", "countdown", "unarmed", "rechargeWithAttack")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "unarmed")));

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Grapple.class ? null : WarSkills.GRAPPLE.get();
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
    public boolean onCast(LivingEntity caster) {
        activate(caster, 60);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 7);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(target).consumePosture(10);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent && CombatUtils.isUnarmed(caster.getHeldItemMainhand(), caster)) {
            if (stats.isCondition() && caster.getLastAttackedEntity() == target) {
                performEffect(caster, target);
                markUsed(caster);
            }
            else stats.flagCondition(true);
        }
    }

    public static class Reversal extends Grapple {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            final ICombatCapability casterCap = CombatData.getCap(caster);
            float casterPerc = casterCap.getPosture() / casterCap.getMaxPosture();
            final ICombatCapability targetCap = CombatData.getCap(target);
            float targetPerc = targetCap.getPosture() / targetCap.getMaxPosture();
            casterCap.setPosture(targetPerc * casterCap.getMaxPosture());
            targetCap.setPosture(casterPerc * targetCap.getMaxPosture());
        }
    }

    public static class Suplex extends Grapple {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            final ICombatCapability casterCap = CombatData.getCap(caster);
            float posture = casterCap.getPosture();
            target.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(-0.3)));
            target.velocityChanged = true;
            CombatData.getCap(target).consumePosture(posture * 2, 0, true);
            casterCap.setPosture(0.1f);
        }
    }
}
