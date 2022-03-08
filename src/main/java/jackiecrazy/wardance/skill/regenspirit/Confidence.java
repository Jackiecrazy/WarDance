package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.RegenSpiritEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.UUID;

import jackiecrazy.wardance.skill.Skill.STATE;

public class Confidence extends Skill {
    private static final AttributeModifier sprint = new AttributeModifier(UUID.fromString("0683fe69-5348-4a83-95d5-81a2eeb2cca0"), "gimli moment", 10, AttributeModifier.Operation.ADDITION);

    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
confidence: your spirit regeneration speed scales proportionally with how much spirit you have left, from 200% at full to 50% at empty
     */

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

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
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof RegenSpiritEvent&&procPoint.getPhase()== EventPriority.HIGHEST) {
            float posPerc = CombatData.getCap(caster).getSpirit() / CombatData.getCap(caster).getMaxSpirit();
            ((RegenSpiritEvent) procPoint).setQuantity(((RegenSpiritEvent) procPoint).getQuantity() * (0.5f + 1.5f * posPerc));
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
