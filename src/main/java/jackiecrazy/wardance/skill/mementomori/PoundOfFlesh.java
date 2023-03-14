package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.SetTag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class PoundOfFlesh extends MementoMori {
    private final SetTag<String> tag = SetTag.create(new HashSet<>(Arrays.asList(ProcPoints.melee, ProcPoints.on_hurt, ProcPoints.state, ProcPoints.on_being_parried)));

    @Override
    public Color getColor() {
        return Color.RED;
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setDuration(0);
        }
        if (to == STATE.HOLSTERED&& cast(caster, CombatData.getCap(caster).getSpirit() * 40)) {
            CombatData.getCap(caster).setSpirit(0);
        }
        return instantCast(prev, from, to);
    }


    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return special;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.ACTIVE)
            if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((ParryEvent) procPoint).canParry()) {
                caster.invulnerableTime = 0;
                caster.hurt(CombatDamageSource.causeSelfDamage(caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).bypassArmor().bypassMagic(), GeneralUtils.getMaxHealthBeforeWounding(caster) * 0.05f);
                ((ParryEvent) procPoint).setPostureConsumption(((ParryEvent) procPoint).getPostureConsumption() + CombatData.getCap(target).getTrueMaxPosture() * 0.1f);
            } else if (procPoint instanceof LivingHurtEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingHurtEvent) procPoint).getEntityLiving() == target && (!(((LivingHurtEvent) procPoint).getSource() instanceof CombatDamageSource) || ((CombatDamageSource) ((LivingHurtEvent) procPoint).getSource()).getSkillUsed() != this)) {
                caster.invulnerableTime = 0;
                caster.hurt(CombatDamageSource.causeSelfDamage(caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).bypassArmor().bypassMagic(), GeneralUtils.getMaxHealthBeforeWounding(caster) * 0.05f);
                ((LivingHurtEvent) procPoint).setAmount(((LivingHurtEvent) procPoint).getAmount() + GeneralUtils.getMaxHealthBeforeWounding(target) * 0.05f);
            }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData d) {
        if (d.getState() == STATE.ACTIVE) {
            if (CombatData.getCap(caster).getSpirit() == CombatData.getCap(caster).getMaxSpirit())
                markUsed(caster);
            d.decrementDuration();
        }
        return super.equippedTick(caster, d);
    }
}
