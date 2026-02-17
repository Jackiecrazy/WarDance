package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;

public class Overpower extends IronGuard {

    @Override
    protected void parry(LivingEntity caster, MeleePostureEvent.Defense procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (state == STATE.COOLING) return;
        final LivingEntity attacker = procPoint.getAttacker();
        if(attacker==null)return;
        CombatData.getCap(attacker).consumePosture(caster, CombatUtils.getPostureAtk(caster, target, procPoint.getDefendingHand(), null, procPoint.getAttackDamage(), procPoint.getDefendingStack()) * stats.getEffectiveness());
        CombatData.getCap(attacker).consumePosture(caster, procPoint.getPostureConsumption() * stats.getEffectiveness());
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
