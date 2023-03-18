package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.AttackMightEvent;
import jackiecrazy.footwork.event.DodgeEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class SpeedDemon extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }


    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.morale;
    }
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof DodgeEvent &&procPoint.getPhase()== EventPriority.HIGHEST) {
            CombatData.getCap(caster).setSpiritGrace(CombatData.getCap(caster).getSpiritGrace() / 2);
        } else if (procPoint instanceof AttackMightEvent &&procPoint.getPhase()== EventPriority.HIGHEST) {
            double spdiff = Math.sqrt(GeneralUtils.getSpeedSq(caster)) - Math.sqrt(GeneralUtils.getSpeedSq(target));
            if (spdiff < 0 || !Double.isFinite(spdiff)) spdiff = 0;
            CombatData.getCap(caster).addSpirit((float) Math.min(1, Math.sqrt(spdiff)));
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
