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

public class Shatter extends HeavyBlow {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        //O(n^2) incoming!
        float buff = CombatData.getCap(caster).getCombo();
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isSkillActive(WarSkills.IRON_GUARD.get())) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), (int) (30 * buff));
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            procPoint.setResult(Event.Result.ALLOW);
            //((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * buff);
            markUsed(caster);
        }
    }
}
