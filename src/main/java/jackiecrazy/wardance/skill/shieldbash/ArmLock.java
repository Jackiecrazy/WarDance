package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.capability.resources.CombatData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import java.awt.*;

public class ArmLock extends ShieldBash{
    @Override
    public Color getColor() {
        return Color.GREEN;
    }


    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 40);
        CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 40);
    }
}
