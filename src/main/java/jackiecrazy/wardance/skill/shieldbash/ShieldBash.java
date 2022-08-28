package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.api.WarAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class ShieldBash extends Skill {
    private final Tag<String> tag = Tag.create(new HashSet<>(Arrays.asList("physical", "melee", "boundCast", "normalAttack", "countdown", ProcPoints.recharge_parry)));
    private final Tag<String> no = Tag.create(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return offensivePhysical;
    }

    @Nonnull
    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.shield_bash;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 2;
    }


    protected void performEffect(LivingEntity caster, LivingEntity target) {
        final ICombatCapability cap = CombatData.getCap(caster);
        SkillUtils.auxAttack(caster, target, new CombatDamageSource("player", caster).setProcNormalEffects(false).setProcAttackEffects(true).setProcSkillEffects(true).setAttackingHand(Hand.OFF_HAND).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setDamageDealer(caster.getMainHandItem()), 0, cap.consumeBarrier(cap.getBarrier()/4));
        cap.setBarrierCooldown(cap.getBarrierCooldown() / 2);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target && CombatUtils.isMeleeAttack(((LivingAttackEvent) procPoint).getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            final boolean base = isPassive(caster) && state != STATE.COOLING;
            final boolean otherwise = state == STATE.HOLSTERED && CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource()));
            if ((base || otherwise) && cast(caster, target, -999)) {
                performEffect(caster, target);
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                markUsed(caster);
            }
        }
        if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING && ((ParryEvent) procPoint).getEntityLiving() == caster) {
            stats.decrementDuration();
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)//no need for cooldown because it basically cools down with shield anyway
            prev.setState(STATE.INACTIVE);
        return boundCast(prev, from, to);
    }

    public static class RimPunch extends ShieldBash {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            target.addEffect(new EffectInstance(Effects.CONFUSION, 60));
            CombatUtils.knockBack(target, caster, (float) caster.getAttributeValue(WarAttributes.BARRIER.get()), true, false);
            CombatData.getCap(caster).consumeBarrier(CombatData.getCap(caster).getBarrier());
            CombatData.getCap(caster).setBarrierCooldown(CombatData.getCap(caster).getBarrierCooldown() / 2);
        }
    }

    public static class FootSlam extends ShieldBash {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            super.performEffect(caster, target);
            final int time = CombatData.getCap(caster).getBarrierCooldown();
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, time * 2));
            target.addEffect(new EffectInstance(FootworkEffects.DISTRACTION.get(), time * 2));
        }
    }
}
