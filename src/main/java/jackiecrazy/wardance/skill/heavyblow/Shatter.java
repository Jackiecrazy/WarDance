package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
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
        float buff = 1;
        int index = 0;
        Skill[] pastCasts = CasterData.getCap(caster).getPastCasts();
        for (Skill s : pastCasts) {
            boolean flag = true;
            for (int find = 0; find < index; find++) {
                if (pastCasts[find] == s) {
                    flag = false;
                    break;
                }
            }
            if (flag) buff += 0.2f;
            index++;
        }
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), (int) (30 * buff));
        } else if (procPoint instanceof CriticalHitEvent) {
            procPoint.setResult(Event.Result.ALLOW);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * buff);
        }
    }
}
