package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.advancement.WarAdvancements;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.SkillResourceEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
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
    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {

    }

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (sd.getDuration() < 1)
            return CastStatus.OTHER;
        return super.castingCheck(caster, sd);
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            stats.decrementDuration(0.20f);
            if (stats.getDuration() - Math.floor(stats.getDuration()) < 0.2) {
                if (!kick(caster, stats)) {
                    //kick failed, return to inactive
                    stats.setState(STATE.INACTIVE);
                    stats.setArbitraryFloat(0);
                    stats.setMaxDuration(7);
                }
                stats.setDuration((float) Math.floor(stats.getDuration()));
                return true;
            }
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

    private boolean kick(LivingEntity caster, SkillData stats) {
        LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
        if (target != null) {
            stats.addArbitraryFloat(1);
            SkillResourceEvent sre = new SkillResourceEvent(caster, target, this);
            SkillCastEvent sce = new SkillCastEvent(caster, target, this, SkillUtils.getSkillEffectiveness(caster), 0, 0, 0, false, stats.getArbitraryFloat());
            if (stats.getArbitraryFloat() != 0) {
                MinecraftForge.EVENT_BUS.post(sre);
                if (sre.isCanceled()) return false;
                sce = initializeCast(caster, target, SkillUtils.getSkillEffectiveness(caster), 0, 0, 0, false, stats.getArbitraryFloat());
                MinecraftForge.EVENT_BUS.post(sce);
            }
            float mult = 1;
            float stack = stats.getArbitraryFloat();
            while (stack > 0) {
                mult *= sce.getEffectiveness();
                stack--;
            }
            stats.setEffectiveness(mult);
            CombatData.getCap(target).consumePosture(caster, 2 * mult);
            target.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setProcNormalEffects(false).setProcAttackEffects(true).setKnockbackPercentage(0.4f), mult);
            if (target.getLastHurtByMob() == null)
                target.setLastHurtByMob(caster);
            if (caster instanceof ServerPlayer sp)
                WarAdvancements.SKILL_CAST_TRIGGER.trigger(sp, target, stats);
            if (stats.getArbitraryFloat() >= 7) {
                ParticleUtils.playBonkParticle(caster.level(), caster.getEyePosition().add(caster.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)))), 1.5, 0, 12, getColor());
                caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.5f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                return false;
            } else {
                ParticleUtils.playBonkParticle(caster.level(), caster.getEyePosition().add(caster.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) * 0.85)), 0.4, 0.3, 4, getColor());
                caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            }
            return true;
        }
        return false;
    }
}
