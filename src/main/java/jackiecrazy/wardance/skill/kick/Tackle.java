package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import java.awt.*;
import java.util.UUID;

public class Tackle extends Kick {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("ef889d62-a0bc-465b-b4af-64d279532d47"), "tackle bonus", 2, AttributeModifier.Operation.ADDITION);

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public void additionally(LivingEntity caster, LivingEntity target) {
        caster.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(0.18)));
        CombatData.getCap(caster).setRollTime(-10);
        caster.velocityChanged = true;
    }

    @Override
    protected int distance() {
        return 5;
    }
}
