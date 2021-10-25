package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class FlameDance extends WarCry {
    private static final UUID attackSpeed = UUID.fromString("338a5b6f-46c2-44b6-913f-f15c5e59cd48");
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoints.melee, ProcPoints.on_parry, ProcPoints.on_being_hurt, ProcPoints.modify_crit, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList(ProcPoints.melee, ProcPoints.on_parry)));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, STATE state) {
        if (state != STATE.COOLING) {
            double newSpeed = Math.floor(CombatData.getCap(caster).getCombo()) * 0.05;
            SkillUtils.modifyAttribute(caster, Attributes.ATTACK_SPEED, attackSpeed, newSpeed, AttributeModifier.Operation.ADDITION);
        }
        return super.equippedTick(caster, state);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.ATTACK_SPEED).removeModifier(attackSpeed);
        super.onEffectEnd(caster, stats);
    }

    protected int getDuration() {
        return 400;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        super.onCooledDown(caster, overflow);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            stats.setArbitraryFloat(stats.getArbitraryFloat() + 0.34f);
        }else if(procPoint instanceof CriticalHitEvent&&stats.getArbitraryFloat()>=1){
            procPoint.setResult(Event.Result.ALLOW);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier()*1.5f);
            stats.setArbitraryFloat(0);
            stats.flagCondition(true);
        }
        else if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getEntityLiving() ==caster && stats.isCondition() && ((ParryEvent) procPoint).getPostureConsumption() > 0) {
            ((ParryEvent) procPoint).setPostureConsumption(0);
            stats.flagCondition(false);
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }
}
