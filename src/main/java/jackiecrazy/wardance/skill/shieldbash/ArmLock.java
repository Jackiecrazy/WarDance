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
        CombatData.getCap(caster).setBarrier(Float.MAX_VALUE);
        final int time = CombatData.getCap(caster).getBarrierCooldown()/2;
        CombatData.getCap(caster).setBarrierCooldown(time);
        CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, time);
        CombatData.getCap(target).setHandBind(Hand.OFF_HAND, time);
    }
}
