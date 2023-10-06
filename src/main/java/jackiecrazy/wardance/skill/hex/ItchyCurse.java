package jackiecrazy.wardance.skill.hex;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.InteractionHand;

public class ItchyCurse extends Hex {

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster);
        if (to == STATE.ACTIVE && target != null && cast(caster, target, -999)) {
            mark(caster, target, 3);
            markUsed(caster);
            if (caster.level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY(), target.getZ(), 20, target.getBbWidth(), target.getBbHeight(), target.getBbWidth(), 0f);
            }
        }
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 15);
        return boundCast(prev, from, to);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        boolean stationary = target.zza == 0 && target.xxa == 0 && target.yya == 0;
        if (stationary) {
            markTickDown(sd);
        }
        sd.addArbitraryFloat(0.05f);
        if (sd.getArbitraryFloat() >= 3) {
            SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
            CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, 20);
            CombatData.getCap(target).setHandBind(InteractionHand.OFF_HAND, 20);
            sd.setArbitraryFloat(-1);
        } else if (sd.getArbitraryFloat() == 0) {
            SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
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
        return sd.getArbitraryFloat()%1==sd.getArbitraryFloat();
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(HEX);
        }
        super.onMarkEnd(caster, target, sd);
    }
}
