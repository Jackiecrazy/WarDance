package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class ArmLock extends ShieldBash {
    @Override
    protected float performEffect(LivingEntity caster, LivingEntity target, float atk) {
        mark(caster, target, 10, 0);
        return atk;
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        boolean mainShield = WeaponStats.isShield(caster, InteractionHand.MAIN_HAND) && CombatUtils.getCooledAttackStrength(caster, InteractionHand.MAIN_HAND, 0.5f) < 1;
        boolean offShield = WeaponStats.isShield(caster, InteractionHand.OFF_HAND) && CombatUtils.getCooledAttackStrength(caster, InteractionHand.MAIN_HAND, 0.5f) < 1;
        if (mainShield || offShield)
            CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, 2);
        else removeMark(target);
        return markTickDown(sd);
    }
}
