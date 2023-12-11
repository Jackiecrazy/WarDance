package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.skill.styles.two.WarCry;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class Lunge extends WarCry {
    /*
    +posture damage on standing
+crit damage on falling
+knockback on sneaking
+range on sprinting
+sweep on riding
     */
    private static final AttributeModifier brace = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "lunge bonus", 1, AttributeModifier.Operation.ADDITION);

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        boolean stationary = caster.isSprinting();
        if (stationary) {
            SkillUtils.addAttribute(caster, ForgeMod.ENTITY_REACH.get(), brace);
        } else SkillUtils.removeAttribute(caster, ForgeMod.ENTITY_REACH.get(), brace);
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.addAttribute(caster, ForgeMod.ENTITY_REACH.get(), brace);
        super.onUnequip(caster, stats);
    }

}
