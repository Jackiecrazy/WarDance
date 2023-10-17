package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AllOrNothing extends SkillStyle {
    private static final UUID mad = UUID.fromString("abe24c38-1234-4551-9df4-e06e117699c1");

    public AllOrNothing() {
        super(3);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        double amount = caster.getAttributeValue(FootworkAttributes.MAX_FRACTURE.get());
        SkillUtils.modifyAttribute(caster, FootworkAttributes.MAX_FRACTURE.get(), mad, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        SkillUtils.modifyAttribute(caster, FootworkAttributes.MAX_POSTURE.get(), mad, (amount-1), AttributeModifier.Operation.MULTIPLY_TOTAL);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, FootworkAttributes.MAX_POSTURE.get(), mad);
        SkillUtils.removeAttribute(caster, FootworkAttributes.MAX_FRACTURE.get(), mad);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof SkillCastEvent sce && sce.getEntity() == caster) {
            if(sce.getSpirit()>0) {
                sce.setEffectiveness(sce.getEffectiveness() * (1 + 0.1f * CombatData.getCap(caster).getSpirit()));
                CombatData.getCap(caster).setSpirit(0);
            }
            if(sce.getMight()>0) {
                sce.setEffectiveness(sce.getEffectiveness() * (1 + 0.25f * CombatData.getCap(caster).getMight()));
                CombatData.getCap(caster).setMight(0);
            }
        }
    }
}
