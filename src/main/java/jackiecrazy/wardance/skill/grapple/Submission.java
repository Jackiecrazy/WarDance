package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Submission extends Grapple {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        if(stats.isCondition())setCooldown(caster, 5);
        else super.onEffectEnd(caster, stats);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            if ((caster.ticksExisted - caster.getLastAttackedEntityTime() < 60 - stats.getDuration() || caster.getTotalArmorValue() > target.getTotalArmorValue()) && caster.getHeldItemMainhand().isEmpty() && caster.getLastAttackedEntity() == target) {
                performEffect(caster, target);
                stats.flagCondition(caster.getTotalArmorValue() > target.getTotalArmorValue());
                markUsed(caster);
            }
        }
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(target).consumePosture(10 + Math.max(0, target.getTotalArmorValue() - caster.getTotalArmorValue()));
    }
}
