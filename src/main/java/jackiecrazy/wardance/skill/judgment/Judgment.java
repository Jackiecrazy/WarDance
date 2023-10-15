package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;

public class Judgment extends Skill {
    /*
    Redirects all might gain to another bar temporarily. Gaining 10 might in this manner will cause your next attack to deal 20% of the target's current health in damage. 10 second cooldown.
    */
    @Override
    public HashSet<String> getTags() {
        return special;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return special;
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.none;
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        float amount = stack == 3 ? target.getHealth() * 0.15f : target.getHealth() * 0.03f;
        target.hurt(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).bypassArmor().bypassMagic(), amount);
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (CombatData.getCap(caster).getComboRank() < 5)
            return CastStatus.OTHER;
        return super.castingCheck(caster, sd);
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 2;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE && stats.getDuration() > 0) {
            boolean stat = stats.getDuration() > 0;
            activeTick(stats);
            if (stats.getDuration() < 0 && stat) {
                onStateChange(caster, stats, STATE.INACTIVE, STATE.HOLSTERED);
                stats.setDuration(0);
            }
            return true;
        }
        return cooldownTick(stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster);
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target != null && cast(caster, target, -999)) {
            int stack = 1;
            float arb = Marks.getCap(target).getActiveMark(this).orElse(SkillData.DUMMY).getArbitraryFloat();
            if (arb >= 2) {//detonate
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                removeMark(target);
            } else {
                prev.flagCondition(true);
                mark(caster, target, 6, 1);
            }
            stack += arb;
            performEffect(caster, target, stack, prev);
            target.invulnerableTime = 0;
            boolean offhand = stack == 2;
            CombatUtils.attack(caster, target, offhand);
            caster.swing(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, true);
            caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.RAVAGER_STEP, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        }
        if (to == STATE.COOLING) {
            if (prev.isCondition())
                prev.setDuration(2);
            else setCooldown(caster, prev, 20);
            return true;
        }
        if (from != STATE.COOLING && to == STATE.HOLSTERED)
            caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        return boundCast(prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            if (existing.getDuration() < 0) return null;
            sd.addArbitraryFloat(existing.getArbitraryFloat());
            sd.setDuration(12);
        }
        return sd;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration(0.05f);
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingDeathEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingDeathEvent) procPoint).getEntity() == target) {
            Marks.getCap(target).getActiveMark(this).ifPresent((a) -> CombatData.getCap(caster).addMight(a.getArbitraryFloat() * mightConsumption(caster) * 1.4f));
        }
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntity() == target) {
            if (state == STATE.ACTIVE)
                ((LivingAttackEvent) procPoint).getSource().bypassArmor().bypassMagic();
            Marks.getCap(target).getActiveMark(this).ifPresent((a) -> a.setDuration(a.getArbitraryFloat() == 1 ? 6 : 12));
        }
    }

}
