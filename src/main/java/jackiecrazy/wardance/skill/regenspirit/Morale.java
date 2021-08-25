package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;
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

    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", SkillTags.on_parry, SkillTags.modify_crit)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 0);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        activate(caster, 0);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof CriticalHitEvent) {
            if (CombatUtils.isCrit((CriticalHitEvent) procPoint))
                CombatData.getCap(caster).addSpirit(1 / (float) GeneralUtils.getAttributeValueHandSensitive(caster, Attributes.ATTACK_SPEED, CombatData.getCap(caster).isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND));
        } else if (procPoint instanceof ParryEvent) {
            if (((ParryEvent) procPoint).canParry())
                CombatData.getCap(caster).addSpirit(1 / (float) GeneralUtils.getAttributeValueHandSensitive(caster, Attributes.ATTACK_SPEED, ((ParryEvent) procPoint).getDefendingHand()));
        }
    }
}