package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public abstract class FiveElementFist extends Skill {

    private static final UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4b5-5e3c9d573b80");
    private static Skill[] cycle = {
    };

    @Override
    public boolean isPassive(LivingEntity caster) {
        return true;
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.five_element_fist;
    }

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return offensivePhysical;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_KNOCKBACK, u, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        //if active, change
        if (stats.getState() == STATE.ACTIVE) {
            swapTo(caster);
            stats.setState(STATE.INACTIVE);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, ForgeMod.ATTACK_RANGE.get(), u, 0, AttributeModifier.Operation.ADDITION);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        //unarmed attack
        if (procPoint instanceof CriticalHitEvent lae && lae.getTarget() == target && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            if (lae.getPhase() == EventPriority.HIGHEST) {
                onStateChange(caster, stats, STATE.INACTIVE, STATE.ACTIVE);
                Vec3 vec=target.getDeltaMovement();
                target.setDeltaMovement(vec.x, 0, vec.z);
                doAttack(caster, target);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        //active means it's expended, cooling means it just swapped in
        if (to == STATE.ACTIVE) prev.setState(STATE.ACTIVE);
        return passive(prev, from, to);
    }

    protected void doAttack(LivingEntity caster, LivingEntity target) {
    }

    protected void swapTo(LivingEntity caster) {
        if (cycle.length == 0)
            cycle = new Skill[]{
                    WarSkills.WOODEN_JAB.get(),
                    WarSkills.FIERY_LUNGE.get(),
                    WarSkills.EARTHEN_SWEEP.get(),
                    WarSkills.IRON_CHOP.get(),
                    WarSkills.WATER_UPPERCUT.get()
            };
        boolean found = false;
        for (int x = 0; x < cycle.length * 2; x++) {
            int working = x % cycle.length;
            if (cycle[working] == this) {
                found = true;
            }
            if (found) {
                int next = (x + 1) % cycle.length;
                final Skill skill = cycle[next];
                if (CasterData.getCap(caster).replaceSkill(this, skill)) {
                    System.out.println("swapped to " + skill);
                    return;
                }
            }
        }
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }
}
