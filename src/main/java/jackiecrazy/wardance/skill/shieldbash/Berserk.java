package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;

import java.awt.*;

public class Berserk extends ShieldBash {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void performEffect(LivingEntity caster, LivingEntity target) {
        super.performEffect(caster, target);
        CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, 1, true);
        CombatUtils.setHandCooldown(caster, InteractionHand.OFF_HAND, 1, false);
        caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, CombatData.getCap(caster).getBarrierCooldown(), 2));
    }
}
