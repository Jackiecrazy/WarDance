package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.event.ConsumePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class Acrobatics extends IronGuard {
    public static final UUID aerial = UUID.fromString("662A6B8D-DA3E-4C1C-2213-96EA6097278D");
    private static final AttributeModifier batics = new AttributeModifier(aerial, "acrobatics bonus", 0.4, AttributeModifier.Operation.ADDITION);
    @Override
    protected void parry(LivingEntity caster, ConsumePostureEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (procPoint.getType()!= ConsumePostureEvent.TYPE.NONE && state != STATE.COOLING && AerialModeData.getCap(caster).isAerialMode()&&procPoint.success()) {
//            float str = -Math.min(procPoint.getPostureConsumption() / 2, 2) * stats.getEffectiveness();
//            caster.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).normalize().scale(str)));
//            caster.hurtMarked = true;
            AerialModeData.getCap(caster).setAerialMode(60);
            CombatData.getCap(caster).setDodgeTime(-99999);//resets dodge
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if(AerialModeData.getCap(caster).isAerialMode())
            SkillUtils.addAttribute(caster, WarAttributes.COMPOSURE.get(), batics);
        else SkillUtils.removeAttribute(caster, WarAttributes.COMPOSURE.get(), batics);
        return super.equippedTick(caster, stats);
    }
}
