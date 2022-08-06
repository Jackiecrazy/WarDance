package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

public class Petrify extends Hex {
    private static final AttributeModifier SPEED = new AttributeModifier(UUID.fromString("d6bf16c6-b548-4253-a00c-2361d243bdb4"), "hex", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    protected int duration() {
        return 100;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED);
        }
        target.addEffect(new EffectInstance(WarEffects.PETRIFY.get(), 60));
        super.onMarkEnd(caster, target, sd);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        final ModifiableAttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED);
            speed.addPermanentModifier(SPEED);
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingDamageEvent && ((LivingDamageEvent) procPoint).getEntityLiving() == target && target.hasEffect(WarEffects.PETRIFY.get())) {
            //create dust cloud and end petrify
            SkillUtils.createCloud(caster.level, caster, target.getX(), target.getY(), target.getZ(), 7, ParticleTypes.LARGE_SMOKE);
            for (LivingEntity entity : target.level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBoxForCulling().inflate(7), a->!TargetingUtils.isAlly(a, caster))) {
                CombatData.getCap(entity).consumePosture(5);
                target.addEffect(new EffectInstance(WarEffects.ENFEEBLE.get(), 60));
            }
            target.removeEffect(WarEffects.PETRIFY.get());
        }
        super.onProc(caster, procPoint, state, stats, target);
    }
}
