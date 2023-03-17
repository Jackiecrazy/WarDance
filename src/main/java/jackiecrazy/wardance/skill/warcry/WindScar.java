package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.HashSet;
import java.util.UUID;

public class WindScar extends WarCry {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1, AttributeModifier.Operation.ADDITION);
    private static final UUID bigReach = UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c3");
    private final HashSet<String> tag = makeTag("chant", ProcPoints.on_being_hurt, ProcPoints.melee, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = makeTag((("sweep")));

    @Override
    protected void evoke(LivingEntity caster) {
        caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).removeModifier(bigReach);
        caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).addTransientModifier(new AttributeModifier(bigReach, "wind scar active bonus", CombatData.getCap(caster).getMight(), AttributeModifier.Operation.ADDITION));
        super.evoke(caster);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).addPermanentModifier(reach);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).removeModifier(reach);
        caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).removeModifier(bigReach);
        super.onUnequip(caster, stats);
    }

    @Override
    protected int getDuration(float might) {
        return (int) (3 * might);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE && ((LivingAttackEvent) procPoint).getEntity() == target) {
            double realReach = caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get()) - stats.getArbitraryFloat();
            double dist = Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target));
            if (dist > realReach)
                stats.decrementDuration((float) (dist - realReach));
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    protected boolean cast(LivingEntity caster, float duration) {
        return cast(caster, null, duration, false, CombatData.getCap(caster).getMight());
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).removeModifier(bigReach);
        }
        return super.onStateChange(caster, prev, from, to);
    }
}
