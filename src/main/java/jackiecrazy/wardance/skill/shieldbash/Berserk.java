package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;

import java.awt.*;

public class Berserk extends ShieldBash {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void performEffect(LivingEntity caster, LivingEntity target) {
        super.performEffect(caster, target);
        CombatUtils.setHandCooldown(caster, Hand.MAIN_HAND, 1, true);
        CombatUtils.setHandCooldown(caster, Hand.OFF_HAND, 1, false);
        caster.addEffect(new EffectInstance(Effects.DIG_SPEED, CombatData.getCap(caster).getBarrierCooldown(), 2));
    }
}
