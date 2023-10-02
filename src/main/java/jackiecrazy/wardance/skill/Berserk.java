package jackiecrazy.wardance.skill;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class Berserk extends Skill {
    private static final AttributeModifier berserk = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk twohanding bonus", 3, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier berserk1 = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk attack speed bonus", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final HashSet<String> tag = makeTag(SkillTags.offensive, SkillTags.physical);

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public HashSet<String> getTags() {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return activeTick(stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (state == STATE.HOLSTERED && cast(caster, target, 3 * SkillUtils.getSkillEffectiveness(caster) * (1 - (caster.getHealth() / caster.getMaxHealth())) * (CombatData.getCap(caster).getMight()))) {
                SkillUtils.addAttribute(caster, FootworkAttributes.TWO_HANDING.get(), berserk);
                SkillUtils.addAttribute(caster, Attributes.ATTACK_SPEED, berserk1);
            }
        }
        if (procPoint instanceof LivingDeathEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST) {
            stats.setDuration(stats.getMaxDuration());
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            CombatData.getCap(caster).setMight(0);
            SkillUtils.removeAttribute(caster, FootworkAttributes.TWO_HANDING.get(), berserk);
            SkillUtils.removeAttribute(caster, Attributes.ATTACK_SPEED, berserk1);
        }
        return boundCast(prev, from, to);
    }


}
