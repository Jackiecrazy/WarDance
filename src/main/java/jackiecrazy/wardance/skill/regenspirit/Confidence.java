package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.RegenSpiritEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class Confidence extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
confidence: your spirit regeneration speed scales proportionally with how much spirit you have left, from 200% at full to 50% at empty
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
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof RegenSpiritEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            float posPerc = CombatData.getCap(caster).getSpirit() * SkillUtils.getSkillEffectiveness(caster) / CombatData.getCap(caster).getMaxSpirit();
            stats.setArbitraryFloat(posPerc);
            ((RegenSpiritEvent) procPoint).setQuantity(((RegenSpiritEvent) procPoint).getQuantity() * (0.5f + 1.5f * posPerc));
        }
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
