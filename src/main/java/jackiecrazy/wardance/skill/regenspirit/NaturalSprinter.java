package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.footwork.api.WarAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.RegenSpiritEvent;
import jackiecrazy.wardance.skill.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.tags.SetTag;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import jackiecrazy.wardance.skill.Skill.STATE;

public class NaturalSprinter extends Skill {
    private static final AttributeModifier sprint = new AttributeModifier(UUID.fromString("0683fe69-5348-4a83-95d5-81a2eeb2cca0"), "gimli moment", 10, AttributeModifier.Operation.ADDITION);

    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
confidence: your spirit regeneration speed scales proportionally with how much spirit you have left, from 200% at full to 50% at empty
     */

    private final SetTag<String> tag = SetTag.create(new HashSet<>(Arrays.asList("passive", ProcPoints.on_kill, ProcPoints.change_spirit)));
    private final SetTag<String> no = SetTag.empty();

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public SetTag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public SetTag<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.morale;
    }

    @Override
    public void onEquip(LivingEntity caster) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).addPermanentModifier(sprint);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingDeathEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            CombatData.getCap(caster).addSpirit(3);
        } else if (procPoint instanceof RegenSpiritEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            ((RegenSpiritEvent) procPoint).setQuantity(((RegenSpiritEvent) procPoint).getQuantity() / 3);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
