package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.RegenSpiritEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class Apathy extends Skill {
    private static final AttributeModifier sprint = new AttributeModifier(UUID.fromString("c992cf1a-c9c4-421b-b470-5cae6471b7e7"), "whatever", -6, AttributeModifier.Operation.ADDITION);

    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", SkillTags.on_kill, SkillTags.change_spirit)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return WarSkills.MORALE.get();
    }

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
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).applyPersistentModifier(sprint);
        activate(caster, 0);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
        if (stats.isCondition())
            CombatData.getCap(caster).setBurnout(stats.getArbitraryFloat());
        activate(caster, 0);
    }

    @Override
    public void onAdded(LivingEntity caster, SkillData stats) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).applyPersistentModifier(sprint);
    }

    @Override
    public void onRemoved(LivingEntity caster, SkillData stats) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
        if (stats.isCondition())
            CombatData.getCap(caster).setBurnout(stats.getArbitraryFloat());
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        d.setArbitraryFloat(d.getArbitraryFloat() + CombatData.getCap(caster).getBurnout());
        d.flagCondition(true);
        CombatData.getCap(caster).setBurnout(0);
        if (CombatData.getCap(caster).getSpiritGrace() == 0)
            CombatData.getCap(caster).setSpirit(CombatData.getCap(caster).getMaxSpirit());
        return super.activeTick(caster, d);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingDeathEvent) {
            CombatData.getCap(caster).addSpirit(3);
        } else if (procPoint instanceof RegenSpiritEvent) {
            ((RegenSpiritEvent) procPoint).setQuantity(((RegenSpiritEvent) procPoint).getQuantity() / 3);
        }
    }
}
