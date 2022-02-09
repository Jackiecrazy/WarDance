package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Kick extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "noDamage", "boundCast", "normalAttack", "countdown", "rechargeWithAttack")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return offensivePhysical;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 3;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        return CombatData.getCap(caster).getSpirit() < spiritConsumption(caster) ? CastStatus.SPIRIT : super.castingCheck(caster);
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.kick;
    }

    protected void additionally(LivingEntity caster, LivingEntity target) {
        final ICombatCapability cap = CombatData.getCap(target);
        if (cap.getStaggerCount() > 0) {
            cap.setStaggerTime(cap.getStaggerTime() + CombatConfig.staggerDuration);
            cap.setStaggerCount(cap.getStaggerCount() + CombatConfig.staggerHits);
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        attackCooldown(procPoint, caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE) {
            LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
            if (target != null && CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster))) {
                CombatData.getCap(target).consumePosture(caster, 4);
                if (caster instanceof PlayerEntity)
                    ((PlayerEntity) caster).spawnSweepParticles();
                target.attackEntityFrom(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                if (target.getRevengeTarget() == null)
                    target.setRevengeTarget(caster);
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                additionally(caster, target);
                markUsed(caster);
            }
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, 4);
            return true;
        }
        return boundCast(prev, from, to);
    }

    protected int distance() {
        return 3;
    }

    public static class Backflip extends Kick {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        protected void additionally(LivingEntity caster, LivingEntity target) {
            final Vector3d vec = caster.getPositionVec().subtractReverse(target.getPositionVec());
            final Vector3d noy = new Vector3d(vec.x, 0, vec.z).normalize().scale(-1);
            caster.setMotion(caster.getMotion().add(noy.x, 0.4, noy.z));
            caster.velocityChanged = true;
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addRank(0.4f);
            cap.addPosture(0.3f * (cap.getPosture() / cap.getMaxPosture()));
        }
    }
}
