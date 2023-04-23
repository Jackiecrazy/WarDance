package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class Morale extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to negate spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    private final HashSet<String> tag = makeTag("passive", ProcPoints.on_parry, ProcPoints.modify_crit);
    private final HashSet<String> no = none;

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.morale;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent&&procPoint.getPhase()== EventPriority.HIGHEST && ((CriticalHitEvent) procPoint).getEntity() == caster) {
            if (DamageUtils.isCrit((CriticalHitEvent) procPoint))
                CombatData.getCap(caster).addSpirit(1 / (float) GeneralUtils.getAttributeValueHandSensitive(caster, Attributes.ATTACK_SPEED, CombatData.getCap(caster).isOffhandAttack() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
        } else if (procPoint instanceof ParryEvent&&procPoint.getPhase()== EventPriority.HIGHEST && ((ParryEvent) procPoint).getEntity() == caster) {
            if (((ParryEvent) procPoint).canParry())
                CombatData.getCap(caster).addSpirit(1 / (float) GeneralUtils.getAttributeValueHandSensitive(caster, Attributes.ATTACK_SPEED, ((ParryEvent) procPoint).getDefendingHand()));
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
