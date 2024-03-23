package jackiecrazy.wardance.skill.misc;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.MovementUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

public class Pounce extends Skill {
    private static final AttributeModifier less = new AttributeModifier(UUID.fromString("eba24c38-73e3-4551-9df4-e06e117699c1"), "pounce debuff", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && lae.getSource() instanceof CombatDamageSource cds && caster.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(less)) {
            //pounce attack, mark as skill
            cds.setSkillUsed(this);
            cds.setProcSkillEffects(true);
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (MovementUtils.hasInvFrames(caster)) {
            Entity collide = MovementUtils.collidingEntity(caster);
            if (collide instanceof LivingEntity le) {
                stats.addTarget(le);
            }
        } else {
            if (!stats.getTargets().isEmpty()) {
                SkillUtils.addAttribute(caster, Attributes.ATTACK_DAMAGE, less);
                SkillUtils.addAttribute(caster, Attributes.ATTACK_KNOCKBACK, less);
                stats.getTargets().forEach(collide -> {
                    collide.invulnerableTime = 0;
                    CombatUtils.attack(caster, collide, false);
                    collide.invulnerableTime = 0;
                    CombatUtils.attack(caster, collide, true);
                });
                SkillUtils.removeAttribute(caster, Attributes.ATTACK_DAMAGE, less);
                SkillUtils.removeAttribute(caster, Attributes.ATTACK_KNOCKBACK, less);
                stats.getTargets().clear();
            }
        }
        return false;
    }
}
