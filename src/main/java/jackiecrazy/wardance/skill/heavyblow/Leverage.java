package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Leverage extends HeavyBlow {

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
        if (procPoint instanceof ParryEvent && stats.isCondition() && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isCategoryActive(SkillCategories.iron_guard)) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent && ((CriticalHitEvent) procPoint).isVanillaCritical() && CombatData.getCap(caster).consumeMight(mightConsumption(caster))) {
            Vector3d extra = caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(-1);
            if (extra.lengthSquared() > 1) extra = extra.normalize();
            caster.setMotion(caster.getMotion().add(extra));
            caster.velocityChanged = true;
            stats.flagCondition(true);
            ((CriticalHitEvent) procPoint).setDamageModifier(1 + (float) Math.max(2, Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) / 4f));
            markUsed(caster);
        }
    }
}
