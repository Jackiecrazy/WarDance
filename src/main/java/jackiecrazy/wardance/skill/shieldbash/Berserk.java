package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class Berserk extends ShieldBash {
    @Override
    public void performEffect(LivingEntity caster, LivingEntity target, float atk) {
        super.performEffect(caster, target, atk);
        CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, 1, true);
        CombatUtils.setHandCooldown(caster, InteractionHand.OFF_HAND, 1, false);
        caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, (int) (atk*40), 2));
    }
}
