package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class Necrosis extends Skill {
    private static final UUID uuid = UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1");

    private static final AttributeModifier health = new AttributeModifier(uuid, "asceticism bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.memento_mori;
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
        double shouldBe = caster.getMaxHealth();
        final double amount = CombatData.getCap(caster).getComboRank() / 7d;
        SkillUtils.modifyAttribute(caster, Attributes.MAX_HEALTH, uuid, amount * SkillUtils.getSkillEffectiveness(caster), AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (caster.getHealth() > shouldBe) caster.setHealth((float) shouldBe);
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.MAX_HEALTH).removeModifier(uuid);
        super.onUnequip(caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
