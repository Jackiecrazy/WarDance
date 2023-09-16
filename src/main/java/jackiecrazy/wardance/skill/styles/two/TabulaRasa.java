package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class TabulaRasa extends ColorRestrictionStyle {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "tabula rasa bonus", 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
    public TabulaRasa() {
        super(2, false, SkillColors.white);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, FootworkAttributes.SKILL_EFFECTIVENESS.get(), reach);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, FootworkAttributes.SKILL_EFFECTIVENESS.get(), reach);
        super.onUnequip(caster, stats);
    }
}
