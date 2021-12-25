package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Stagger extends HeavyBlow {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (CasterData.getCap(target).isSkillActive(WarSkills.IRON_GUARD.get())) return;
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isSkillActive(WarSkills.IRON_GUARD.get())) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            procPoint.setResult(Event.Result.ALLOW);
            if (getParentSkill() == null)
                ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * 1.4f);
            markUsed(caster);
        }
    }
}
