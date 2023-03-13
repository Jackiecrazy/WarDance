package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.SetTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Kick extends Skill {
    private final SetTag<String> tag = SetTag.create(new HashSet<>(Arrays.asList("physical", "melee", "noDamage", "boundCast", "normalAttack", "countdown", "rechargeWithAttack")));
    private final SetTag<String> no = SetTag.create(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public SetTag<String> getTags(LivingEntity caster) {
        return offensivePhysical;
    }

    @Nonnull
    @Override
    public SetTag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 3;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.kick;
    }

    protected void additionally(LivingEntity caster, LivingEntity target) {
        final ICombatCapability cap = CombatData.getCap(target);
        if (cap.getStaggerTime() > 0) {
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
        LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target!=null&& cast(caster, target, -999)) {
                CombatData.getCap(target).consumePosture(caster, 4);
                if (caster instanceof Player)
                    ((Player) caster).sweepAttack();
                additionally(caster, target);
                target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                if (target.getLastHurtByMob() == null)
                    target.setLastHurtByMob(caster);
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 4);
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
            final Vec3 vec = caster.position().vectorTo(target.position());
            final Vec3 noy = new Vec3(vec.x, 0, vec.z).normalize().scale(-1);
            caster.setDeltaMovement(caster.getDeltaMovement().add(noy.x, 0.4, noy.z));
            caster.hurtMarked = true;
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addRank(0.4f);
            cap.addPosture(0.3f * (cap.getPosture() / cap.getMaxPosture()));
        }
    }
}
