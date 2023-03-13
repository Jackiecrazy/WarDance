package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Overpower extends IronGuard {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (state == STATE.COOLING) return;
        CombatData.getCap(procPoint.getAttacker()).consumePosture(caster, CombatUtils.getPostureAtk(caster, target, procPoint.getDefendingHand(), procPoint.getAttackDamage(), procPoint.getDefendingStack()));
        CombatData.getCap(procPoint.getAttacker()).consumePosture(caster, procPoint.getPostureConsumption());
        markUsed(caster);
    }


    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        final ICombatCapability cap = CombatData.getCap(caster);
        if (stats.getDuration() > 0.1 || (cap.getPosture() == cap.getMaxPosture()&& cap.getMaxBarrier()== cap.getBarrier()))
            return cooldownTick(stats);
        return false;
    }
}
