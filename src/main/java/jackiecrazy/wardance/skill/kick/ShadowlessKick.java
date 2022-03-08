package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

import java.awt.*;

public class ShadowlessKick extends Kick {

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 4;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            stats.decrementDuration();
            if (stats.getDuration() == 0 && kick(caster, stats)) {
                stats.setDuration(5);
            }
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    protected void additionally(LivingEntity caster, LivingEntity target) {

    }


    private boolean kick(LivingEntity caster, SkillData stats) {
        LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
        if (target != null) {
            CombatData.getCap(target).consumePosture(caster, 2);
            if (caster instanceof PlayerEntity && caster.level instanceof ServerWorld) {
                double d0 = (double) (-MathHelper.sin(caster.yRot * ((float) Math.PI / 180F)));
                double d1 = (double) MathHelper.cos(caster.yRot * ((float) Math.PI / 180F));
                ((ServerWorld) caster.level).sendParticles(ParticleTypes.EXPLOSION, caster.getX() + d0, caster.getY(0.5D), caster.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
            target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcNormalEffects(false).setProcAttackEffects(true).setKnockbackPercentage(0.7f), 1);
            if (target.getLastHurtByMob() == null)
                target.setLastHurtByMob(caster);
            stats.setArbitraryFloat(stats.getArbitraryFloat() + 1);
            if (stats.getArbitraryFloat() >= 6) {
                markUsed(caster);
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 0.5f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                return false;
            }
            caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            return true;
        }
        return false;
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && kick(caster, prev)) {
            LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
            if (target != null && cast(caster, 5)) {
                kick(caster, prev);
            }
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 6);
            return true;
        }
        return boundCast(prev, from, to);
    }
}
