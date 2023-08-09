package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;

public class PoundOfFlesh extends MementoMori {
    private final HashSet<String> tag = makeTag(ProcPoints.melee, ProcPoints.on_hurt, ProcPoints.state, ProcPoints.on_being_parried);

    @Override
    public HashSet<String> getTags() {
        return special;
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

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setDuration(0);
        }
        if (to == STATE.HOLSTERED && cast(caster, CombatData.getCap(caster).getSpirit() * 100)) {
            CombatData.getCap(caster).setSpirit(0);
        }
        return instantCast(prev, from, to);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.ACTIVE) {
            final float amount = GeneralUtils.getMaxHealthBeforeWounding(caster) * 0.1f/stats.getEffectiveness();
            if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((ParryEvent) procPoint).canParry()) {
                caster.invulnerableTime = 0;
                caster.hurt(CombatDamageSource.causeSelfDamage(caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).bypassArmor().bypassMagic(), amount);
                ((ParryEvent) procPoint).setPostureConsumption(((ParryEvent) procPoint).getPostureConsumption() + CombatData.getCap(target).getMaxPosture() * 0.15f * stats.getEffectiveness());
            } else if (procPoint instanceof LivingHurtEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() != caster && (!(lhe.getSource() instanceof CombatDamageSource cds) || cds.getSkillUsed() != this)) {
                caster.invulnerableTime = 0;
                caster.hurt(CombatDamageSource.causeSelfDamage(caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).bypassArmor().bypassMagic(), amount);
                lhe.setAmount(((LivingHurtEvent) procPoint).getAmount() + GeneralUtils.getMaxHealthBeforeWounding(target) * 0.07f * stats.getEffectiveness());
            }
        }
    }
}
