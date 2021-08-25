package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Afflictions;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.Hand;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class ItchyCurse extends Hex {
    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && (!((ParryEvent) procPoint).canParry() || getTags(caster).contains(SkillTags.unblockable))) {
            procPoint.setCanceled(true);
            afflict(caster, target, 60);
            markUsed(caster);
        }
    }

    @Override
    public boolean statusTick(LivingEntity caster, LivingEntity target, SkillData sd) {
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
            Afflictions.getCap(target).removeStatus(this);
            return true;
        }
        return false;
    }

    @Override
    public void onStatusEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(HEX.getID());
        }
        super.onStatusEnd(caster, target, sd);
    }
}