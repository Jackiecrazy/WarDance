package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class Tackle extends Kick {
    private static final AttributeModifier reach=new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b1"), "tackle bonus", 2, AttributeModifier.Operation.ADDITION);
    @Override
    public boolean onCast(LivingEntity caster) {
        ForgeMod.REACH_DISTANCE.ifPresent((e)->{Optional.ofNullable(caster.getAttribute(e)).ifPresent((a)->{a.applyPersistentModifier(reach);});});
        return super.onCast(caster);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        super.onEffectEnd(caster, stats);
        ForgeMod.REACH_DISTANCE.ifPresent((e)->{Optional.ofNullable(caster.getAttribute(e)).ifPresent((a)->{a.removeModifier(reach);});});
    }

    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        caster.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(0.18)));
        CombatData.getCap(caster).setRollTime(-10);
        caster.velocityChanged=true;
    }
}
