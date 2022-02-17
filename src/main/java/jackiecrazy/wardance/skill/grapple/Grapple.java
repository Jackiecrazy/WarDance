package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Grapple extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "melee", "normalAttack", "countdown", "unarmed", "rechargeWithAttack")));
    private final Tag<String> unarm = makeTag(SkillTags.offensive, SkillTags.physical, SkillTags.unarmed);

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.grapple;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return unarm;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 2;
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {
        caster.world.playSound(null, target.getPosX(), target.getPosY(), target.getPosZ(), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        if (this==WarSkills.THROW.get()) CombatData.getCap(target).consumePosture(caster, 11, 0, true);
        else CombatData.getCap(target).consumePosture(caster, 7, 0, true);

    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (state == STATE.HOLSTERED && isUnarmed(caster)) {
                if (stats.isCondition() && caster.getLastAttackedEntity() == target && caster.ticksExisted-caster.getLastAttackedEntityTime()<40 && cast(caster, -999)) {
                    performEffect(caster, target);
                } else stats.flagCondition(true);
            }
        }
        attackCooldown(procPoint, caster, stats);
    }

    protected boolean isUnarmed(LivingEntity caster) {
        return CombatUtils.isUnarmed(caster.getHeldItemMainhand(), caster);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 7);
        }
        prev.flagCondition(false);
        return boundCast(prev, from, to);
    }

    public static class ReverseGrip extends Grapple {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        protected boolean isUnarmed(LivingEntity caster) {
            return true;
        }

        @Override
        public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
            if (to == STATE.COOLING) {
                setCooldown(caster, prev, 9);
            }
            prev.flagCondition(false);
            return boundCast(prev, from, to);
        }
    }

    public static class Suplex extends Grapple {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            caster.world.playSound(null, target.getPosX(), target.getPosY(), target.getPosZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            final ICombatCapability casterCap = CombatData.getCap(caster);
            float posture = casterCap.getPosture();
            target.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(-0.3)));
            target.velocityChanged = true;
            CombatData.getCap(target).consumePosture(caster, posture * 2, 0, true);
            casterCap.setPosture(0.1f);
        }
    }
}
