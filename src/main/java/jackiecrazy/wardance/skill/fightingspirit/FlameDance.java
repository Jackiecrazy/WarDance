package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class FlameDance extends FightingSpirit {
    private static final UUID attackSpeed = UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b4");
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoint.melee, ProcPoint.on_parry, ProcPoint.on_being_hurt, ProcPoint.modify_crit, ProcPoint.countdown, ProcPoint.recharge_time, ProcPoint.recharge_sleep)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList(ProcPoint.melee, ProcPoint.on_parry)));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, STATE state) {
        if (state != STATE.COOLING) {
            double newSpeed = Math.floor(CombatData.getCap(caster).getCombo()) * 0.05;
            final ModifiableAttributeInstance instance = caster.getAttribute(Attributes.ATTACK_SPEED);
            AttributeModifier am = instance.getModifier(attackSpeed);
            if (am == null || am.getAmount() != newSpeed) {
                instance.removeModifier(attackSpeed);
                instance.applyNonPersistentModifier(new AttributeModifier(attackSpeed, "flame dance bonus", newSpeed, AttributeModifier.Operation.ADDITION));
            }
        }
        return super.equippedTick(caster, state);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.ATTACK_SPEED).removeModifier(attackSpeed);
        CombatData.getCap(caster).setForcedSweep(-1);
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
        }
        else if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getEntityLiving() ==caster && stats.getArbitraryFloat() >= 1 && ((ParryEvent) procPoint).getPostureConsumption() > 0) {
            ((ParryEvent) procPoint).setPostureConsumption(0);
            stats.setArbitraryFloat(0);
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }
}
