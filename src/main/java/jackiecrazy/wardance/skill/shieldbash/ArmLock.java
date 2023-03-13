package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;

import java.awt.*;

public class ArmLock extends ShieldBash{
    @Override
    public Color getColor() {
        return Color.GREEN;
    }


    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(caster).consumeBarrier(Float.MAX_VALUE);
        final int time = CombatData.getCap(caster).getBarrierCooldown()/2;
        CombatData.getCap(caster).setBarrierCooldown(time);
        CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, time);
        CombatData.getCap(target).setHandBind(InteractionHand.OFF_HAND, time);
    }
}
