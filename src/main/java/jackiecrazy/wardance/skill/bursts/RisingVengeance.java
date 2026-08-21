package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.event.DodgeEvent;
import jackiecrazy.footwork.event.GainAdrenalineEvent;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.skill.ExcaliburEntity;
import jackiecrazy.wardance.event.GrappleEvent;
import jackiecrazy.wardance.event.KickEvent;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.MobilityUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class RisingVengeance extends Skill {
    private static final AttributeModifier moveDown = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "final strike speed penalty", -0.6, AttributeModifier.Operation.MULTIPLY_TOTAL);
    final List<MotionFrame> FORWARD = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -1)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 0.5)));
    final MotionManager FW = new MotionManagers.DefinitionMM(new MotionGroup(FORWARD, EasingFunctionEnum.OUT_SINE, 13));
    final List<MotionFrame> UPP = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 0.5)),
            new MotionFrame(new Vec3(0, 1, 0), new Vec3(0, 0, 1)));
    final MotionManager UP = new MotionManagers.DefinitionMM(new MotionGroup(UPP, EasingFunctionEnum.OUT_SINE, 17));

    /*
    charge as long as you want after activating. Next swing's size and distance increases with time
     */

    @Override
    public HashSet<String> getTags() {
        return burst;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return burst;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (FlyingWeaponData.getCap(caster).getExtraWeapons("guardian").isEmpty()) {
                markUsed(caster);
                return true;
            }
            FlyingItemEntity fie = FlyingWeaponData.getCap(caster).getExtraWeapons("guardian").get(0);
            if (fie.isIdle()) {
                if (!StylishData.getCap(caster).drainAdrenaline(0.007f)) {
                    swing(caster, stats.getArbitraryFloat());
                    markUsed(caster);
                    return true;
                }
                CombatData.getCap(caster).addSpirit(0.05f);
                stats.addArbitraryFloat(0.007f * stats.getEffectiveness());
                FlyingWeaponData.getCap(caster).getExtraWeapons("guardian").forEach(a -> {
                    a.setEffect(FlyingWeaponEffect.BIG_SHADOW);
                    a.setInteractionRange(0.5f+10 * stats.getArbitraryFloat());
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (!StylishData.getCap(caster).maxAdrenaline()) return CastStatus.ADRENALINE;
        return super.castingCheck(caster, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        //nothing else matters
        if (procPoint instanceof LivingAttackEvent le) {
            if (le.getEntity() == caster && state == STATE.ACTIVE && !stats.getTargets().contains(le.getSource().getEntity())) {//attacked while active, ignore and act as if parried
                MobilityUtils.knockBack(target, caster, 1, true, true);
                le.setCanceled(true);
                return;
            } else if (le.getEntity() == target && le.getSource() instanceof CombatDamageSource cds && cds.getSkillUsed() == this && !stats.getTargets().contains(target)) {
                //this weapon, but not the nemesis. Cancel.
                le.setCanceled(true);
                return;
            }
        }
        if (state == STATE.ACTIVE) {
            if (procPoint instanceof DodgeEvent de)
                de.setCanceled(true);
            if (procPoint instanceof GrappleEvent de)
                de.setCanceled(true);
            if (procPoint instanceof KickEvent de)
                de.setCanceled(true);
            if (procPoint instanceof PlayInteractionEvent.Pre e && e.getPhase() == EventPriority.HIGHEST) {
                if (FlyingWeaponData.getCap(caster).getExtraWeapons("guardian").isEmpty()) {
                    markUsed(caster);
                    return;
                }
                //control excalibur to attack
                e.setCanceled(true);
                FlyingItemEntity fie = FlyingWeaponData.getCap(caster).getExtraWeapons("guardian").get(0);
                if (fie.isIdle()) {
                    swing(caster, stats.getArbitraryFloat());
                    markUsed(caster, true);
                }
            }
            if (procPoint instanceof GainAdrenalineEvent gme) {
                gme.setQuantity(0);
            }
        }
    }

    private void swing(LivingEntity caster, float chargeTime) {
        final List<MotionFrame> FRAME = List.of(
                new MotionFrame(new Vec3(0, 1, 0), new Vec3(0, 0, 1)).setEffects(new FrameEffects().setRange(chargeTime * 10).setEffects(FlyingWeaponEffect.BIG_SHADOW, FlyingWeaponEffect.TRAIL).setHit(new HitInfo(chargeTime * 3, chargeTime * 10, chargeTime * 10, false, true, 1).withDamageTags("wardance:no_spirit_code", "wardance:no_composure"))),
                new MotionFrame(new Vec3(0, 0, 1.2), new Vec3(0, 0, 1)));
        final MotionManager MM = new MotionManagers.DefinitionMM(new MotionGroup(FRAME, EasingFunctionEnum.IN_SINE, 20));

        FlyingWeaponData.getCap(caster).getExtraWeapons("guardian").forEach(a -> {
            a.queuePath(MM);
        });
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE) {
            LivingEntity le = SkillUtils.aimLiving(caster, 10);
            if (le != null && cast(caster, 7)) {
                le.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1));
                prev.clearTargets();
                prev.addTarget(le);
                CombatUtils.triggerSteveTime(caster, 30);
                SkillUtils.addAttribute(caster, Attributes.MOVEMENT_SPEED, moveDown);
                ExcaliburEntity awe = new ExcaliburEntity(WarEntities.EXCALIBUR.get(), caster.level());
                awe.setHeldItem(new ItemStack(WeaponStats.getGuardianForPlayer(caster)));
                awe.setOwner(caster);
                awe.setEffect(FlyingWeaponEffect.BIG_SHADOW);
                awe.setSkillUsed(this);
                awe.setInteractionRange(0.5f);
                awe.moveTo(caster.getEyePosition().add(caster.getLookAngle().reverse().scale(3)));
                awe.setGlowingTag(true);
                awe.queuePath(FW);
                awe.queuePath(UP);
                awe.setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 1, 0), new Vec3(0, 0, 1)), 2));
                awe.setUniversalOffset(new Vec3(0, 0, 1));
                caster.level().addFreshEntity(awe);
                FlyingWeaponData.getCap(caster).addExtraWeapon("guardian", awe);
                return true;
            }
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setArbitraryFloat(0);
            SkillUtils.removeAttribute(caster, Attributes.MOVEMENT_SPEED, moveDown);
            FlyingWeaponData.getCap(caster).dismissWeapons("guardian");
            prev.getTargets().forEach(a -> a.removeEffect(MobEffects.GLOWING));
            return true;
        }
        return boundCast(prev, from, to);
    }


}
