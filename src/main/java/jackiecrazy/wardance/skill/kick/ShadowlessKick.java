package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.SkillResourceEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class ShadowlessKick extends Kick {

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 2;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && kick(caster, prev)) {
            LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
            if (target != null && cast(caster, target, 5)) {
                kick(caster, prev);
            }
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 6);
            return true;
        }
        return boundCast(prev, from, to);
    }

    @Override
    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {

    }


    private boolean kick(LivingEntity caster, SkillData stats) {
        LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
        if (target != null) {
            SkillResourceEvent sre = new SkillResourceEvent(caster, target, this);
            SkillCastEvent sce = new SkillCastEvent(caster, target, this, SkillUtils.getSkillEffectiveness(caster), 0, 0, 0, false, stats.getArbitraryFloat());
            if (stats.getArbitraryFloat() != 0) {
                MinecraftForge.EVENT_BUS.post(sre);
                MinecraftForge.EVENT_BUS.post(sce);
            }
            float arb = stats.getArbitraryFloat();
            float mult = 1;
            while (arb > 0) {
                mult *= sce.getEffectiveness();
                arb--;
            }
            CombatData.getCap(target).consumePosture(caster, 2 * mult);
            if (caster instanceof Player && caster.level instanceof ServerLevel) {
                double d0 = (double) (-Mth.sin(caster.getYRot() * ((float) Math.PI / 180F)));
                double d1 = (double) Mth.cos(caster.getYRot() * ((float) Math.PI / 180F));
                //((ServerLevel) caster.level).sendParticles(ParticleTypes.EXPLOSION, caster.getX() + d0, caster.getY(0.5D), caster.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
            target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setProcNormalEffects(false).setProcAttackEffects(true).setKnockbackPercentage(0.4f), mult);
            if (target.getLastHurtByMob() == null)
                target.setLastHurtByMob(caster);
            stats.addArbitraryFloat(1);
            if (stats.getArbitraryFloat() >= 6) {
                markUsed(caster);
                ParticleUtils.playBonkParticle(caster.level, caster.getEyePosition().add(caster.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)))), 1.5, 0, 12, getColor());
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.5f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                return false;
            } else {
                ParticleUtils.playBonkParticle(caster.level, caster.getEyePosition().add(caster.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) * 0.85)), 0.4, 0.3, 4, getColor());
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
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
}
