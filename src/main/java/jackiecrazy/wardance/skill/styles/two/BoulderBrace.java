package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.UUID;

public class BoulderBrace extends WarCry {
    private static final AttributeModifier brace = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "boulder brace bonus", 3, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier brrrr = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "boulder brace bonus", 0.5, AttributeModifier.Operation.MULTIPLY_BASE);

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof StunEvent se && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (se.getEntity() == caster && CombatData.getCap(caster).consumeMight(1 / SkillUtils.getSkillEffectiveness(caster)))
                se.setCanceled(true);
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        boolean stationary = caster.zza == 0 && caster.xxa == 0 && caster.yya == 0;
        if (stationary) {
            SkillUtils.addAttribute(caster, FootworkAttributes.POSTURE_REGEN.get(), brace);
        } else SkillUtils.removeAttribute(caster, FootworkAttributes.POSTURE_REGEN.get(), brace);
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, FootworkAttributes.POSTURE_COOLDOWN.get(), brrrr);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.addAttribute(caster, FootworkAttributes.POSTURE_REGEN.get(), brace);
        SkillUtils.addAttribute(caster, FootworkAttributes.POSTURE_COOLDOWN.get(), brrrr);
        super.onUnequip(caster, stats);
    }

}
