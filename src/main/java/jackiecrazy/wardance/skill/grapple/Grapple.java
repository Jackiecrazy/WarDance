package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

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
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return unarm;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 2;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 60);
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 7);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {
        caster.world.playSound(null, target.getPosX(), target.getPosY(), target.getPosZ(), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        if (getParentCategory() == null) CombatData.getCap(target).consumePosture(caster, 11, 0, true);
        else CombatData.getCap(target).consumePosture(caster, 8, 0, true);

    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
        if (procPoint instanceof LivingAttackEvent && CombatUtils.isUnarmed(caster.getHeldItemMainhand(), caster)) {
            if (stats.isCondition() && caster.getLastAttackedEntity() == target) {
                performEffect(caster, target);
                markUsed(caster);
            } else stats.flagCondition(true);
        }
    }

    public static class Reversal extends Grapple {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
            if (procPoint instanceof LivingAttackEvent && CombatUtils.isUnarmed(caster.getHeldItemMainhand(), caster)) {
                if (stats.isCondition()) {
                    Entity prev=target.world.getEntityByID((int) stats.getArbitraryFloat());
                    if(!(prev instanceof LivingEntity) || prev == target)prev=caster;
                    final ICombatCapability casterCap = CombatData.getCap((LivingEntity) prev);
                    float casterPerc = casterCap.getPosture() / casterCap.getMaxPosture();
                    final ICombatCapability targetCap = CombatData.getCap(target);
                    float targetPerc = targetCap.getPosture() / targetCap.getMaxPosture();
                    casterCap.setPosture(targetPerc * casterCap.getMaxPosture());
                    targetCap.setPosture(casterPerc * targetCap.getMaxPosture());
                    markUsed(caster);
                } else{
                    stats.flagCondition(true);
                    stats.setArbitraryFloat(target.getEntityId());
                }
            }
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
