package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class Throw extends Grapple {


    @Override
    public float spiritConsumption(LivingEntity caster) {
        return caster.getFirstPassenger() == null ? 1 : 0;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.HOLSTERED && isUnarmed(caster)) {
            if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() != caster && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
                if (caster.getFirstPassenger() != null)
                    lae.setCanceled(true);
                if (stats.isCondition() && caster.getLastHurtMob() == target && caster.tickCount - caster.getLastHurtMobTimestamp() < 40 && cast(caster, target, 10)) {
                    ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, caster.position(), 0, 1, getColor(), 0);
                    performEffect(caster, target, stats);
                } else {
                    stats.flagCondition(true);
                    caster.setLastHurtMob(target);
                }
            } else if (procPoint instanceof StunEvent se && se.getEntity() == target && se.getPhase() == EventPriority.LOWEST)
                performEffect(caster, target, stats);
        }
        attackCooldown(procPoint, caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.ACTIVE) {
            if (caster.getFirstPassenger() != null && cast(caster)) {
                //yeet!
                Entity yeet = caster.getFirstPassenger();//caster.level.getEntity((int) prev.getDuration());
                if (yeet != null) {
                    Entity rider = yeet.getVehicle();
                    yeet.stopRiding();
                    yeet.setPos(caster.getPosition(0.5f).add(0, +caster.getBbHeight() + 0.5f, 0));
                    if (caster instanceof ServerPlayer p && rider != null)
                        p.connection.send(new ClientboundSetPassengersPacket(rider));
                    yeet.setDeltaMovement(caster.getLookAngle().scale(1.5 * prev.getEffectiveness()));
                    yeet.hurtMarked = true;
                    if (yeet instanceof LivingEntity e) mark(caster, e, 5, 0, true);
                }
            } else {
                LivingEntity le = SkillUtils.aimLiving(caster);
                if (le != null && (le.isShiftKeyDown() || CombatData.getCap(le).isVulnerable())) {
                    //auto pickup for friendly throw
                    mark(caster, le, 100, SkillUtils.getSkillEffectiveness(caster));
                }
            }
        }
        if (to == STATE.COOLING) {
            if (prev.isCondition())
                prev.setState(STATE.HOLSTERED);
            else
                setCooldown(caster, prev, 7);
        }
        return boundCast(prev, from, to);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, SkillData stats) {
        caster.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        if (CombatData.getCap(target).consumePosture(caster, 7 * stats.getEffectiveness(), 0, true) < 0 || CombatData.getCap(target).isVulnerable()) {
            stats.setDuration(target.getId());
            mark(caster, target, 100, stats.getEffectiveness());
            stats.flagCondition(true);
        } else {
            stats.flagCondition(false);
        }
        markUsed(caster);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration(0.05f);
        Entity collide = GeneralUtils.collidingEntity(target);
        if (sd.isCondition() && (target.horizontalCollision || target.verticalCollision || collide != null)) {
            removeMark(target);
            if (collide != null) {
                collide.setDeltaMovement(target.getDeltaMovement());
                collide.hurtMarked = true;
            }
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            targetCollision(caster, target, sd, collide);
        }
        if (!sd.isCondition()) {
            if (!CombatData.getCap(target).isVulnerable() && !target.isShiftKeyDown()) {
                removeMark(target);
                setCooldown(caster, CasterData.getCap(caster).getSkillData(this).get(), 7);
            }
        }
        return super.markTick(caster, target, sd);
    }

    protected void targetCollision(LivingEntity caster, LivingEntity target, SkillData sd, Entity collide) {
        if (caster != null) {
            target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setProcAttackEffects(true), 10 * sd.getArbitraryFloat() * sd.getArbitraryFloat());
            if (collide instanceof LivingEntity elb) {
                CombatData.getCap(elb).consumePosture(caster, 7 * sd.getArbitraryFloat() * sd.getArbitraryFloat());
                elb.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setProcAttackEffects(true), 3 * sd.getArbitraryFloat() * sd.getArbitraryFloat());
            }
        }
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (!sd.isCondition()) {
            target.startRiding(caster, true);
            if (caster instanceof ServerPlayer p)
                p.connection.send(new ClientboundSetPassengersPacket(caster));
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        target.stopRiding();
        if (caster instanceof ServerPlayer p)
            p.connection.send(new ClientboundSetPassengersPacket(caster));

    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }
}
