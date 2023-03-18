package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class Submission extends Grapple {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntity() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (state == STATE.HOLSTERED && (caster.tickCount - caster.getLastHurtMobTimestamp() < 40 || caster.getArmorValue() > target.getArmorValue()) && CombatUtils.isUnarmed(caster.getMainHandItem(), caster) && caster.getLastHurtMob() == target && cast(caster, target, -999)) {
                performEffect(caster, target);
                stats.flagCondition(caster.getArmorValue() > target.getArmorValue());
            } else if (state == STATE.COOLING) stats.decrementDuration();
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, prev.isCondition() ? 4 : 7);
        }
        prev.flagCondition(false);
        return boundCast(prev, from, to);
    }
}
