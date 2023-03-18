package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class Grapple extends Skill {
    private final HashSet<String> tag = makeTag("physical", "boundCast", "melee", "normalAttack", "countdown", "unarmed", "rechargeWithAttack");
    private final HashSet<String> unarm = makeTag(SkillTags.offensive, SkillTags.physical, SkillTags.unarmed);

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.grapple;
    }

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return unarm;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {
        caster.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        if (this==WarSkills.THROW.get()) CombatData.getCap(target).consumePosture(caster, 11, 0, true);
        else CombatData.getCap(target).consumePosture(caster, 7, 0, true);

    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntity() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (state == STATE.HOLSTERED && isUnarmed(caster)) {
                if (stats.isCondition() && caster.getLastHurtMob() == target && caster.tickCount-caster.getLastHurtMobTimestamp()<40 && cast(caster, target, -999)) {
                    performEffect(caster, target);
                } else{
                    stats.flagCondition(true);
                    caster.setLastHurtMob(target);
                }
            }
        }
        attackCooldown(procPoint, caster, stats);
    }

    protected boolean isUnarmed(LivingEntity caster) {
        return CombatUtils.isUnarmed(caster.getMainHandItem(), caster);
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

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            caster.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            final ICombatCapability casterCap = CombatData.getCap(caster);
            float posture = casterCap.getPosture();
            target.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).scale(-0.3)));
            target.hurtMarked = true;
            CombatData.getCap(target).consumePosture(caster, posture * 2, 0, true);
            casterCap.setPosture(0.1f);
        }
    }
}
