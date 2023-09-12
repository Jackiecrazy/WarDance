package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.DamageKnockbackEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WoodenJab extends FiveElementFist {

    private static final UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4b5-5e3c9d573b80");

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, ForgeMod.ATTACK_RANGE.get(), u, CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) ? 1 : 0, AttributeModifier.Operation.ADDITION);
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_KNOCKBACK, u, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, ForgeMod.ATTACK_RANGE.get(), u, 0, AttributeModifier.Operation.ADDITION);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof DamageKnockbackEvent e && procPoint.getPhase() == EventPriority.HIGHEST && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) && e.getEntity() == target) {
            e.setStrength((float) (e.getOriginalStrength() * 1.5));
        }
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(caster).addRank(0.1f);
    }
}
