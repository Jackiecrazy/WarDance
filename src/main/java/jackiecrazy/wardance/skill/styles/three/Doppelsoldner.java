package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.event.SuppressOffhandEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Doppelsoldner extends SkillStyle {
    private static final UUID uid = UUID.fromString("abe24c38-1234-4551-9df4-e06f111699c1");

    public Doppelsoldner() {
        super(3);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_SPEED, uid, CombatData.getCap(caster).isCombatMode() ? 0.4 * SkillUtils.getSkillEffectiveness(caster) : 0, AttributeModifier.Operation.ADDITION);
        if (CombatData.getCap(caster).isCombatMode()) {
            CombatData.getCap(caster).setHandBind(InteractionHand.OFF_HAND, 60);
        }
        return false;
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.ATTACK_SPEED).removeModifier(uid);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if(procPoint instanceof SuppressOffhandEvent the){
            the.setResult(Event.Result.ALLOW);
        }
    }
}
