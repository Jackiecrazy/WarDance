package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

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
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    protected void evoke(LivingEntity caster) {
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(bigReach);
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).applyNonPersistentModifier(bigReach);

    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        return false;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(reach);
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(bigReach);
        CombatData.getCap(caster).setForcedSweep(-1);
        super.onEffectEnd(caster, stats);
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        final ModifiableAttributeInstance instance = caster.getAttribute(ForgeMod.REACH_DISTANCE.get());
        instance.removeModifier(reach);
        instance.applyPersistentModifier(reach);
        super.onCooledDown(caster, overflow);
    }

    @Override
    protected int getDuration(float might) {
        return (int)(3*might);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            double realReach = caster.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) - 10;
            if (((LivingAttackEvent) procPoint).getEntityLiving() == target) {
                double dist = Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target));
                if (dist > realReach)
                    stats.setDuration((float) (dist - realReach));
            }
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }
}
