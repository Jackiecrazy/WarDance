package jackiecrazy.wardance.skill.shieldbash;

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
    public boolean onCast(LivingEntity caster) {
        CombatUtils.setHandCooldown(caster, Hand.MAIN_HAND, 1, true);
        CombatUtils.setHandCooldown(caster, Hand.OFF_HAND, 1, false);
        caster.addPotionEffect(new EffectInstance(Effects.HASTE, 30, 2));
        return super.onCast(caster);
    }

    @Override
    public void performEffect(LivingEntity caster, LivingEntity target) {
        CombatUtils.setHandCooldown(caster, Hand.MAIN_HAND, 1, true);
        CombatUtils.setHandCooldown(caster, Hand.OFF_HAND, 1, false);
        caster.addPotionEffect(new EffectInstance(Effects.HASTE, 30, 2));
    }
}
