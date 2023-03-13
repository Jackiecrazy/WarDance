package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Clinch extends Grapple {
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
            if (state == STATE.HOLSTERED) {
                if (stats.isCondition() && CombatUtils.isUnarmed(caster.getMainHandItem(), caster) && caster.tickCount - caster.getLastHurtMobTimestamp() < 40 && caster.getLastHurtMob() == target && cast(caster, target, -999)) {
                    performEffect(caster, target);
                    markUsed(caster);
                } else {
                    stats.flagCondition(true);
                    final boolean offhand = CombatData.getCap(caster).isOffhandAttack();
                    CombatData.getCap(target).setHandBind(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, 40);
                    if (CombatUtils.isUnarmed(caster.getOffhandItem(), caster))
                        CombatData.getCap(target).setHandBind(offhand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 40);
                }
            } else if (state == STATE.COOLING) stats.decrementDuration();
        }
    }
}
