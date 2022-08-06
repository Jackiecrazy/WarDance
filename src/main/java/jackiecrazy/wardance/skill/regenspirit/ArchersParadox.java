package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.event.EntityAwarenessEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;

public class ArchersParadox extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1.5s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }


    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.morale;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData d) {
        if (d.isCondition())
            d.decrementDuration();
        if (d.getDuration() <= 0) {
            d.flagCondition(false);
            d.setDuration(30);
        }
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (stats.isCondition()) return;
        if (procPoint instanceof ProjectileImpactEvent&&procPoint.getPhase()== EventPriority.HIGHEST) {
            CombatData.getCap(caster).addSpirit(1);
            stats.flagCondition(true);
        } else if (procPoint instanceof EntityAwarenessEvent&&((EntityAwarenessEvent) procPoint).getAttacker()==caster&&procPoint.getPhase()== EventPriority.HIGHEST) {
            if (((EntityAwarenessEvent) procPoint).getAwareness() != StealthUtils.Awareness.ALERT) {
                CombatData.getCap(caster).addSpirit(1);
                stats.flagCondition(true);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
