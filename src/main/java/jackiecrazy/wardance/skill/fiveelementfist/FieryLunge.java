package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.MeleeKnockbackEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FieryLunge extends FiveElementFist {

    private static final UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4b5-5e3c9d573b80");

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, ForgeMod.ATTACK_RANGE.get(), u, CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) ? 2 : 0, AttributeModifier.Operation.ADDITION);
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, ForgeMod.ATTACK_RANGE.get(), u, 0, AttributeModifier.Operation.ADDITION);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof MeleeKnockbackEvent e && procPoint.getPhase() == EventPriority.HIGHEST && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) && e.getAttacker() == caster) {
            e.setStrength((float) (e.getOriginalStrength() * 1.5));
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        caster.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).scale(0.14)));
        CombatData.getCap(caster).setRollTime(-10);
        caster.hurtMarked = true;
        CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, (int) (SkillUtils.getSkillEffectiveness(caster) * 10));
    }
}
