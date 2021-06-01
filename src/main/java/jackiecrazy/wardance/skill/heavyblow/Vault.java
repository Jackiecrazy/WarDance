package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class Vault extends HeavyBlow {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("67fe7ef7-a398-4c65-9bb1-42edaa80e7b1"), "lunge bonus", 2, AttributeModifier.Operation.ADDITION);

    @Override
    public boolean onCast(LivingEntity caster) {
        ForgeMod.REACH_DISTANCE.ifPresent((e) -> {

            Optional.ofNullable(caster.getAttribute(e)).ifPresent((a) -> {
                a.removeModifier(reach);
                a.applyPersistentModifier(reach);
            });
        });
        return super.onCast(caster);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        super.onEffectEnd(caster, stats);
        ForgeMod.REACH_DISTANCE.ifPresent((e) -> {
            Optional.ofNullable(caster.getAttribute(e)).ifPresent((a) -> {a.removeModifier(reach);});
        });
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        Vector3d extra = caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(-1);
        if (extra.lengthSquared() > 1) extra = extra.normalize();
        caster.setMotion(caster.getMotion().add(extra));
        caster.velocityChanged = true;
    }
}
