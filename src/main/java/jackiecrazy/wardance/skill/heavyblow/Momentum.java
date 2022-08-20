package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Momentum extends HeavyBlow {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getDefendingHand() != null && procPoint.getPhase() == EventPriority.HIGHEST && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).getCategoryState(SkillCategories.iron_guard) == STATE.ACTIVE) return;
            ((ParryEvent) procPoint).setPostureConsumption(((ParryEvent) procPoint).getPostureConsumption() + 0.01f * power(2, CombatData.getCap(caster).getComboRank()));
        } else if (procPoint instanceof CriticalHitEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            float combo = stats.getArbitraryFloat() + 1;
            combo %= 8 - CombatData.getCap(caster).getComboRank();
            if (combo == 0) {
                procPoint.setResult(Event.Result.ALLOW);
            } else procPoint.setResult(Event.Result.DENY);
            stats.setArbitraryFloat(combo);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() + 0.005f * power(2, CombatData.getCap(caster).getComboRank()));
        }
    }

    private float power(float base, int to) {
        float fin = 1;
        while (to != 0) {
            fin *= base;
            to--;
        }
        return fin;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }
}
