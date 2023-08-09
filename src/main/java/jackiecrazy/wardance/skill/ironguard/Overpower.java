package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;

public class Overpower extends IronGuard {

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (state == STATE.COOLING) return;
        CombatData.getCap(procPoint.getAttacker()).consumePosture(caster, CombatUtils.getPostureAtk(caster, target, procPoint.getDefendingHand(), procPoint.getAttackDamage(), procPoint.getDefendingStack()) * stats.getEffectiveness());
        CombatData.getCap(procPoint.getAttacker()).consumePosture(caster, procPoint.getPostureConsumption() * stats.getEffectiveness());
        markUsed(caster);
    }


    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        final ICombatCapability cap = CombatData.getCap(caster);
        if (stats.getDuration() > 0.1 || (cap.getPosture() == cap.getMaxPosture()))
            return cooldownTick(stats);
        return false;
    }
}
