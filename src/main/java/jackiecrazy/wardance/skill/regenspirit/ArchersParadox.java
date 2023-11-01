package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.skill.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class ArchersParadox extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1.5s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.morale;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData d) {
        return cooldownTick(d);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (stats.isCondition()) return;
        if (procPoint instanceof ProjectileImpactEvent pie && procPoint.getPhase() == EventPriority.HIGHEST) {
            CombatData.getCap(caster).addSpirit(1);
            markUsed(caster, true);
        } else if (procPoint instanceof EntityAwarenessEvent && ((EntityAwarenessEvent) procPoint).getAttacker() == caster && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (((EntityAwarenessEvent) procPoint).getAwareness() != StealthUtils.Awareness.ALERT) {
                CombatData.getCap(caster).addSpirit(1);
                markUsed(caster, true);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 3);
        if (to == STATE.INACTIVE && from == STATE.COOLING && prev.getDuration() <= 0) {
            prev.setState(STATE.ACTIVE);//so it shows up
            prev.setDuration(0);
            prev.setMaxDuration(0);
            return true;
        }
        return false;
    }
}
