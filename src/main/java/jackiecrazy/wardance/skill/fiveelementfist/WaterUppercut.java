package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class WaterUppercut extends FiveElementFist {
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof EntityAwarenessEvent.Attack e && procPoint.getPhase() == EventPriority.HIGHEST && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) && e.getAttacker() == caster) {
            e.setAwareness(e.getOriginalAwareness() == StealthUtils.Awareness.ALERT ? StealthUtils.Awareness.DISTRACTED : StealthUtils.Awareness.UNAWARE);
        }
        if (procPoint instanceof LivingHurtEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() != caster && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            mark(caster, target, 1);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        removeMark(target);
        target.setDeltaMovement(0, 1, 0);
        target.hurtMarked = true;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        removeMark(target);
        target.setDeltaMovement(target.getDeltaMovement().add(0, 0.4, 0));
        target.hurtMarked = true;
        return super.markTick(caster, target, sd);
    }
}
