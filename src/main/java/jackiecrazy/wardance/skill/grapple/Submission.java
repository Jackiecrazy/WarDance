package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
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
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
        if (procPoint instanceof LivingAttackEvent) {
            if ((caster.ticksExisted - caster.getLastAttackedEntityTime() < 60 - stats.getDuration() || caster.getTotalArmorValue() > target.getTotalArmorValue()) && CombatUtils.isUnarmed(caster.getHeldItemMainhand(), caster) && caster.getLastAttackedEntity() == target) {
                performEffect(caster, target);
                stats.flagCondition(caster.getTotalArmorValue() > target.getTotalArmorValue());
                markUsed(caster);
            }
        }
    }
}
