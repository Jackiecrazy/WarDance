package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Clinch extends Grapple {
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            if (caster.ticksExisted - caster.getLastAttackedEntityTime() < 60 - stats.getDuration() && caster.getHeldItemMainhand().isEmpty() && caster.getLastAttackedEntity() == target) {
                performEffect(caster, target);
                markUsed(caster);
            } else {
                final boolean offhand = CombatData.getCap(caster).isOffhandAttack();
                CombatData.getCap(target).setHandBind(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, 40);
                if (caster.getHeldItemOffhand().isEmpty())
                    CombatData.getCap(target).setHandBind(offhand ? Hand.MAIN_HAND : Hand.OFF_HAND, 40);
            }
        }
    }
}
