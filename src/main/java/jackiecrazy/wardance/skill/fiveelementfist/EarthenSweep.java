package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class EarthenSweep extends FiveElementFist {

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ConsumePostureEvent e && procPoint.getPhase() == EventPriority.HIGHEST && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) && CombatData.getCap(e.getEntity()).getHandBind(InteractionHand.MAIN_HAND) != 0 && e.getAttacker() == caster) {
            e.setAmount(e.getAmount() * 2);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(caster).setForcedSweep(40);
        CombatUtils.sweep(caster, target, InteractionHand.MAIN_HAND, 3);
    }
}