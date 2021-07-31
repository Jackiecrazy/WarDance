package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

public class Petrify extends Hex {
    private static final AttributeModifier NOPE = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1"), "hex", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier ARMOR = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1"), "hex", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onStatusEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance absorption = target.getAttribute(WarAttributes.ABSORPTION.get());
        if (absorption != null) {
            absorption.removeModifier(NOPE);
        }
        final ModifiableAttributeInstance deflection = target.getAttribute(WarAttributes.ABSORPTION.get());
        if (deflection != null) {
            deflection.removeModifier(NOPE);
        }
        final ModifiableAttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR);
        }
        super.onStatusEnd(caster, target, sd);
    }

    @Override
    public SkillData onStatusAdd(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        final ModifiableAttributeInstance absorption = target.getAttribute(WarAttributes.ABSORPTION.get());
        final ModifiableAttributeInstance deflection = target.getAttribute(WarAttributes.DEFLECTION.get());
        final ModifiableAttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        double shatter = 0;
        if (absorption != null) {
            absorption.removeModifier(NOPE);
            absorption.applyPersistentModifier(NOPE);
            shatter += absorption.getValue();
        }
        if (deflection != null) {
            deflection.removeModifier(NOPE);
            deflection.applyPersistentModifier(NOPE);
            shatter += deflection.getValue();
        }
        if (armor != null) {
            armor.removeModifier(ARMOR);
            armor.applyPersistentModifier(ARMOR);
            shatter += armor.getValue();
        }
        CombatData.getCap(target).setShatterCooldown((int) shatter);
        return super.onStatusAdd(caster, target, sd, existing);
    }
}
