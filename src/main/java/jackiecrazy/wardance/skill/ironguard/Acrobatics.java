package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.event.ConsumePostureEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.UUID;

public class Acrobatics extends Skill {
    public static final UUID aerial = UUID.fromString("662A6B8D-DA3E-4C1C-2213-96EA6097278D");
    private static final AttributeModifier batics = new AttributeModifier(aerial, "acrobatics bonus", 0.4, AttributeModifier.Operation.ADDITION);

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (!caster.onGround()) {
            if (stats.getState() != STATE.ACTIVE) {
                SkillUtils.addAttribute(caster, WarAttributes.COMPOSURE.get(), batics);
                stats.setState(STATE.ACTIVE);
            }
        } else if (stats.getState() == STATE.ACTIVE) {
            SkillUtils.removeAttribute(caster, WarAttributes.COMPOSURE.get(), batics);
            stats.setState(STATE.INACTIVE);
        }
        return super.equippedTick(caster, stats);
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
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {
        if (procPoint instanceof ConsumePostureEvent d && procPoint.getPhase() == EventPriority.HIGHEST && state != STATE.COOLING && d.getPostureConsumption() > 0 && d.getEntity() == caster) {
            if (d.getType() != ConsumePostureEvent.TYPE.NONE && state != STATE.COOLING && !caster.onGround() && d.success()) {
                //            float str = -Math.min(procPoint.getPostureConsumption() / 2, 2) * stats.getEffectiveness();
                //            caster.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).normalize().scale(str)));
                //            caster.hurtMarked = true;
                AerialModeData.getCap(caster).setAerialMode(60);
                CombatData.getCap(caster).setDodgeTime(-99999);//resets dodge
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 4);
        return passive(prev, from, to);
    }

}
