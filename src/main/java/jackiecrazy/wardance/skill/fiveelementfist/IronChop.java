package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class IronChop extends FiveElementFist {
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ParryEvent e && e.getEntity() == caster && e.canParry() && procPoint.getPhase() == EventPriority.HIGHEST && stats.isCondition()) {
            e.setPostureConsumption(0);
            stats.flagCondition(false);
        }
        if (procPoint instanceof LivingHurtEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() != caster && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            mark(caster, target, 1);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)//swap in
            prev.flagCondition(true);
        return super.onStateChange(caster, prev, from, to);
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        target.setDeltaMovement(target.getDeltaMovement().add(0, -2, 0));
        target.fallDistance += 5 * SkillUtils.getSkillEffectiveness(caster);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        removeMark(target);
        target.setDeltaMovement(target.getDeltaMovement().add(0, -1 * SkillUtils.getSkillEffectiveness(caster), 0));
        target.hurtMarked = true;
        return super.markTick(caster, target, sd);
    }
}
