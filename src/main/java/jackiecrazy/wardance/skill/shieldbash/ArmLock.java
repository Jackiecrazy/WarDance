package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class ArmLock extends ShieldBash{
    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, float atk) {
        final int time = (int) (atk/2);
        CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, time);
        CombatData.getCap(target).setHandBind(InteractionHand.OFF_HAND, time);
    }
}
