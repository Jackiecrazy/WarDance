package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Optional;
import java.util.UUID;

public class Lunge extends HeavyBlow {
    private static final AttributeModifier reach=new AttributeModifier(UUID.fromString("67fe7ef7-a398-4c65-9bb1-42edaa80e7b1"), "lunge bonus", 2, AttributeModifier.Operation.ADDITION);
    @Override
    public boolean onCast(LivingEntity caster, SkillData stats) {
        ForgeMod.REACH_DISTANCE.ifPresent((e)->{Optional.ofNullable(caster.getAttribute(e)).ifPresent((a)->{a.applyPersistentModifier(reach);});});
        return super.onCast(caster, stats);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        super.onEffectEnd(caster, stats);
        ForgeMod.REACH_DISTANCE.ifPresent((e)->{Optional.ofNullable(caster.getAttribute(e)).ifPresent((a)->{a.removeModifier(reach);});});
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        caster.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(0.1)));//TODO check scaling
    }
}
