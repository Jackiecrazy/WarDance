package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

public class BoulderBrace extends WarCry {
    public static final UUID uid = UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1");
    public static final UUID sprinting = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");//this is the sprinting speed UUID so it'll remove sprinting bonus
    private static final AttributeModifier brace = new AttributeModifier(uid, "boulder brace bonus", 2, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier slow = new AttributeModifier(uid, "boulder brace debuff", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier fast = new AttributeModifier(uid, "boulder brace buff", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        //TODO doesn't do fist crits?????
        final ICombatCapability cap = CombatData.getCap(caster);
//        if (procPoint instanceof MeleePostureEvent.Defense pe && procPoint.getPhase() == EventPriority.HIGHEST && pe.getDamageSource() instanceof CombatDamageSource cds && cds.isCrit()) {
//            pe.setPostureConsumption(pe.getPostureConsumption() + (cap.getPostuetre() * SkillUtils.getSkillEffectiveness(caster) * cds.getCritDamage() / 2));
//            cap.consumePosture(cap.getPosture() / 2);
//
//            if (caster.level() instanceof ServerLevel s)
//                for (int reps = 0; reps < 100; reps++) {
//                    Vec3 startAt = target.position().add((((target.tickCount+reps) * 5) % target.getBbWidth()) - target.getBbWidth() / 2, (((target.tickCount+reps) * 31) % target.getBbHeight()), (((target.tickCount+reps) * 17) % target.getBbWidth()) - target.getBbWidth() / 2);
//                    Vec3 move = startAt.subtract(target.position()).normalize();
//                    s.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()).setPos(target.blockPosition()), startAt.x, startAt.y, startAt.z, 0, move.x, move.y, move.z, 1);
//                }
//
//        }
        if (procPoint instanceof PlayInteractionEvent.Pre) {
            boolean dusty = stats.getState() == STATE.ACTIVE || caster.fallDistance > 4;
            caster.setDeltaMovement(caster.getDeltaMovement().multiply(0, 1, 0));
            caster.hurtMarked = true;
            caster.resetFallDistance();
            beStill(caster, stats);
            if (!dusty) return;
            CombatData.getCap(caster).pin(10);
            final BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()).setPos(caster.blockPosition());
            SkillUtils.createCloud(caster.level(), caster, caster.getX(), caster.getY(), caster.getZ(), 7, dust);
            caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 0.45f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            //dust cloud explosion
            if (caster.level() instanceof ServerLevel s)
                for (int reps = 0; reps < 50; reps++) {
                    Vec3 startAt = caster.position().add(((reps * 5) % caster.getBbWidth()) - caster.getBbWidth() / 2, ((caster.tickCount * 31) % caster.getBbHeight()), ((caster.tickCount * 17) % caster.getBbWidth()) - caster.getBbWidth() / 2);
                    Vec3 move = startAt.subtract(caster.position()).normalize();
                    s.sendParticles(dust, startAt.x, startAt.y, startAt.z, 0, move.x, move.y, move.z, 10);
                }

            for (LivingEntity entity : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBoxForCulling().inflate(7), a -> !TargetingUtils.isAlly(a, caster))) {
                entity.addEffect(new MobEffectInstance(FootworkEffects.ENFEEBLE.get(), (int) (60 * SkillUtils.getSkillEffectiveness(caster))));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (60 * SkillUtils.getSkillEffectiveness(caster))));
            }
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        final Vec3 m = CombatData.getCap(caster).getMotionConsistently();
        if(m==null)return false;
        if(!StylishData.getCap(caster).isCombatMode()) {
            if(stats.getState()!=STATE.COOLING) {
                stats.setState(STATE.COOLING);
                SkillUtils.removeAttribute(caster, Attributes.MOVEMENT_SPEED, uid);
                startMoving(caster);
            }
            return true;
        }
        if(stats.getState()==STATE.COOLING)stats.setState(STATE.INACTIVE);
        if (m.lengthSqr()==0) {
            if(!stats.isCondition()) {
                beStill(caster, stats);
                stats.flagCondition(true);
            }
        } else if(stats.isCondition()) {
            startMoving(caster);
            stats.flagCondition(false);
        }
        if (caster.isSprinting()) {
            stats.flagCondition(false);
            stats.addDuration(1);
            //slowly accelerate
            final float rollin = Math.min(60,stats.getDuration());
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, uid, (-0.3 + (rollin / 60)), AttributeModifier.Operation.MULTIPLY_TOTAL);
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, sprinting, 0.3 * (rollin / 60), AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (rollin == 30) {
                stats.setState(STATE.ACTIVE);
            } else if (rollin > 30) {
                //aoe hit aura
                CombatData.getCap(caster).consumePosture(0.2f, 1);
                CombatData.getCap(caster).setGuardTime(6);
                for (LivingEntity e : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().expandTowards(m).inflate(1+rollin/60))) {
                    if (!TargetingUtils.isAlly(e, caster)) {
                        e.hurt(new CombatDamageSource(caster).setDamageTyping(FootworkDamageArchetype.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(rollin/30).setAttackingHand(null).setSkillUsed(this), rollin / 15);
                    }
                }
            }
        } else if (stats.getState() == STATE.ACTIVE) {
            beStill(caster, stats);
            stats.flagCondition(true);
        }
        return super.equippedTick(caster, stats);
    }

    private void startMoving(LivingEntity caster) {
        SkillUtils.removeAttribute(caster, WarAttributes.MAX_RALLY.get(), uid);
        SkillUtils.removeAttribute(caster, WarAttributes.RALLY_REGEN.get(), uid);
        SkillUtils.removeAttribute(caster, WarAttributes.RALLY_GUARD.get(), uid);
        SkillUtils.removeAttribute(caster, WarAttributes.COMPOSURE.get(), uid);
        SkillUtils.removeAttribute(caster, Attributes.KNOCKBACK_RESISTANCE, uid);
        //SkillUtils.removeAttribute(caster, Attributes.MOVEMENT_SPEED, slow);
    }

    private void beStill(LivingEntity caster, SkillData stats) {
        SkillUtils.addAttribute(caster, WarAttributes.MAX_RALLY.get(), brace);
        SkillUtils.addAttribute(caster, WarAttributes.RALLY_REGEN.get(), brace);
        SkillUtils.addAttribute(caster, WarAttributes.RALLY_GUARD.get(), brace);
        SkillUtils.addAttribute(caster, WarAttributes.COMPOSURE.get(), brace);
        SkillUtils.removeAttribute(caster, Attributes.MOVEMENT_SPEED, sprinting);
        SkillUtils.addAttribute(caster, Attributes.MOVEMENT_SPEED, slow);
        stats.setState(STATE.INACTIVE);
        stats.setDuration(0);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, Attributes.MOVEMENT_SPEED, slow);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        startMoving(caster);
        SkillUtils.removeAttribute(caster, Attributes.MOVEMENT_SPEED, uid);
        super.onUnequip(caster, stats);
    }

}
