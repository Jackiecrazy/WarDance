package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class Apathy extends Skill {
    private static final AttributeModifier sprint = new AttributeModifier(UUID.fromString("0683fe69-5348-4a83-95d5-81a2eeb2ffa0"), "meh", -2, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier sprint2 = new AttributeModifier(UUID.fromString("0683fe69-5348-4a83-95d5-81a2eeb2ffa0"), "eh", 1, AttributeModifier.Operation.ADDITION);

    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
confidence: your spirit regeneration speed scales proportionally with how much spirit you have left, from 200% at full to 50% at empty
     */

    private final HashSet<String> tag = makeTag("passive", ProcPoints.on_kill, ProcPoints.change_spirit);
    private final HashSet<String> no = none;

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.morale;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        final ICombatCapability cap = CombatData.getCap(caster);
        if (cap.getSpiritGrace() == 0 && cap.getSpirit() < cap.getMaxSpirit())
            cap.setSpirit(cap.getMaxSpirit());
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, FootworkAttributes.MAX_SPIRIT.get(), sprint);
        SkillUtils.addAttribute(caster, FootworkAttributes.SPIRIT_COOLDOWN.get(), sprint2);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, FootworkAttributes.MAX_SPIRIT.get(), sprint);
        SkillUtils.removeAttribute(caster, FootworkAttributes.SPIRIT_COOLDOWN.get(), sprint2);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
