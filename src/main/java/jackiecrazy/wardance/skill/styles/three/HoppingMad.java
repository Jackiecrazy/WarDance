package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HoppingMad extends SkillStyle {
    private static final AttributeModifier mad = new AttributeModifier(UUID.fromString("abe24c38-1234-4551-9df4-e06e117699c1"), "hopping mad", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public HoppingMad() {
        super(3);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        caster.getAttribute(FootworkAttributes.MAX_POSTURE.get()).addPermanentModifier(mad);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(FootworkAttributes.MAX_POSTURE.get()).removeModifier(mad);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingEvent.LivingJumpEvent) {
            stats.flagCondition(true);
        } else if (procPoint instanceof LivingFallEvent && stats.isCondition() && procPoint.getPhase() == EventPriority.HIGHEST) {
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addPosture((cap.getMaxPosture() - cap.getPosture()) / 2);
            stats.flagCondition(false);
        }
    }
}
