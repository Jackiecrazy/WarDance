package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class WindScar extends WarCry {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier bigReach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c3"), "wind scar active bonus", 10, AttributeModifier.Operation.ADDITION);
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoints.on_being_hurt, ProcPoints.melee, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("sweep")));

    @Override
    protected void evoke(LivingEntity caster) {
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(bigReach);
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).applyNonPersistentModifier(bigReach);
        super.evoke(caster);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).applyPersistentModifier(reach);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(reach);
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(bigReach);
        super.onUnequip(caster, stats);
    }

    @Override
    protected int getDuration(float might) {
        return (int) (3 * might);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
            double realReach = caster.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) - 10;
            if (((LivingAttackEvent) procPoint).getEntityLiving() == target) {
                double dist = Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target));
                if (dist > realReach)
                    stats.decrementDuration((float) (dist - realReach));
            }
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }
}
