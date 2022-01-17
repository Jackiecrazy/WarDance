package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
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
        if (CasterData.getCap(target).isCategoryActive(SkillCategories.iron_guard)) return;
        if (procPoint instanceof ParryEvent && stats.isCondition() && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isCategoryActive(SkillCategories.iron_guard)) return;
            CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 30);
            CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 30);
            ((ParryEvent) procPoint).setPostureConsumption(((ParryEvent) procPoint).getPostureConsumption() * 2);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            stats.flagCondition(((CriticalHitEvent) procPoint).isVanillaCritical());
            ((CriticalHitEvent) procPoint).setDamageModifier(1.3f);
        }
    }
}
