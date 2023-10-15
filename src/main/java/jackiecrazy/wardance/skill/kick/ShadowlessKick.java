package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class ShadowlessKick extends Kick {

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target != null) {
            if (cast(caster, target, prev.getDuration())) {
                prev.setMaxDuration(7);
                kick(caster, prev);
                prev.decrementDuration();
            }
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setArbitraryFloat(0);
            prev.setDuration((float) Math.max(0, Math.floor(prev.getDuration())));
            prev.setMaxDuration(7);
            return true;
        }
        return boundCast(prev, from, to);
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if(CasterData.getCap(caster).getSkillData(this).isEmpty())
            return CastStatus.OTHER;
        return super.castingCheck(caster);
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
            float mult = 1;
            mult *= sce.getEffectiveness();
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
            if (stats.getArbitraryFloat() >= 7) {
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
            stats.decrementDuration(0.20f);
            System.out.println(stats.getDuration());
            if (stats.getDuration() - Math.floor(stats.getDuration()) < 0.2) {
                if (!kick(caster, stats)) {
                    //kick failed, return to inactive
                    stats.setState(STATE.INACTIVE);
                    stats.setDuration((int) stats.getDuration());
                    stats.setArbitraryFloat(0);
                    stats.setMaxDuration(7);
                }
            }
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    protected void attackCooldown(Event e, LivingEntity caster, SkillData stats) {
        if (e instanceof LivingAttackEvent && ((LivingAttackEvent) e).getEntity() != caster && (stats.getState() == STATE.INACTIVE || stats.getState() == STATE.HOLSTERED) && e.getPhase() == EventPriority.HIGHEST) {
            stats.setDuration(Math.min(7, stats.getDuration() + 1));
            stats.markDirty();
            stats.setMaxDuration(7);
        }
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }
}
