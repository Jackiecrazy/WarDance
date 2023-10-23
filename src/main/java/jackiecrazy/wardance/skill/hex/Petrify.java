package jackiecrazy.wardance.skill.hex;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.UUID;

public class Petrify extends Hex {
    private static final AttributeModifier SPEED = new AttributeModifier(UUID.fromString("d6bf16c6-b548-4253-a00c-2361d243bdb4"), "hex", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (target.level() instanceof ServerLevel s)
            s.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()).setPos(target.blockPosition()), target.getX() + ((target.tickCount * 5) % target.getBbWidth()) - target.getBbWidth() / 2, target.getY() + ((target.tickCount * 31) % target.getBbHeight()), target.getZ() + ((target.tickCount * 17) % target.getBbWidth()) - target.getBbWidth() / 2, 0, 0, -0.1, 0.0, 1.0);
        //s.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()).setPos(target.blockPosition()), target.getX(), target.getY(), target.getZ(), (int) 2, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingHurtEvent && ((LivingHurtEvent) procPoint).getEntity() == target && target.hasEffect(FootworkEffects.PETRIFY.get())) {
            //create dust cloud and end petrify
            final BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()).setPos(target.blockPosition());
            SkillUtils.createCloud(caster.level(), caster, target.getX(), target.getY(), target.getZ(), 7, dust);
            //dust cloud explosion
            if (target.level() instanceof ServerLevel s)
                for (int reps = 0; reps < 50; reps++) {
                    Vec3 startAt = target.position().add(((reps * 5) % target.getBbWidth()) - target.getBbWidth() / 2, ((target.tickCount * 31) % target.getBbHeight()), ((target.tickCount * 17) % target.getBbWidth()) - target.getBbWidth() / 2);
                    Vec3 move = startAt.subtract(target.position()).normalize();
                    s.sendParticles(dust, startAt.x, startAt.y, startAt.z, 0, move.x, move.y, move.z, 10);
                }

            for (LivingEntity entity : target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBoxForCulling().inflate(7), a -> !TargetingUtils.isAlly(a, caster))) {
                CombatData.getCap(entity).consumePosture(5 * SkillUtils.getSkillEffectiveness(caster));
                entity.addEffect(new MobEffectInstance(FootworkEffects.ENFEEBLE.get(), (int) (60 * SkillUtils.getSkillEffectiveness(caster))));
            }
            target.removeEffect(FootworkEffects.PETRIFY.get());
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        SkillUtils.addAttribute(target, Attributes.MOVEMENT_SPEED, SPEED);
        SkillUtils.addAttribute(target, Attributes.FLYING_SPEED, SPEED);
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        SkillUtils.removeAttribute(target, Attributes.MOVEMENT_SPEED, SPEED);
        SkillUtils.removeAttribute(target, Attributes.FLYING_SPEED, SPEED);
        target.level().playSound(null, caster, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat(), 0.25f + WarDance.rand.nextFloat() * 0.25f);
        target.addEffect(new MobEffectInstance(FootworkEffects.PETRIFY.get(), 60));
        super.onMarkEnd(caster, target, sd);
    }

    @Override
    protected int duration() {
        return 7;
    }
}
