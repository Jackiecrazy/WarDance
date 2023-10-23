package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.DamageKnockbackEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public abstract class FiveElementFist extends Skill {

    private static final HashSet<String> tag = makeTag(SkillTags.offensive, SkillTags.physical, "elementfist");
    private static final HashSet<String> no = makeTag("elementfist");


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
    public HashSet<String> getTags() {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public HashSet<String> getHardIncompatibility() {
        return no;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_KNOCKBACK, u, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        //if active, change
        if (stats.getState() == STATE.ACTIVE) {
            swap(caster);
            stats.setState(STATE.INACTIVE);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, ForgeMod.ENTITY_REACH.get(), u, 0, AttributeModifier.Operation.ADDITION);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        //swap
        if (procPoint instanceof CriticalHitEvent lae && lae.getTarget() == target && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            if (lae.getPhase() == EventPriority.HIGHEST) {
                onStateChange(caster, stats, STATE.INACTIVE, STATE.ACTIVE);
                ParticleUtils.playBonkParticle(caster.level(), caster.getEyePosition().add(caster.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) * 0.9)), 0.5, 0.1, 8, getColor());
            }
        }
        //unarmed attack
        if (procPoint instanceof DamageKnockbackEvent lae && lae.getEntity() == target && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            if (lae.getPhase() == EventPriority.HIGHEST) {
                if (lae.getDamageSource() instanceof CombatDamageSource cds) {
                    cds.setSkillUsed(this);
                    cds.setProcSkillEffects(true);
                }
                lae.setStrength(0.15);
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

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }

    protected void doAttack(LivingEntity caster, LivingEntity target) {
    }

    protected void swap(LivingEntity caster) {
        if (cycle.length == 0)
            cycle = new Skill[]{
                    WarSkills.WOODEN_JAB.get(),
                    WarSkills.FIERY_LUNGE.get(),
                    WarSkills.EARTHEN_SWEEP.get(),
                    WarSkills.IRON_CHOP.get(),
                    WarSkills.WATER_UPPERCUT.get()
            };
        int found = -1;
        for (int x = 0; x < cycle.length * 2; x++) {
            int working = x % cycle.length;
            if (cycle[working] == this) {
                found = working;
            }
            if (found >= 0) {
                int next = (x + 1) % cycle.length;
                if (next == (found + 1) % cycle.length)
                    CombatData.getCap(caster).addRank(0.1f);
                final Skill skill = cycle[next];
                if (CasterData.getCap(caster).replaceSkill(this, skill)) {
                    return;
                }
            }
        }
    }
}
