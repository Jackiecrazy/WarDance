package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class Pummel extends ShieldBash{
    protected void performEffect(LivingEntity caster, LivingEntity target) {
        final ICombatCapability cap = CombatData.getCap(caster);
        if (CombatUtils.isShield(caster, caster.getHeldItemOffhand()))
                SkillUtils.auxAttack(caster, target, new CombatDamageSource("player", caster).setProcNormalEffects(false).setProcAttackEffects(true).setProcSkillEffects(true).setAttackingHand(Hand.OFF_HAND).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setDamageDealer(caster.getHeldItemOffhand()), 0, cap.getBarrier());

    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return cooldownTick(stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 6);
        return boundCast(prev, from, to);
    }
    @Override
    public boolean isPassive(LivingEntity caster) {
        return true;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 0;
    }
}
