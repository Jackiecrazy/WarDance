package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.RegenSpiritEvent;
import jackiecrazy.wardance.skill.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class NaturalSprinter extends Skill {
    private static final AttributeModifier sprint = new AttributeModifier(UUID.fromString("0683fe69-5348-4a83-95d5-81a2eeb2cca0"), "gimli moment", 10, AttributeModifier.Operation.ADDITION);

    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", ProcPoints.on_kill, ProcPoints.change_spirit)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return empty;
    }


    @Nonnull
    @Override
    public SkillCategory getParentSkill() {
        return SkillCategories.morale;
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        final ModifiableAttributeInstance beep = caster.getAttribute(WarAttributes.MAX_SPIRIT.get());
        beep.removeModifier(sprint);
        beep.applyPersistentModifier(sprint);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
    }

    @Override
    public void onAdded(LivingEntity caster, SkillData stats) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).applyPersistentModifier(sprint);
    }

    @Override
    public void onRemoved(LivingEntity caster, SkillData stats) {
        caster.getAttribute(WarAttributes.MAX_SPIRIT.get()).removeModifier(sprint);
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
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
