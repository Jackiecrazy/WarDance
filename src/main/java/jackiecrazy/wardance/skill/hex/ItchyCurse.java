package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.Hand;

import java.awt.*;

public class ItchyCurse extends Hex {
    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        boolean stationary = target.moveForward == 0 && target.moveStrafing == 0 && target.moveVertical == 0;
        if (stationary) {
            sd.decrementDuration();
        }
        if (target.ticksExisted % 20 == 0) {
            sd.setArbitraryFloat(sd.getArbitraryFloat() + 1);
            if (sd.getArbitraryFloat() >= 3) {
                SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX.getID(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
                CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 20);
                CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 20);
                sd.setArbitraryFloat(-1);
            } else if (sd.getArbitraryFloat() == 0) {
                SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX.getID(), 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
        }
        //SkillUtils.modifyAttribute(target, Attributes.ARMOR, HEX.getID(), -sd.getArbitraryFloat() * 2, AttributeModifier.Operation.ADDITION);
//            if (target.getTotalArmorValue() == 0 && sd.getArbitraryFloat() > 7) {
//                CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 60);
//                CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 60);
//                sd.setDuration(60);
//                SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX.getID(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
//            }
        if (sd.getDuration() <= 0) {
            Marks.getCap(target).removeMark(this);
            return true;
        }
        return false;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(HEX.getID());
        }
        super.onMarkEnd(caster, target, sd);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.ACTIVE && cast(caster, -999)) {
            LivingEntity e = SkillUtils.aimLiving(caster);
            if (e != null) {
                mark(caster, e, 60);
                markUsed(caster);
            }
        }
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 15);
        return boundCast(prev, from, to);
    }
}
