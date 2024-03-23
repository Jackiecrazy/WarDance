package jackiecrazy.wardance.skill.misc;

import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.event.SweepEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.two.WarCry;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

public class Lunge extends Skill {
    /*
    +posture damage on standing
+crit damage on falling
+knockback on sneaking
+range on sprinting
+sweep on riding
     */
    private static final AttributeModifier sprint = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "lunge bonus", 1, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier sneak = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "lunge bonus", 0.3, AttributeModifier.Operation.ADDITION);

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (caster.isSprinting()) {
            SkillUtils.addAttribute(caster, ForgeMod.ENTITY_REACH.get(), sprint);
        } else SkillUtils.removeAttribute(caster, ForgeMod.ENTITY_REACH.get(), sprint);
        if (caster.isCrouching()) {
            SkillUtils.addAttribute(caster, Attributes.ATTACK_KNOCKBACK, sneak);
        } else SkillUtils.removeAttribute(caster, Attributes.ATTACK_KNOCKBACK, sneak);
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, ForgeMod.ENTITY_REACH.get(), sprint);
        SkillUtils.removeAttribute(caster, Attributes.ATTACK_KNOCKBACK, sneak);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent e && isCrit(e) && e.getPhase() == EventPriority.HIGHEST) {
            e.setDamageModifier(e.getDamageModifier() + 0.2f);
        }
        if (procPoint instanceof SweepEvent e && e.getPhase() == EventPriority.HIGHEST) {
            e.setSweepLevel(e.getSweepLevel() + 2);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

}
