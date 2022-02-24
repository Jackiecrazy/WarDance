package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
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
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "boundCast", "normalAttack", "countdown", ProcPoints.recharge_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return offensivePhysical;
    }

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
        return isPassive(caster) ? 0 : 2;
    }

    @Override
    public boolean isPassive(LivingEntity caster) {
        return this == WarSkills.PUMMEL.get();
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {
        final ItemStack off = caster.getHeldItemOffhand();
        if (CombatUtils.isShield(caster, off)) {
            SkillUtils.auxAttack(caster, target, new CombatDamageSource("player", caster).setProcNormalEffects(false).setProcAttackEffects(true).setProcSkillEffects(true).setAttackingHand(Hand.OFF_HAND).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setDamageDealer(off), 0, CombatUtils.getShieldStats(off).getA() / (isPassive(caster) ? 40f : 20f));
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target && CombatUtils.isMeleeAttack(((LivingAttackEvent) procPoint).getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            final boolean base = isPassive(caster) && state != STATE.COOLING;
            final boolean otherwise = state == STATE.HOLSTERED && CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource()));
            if ((base || otherwise) && cast(caster, -999)) {
                performEffect(caster, target);
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                markUsed(caster);
            }
        }
        if (procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING && ((ParryEvent) procPoint).getEntityLiving() == caster) {
            stats.decrementDuration();
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (isPassive(caster)) return cooldownTick(stats);
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, prev, isPassive(caster) ? 6 : 4);
        return boundCast(prev, from, to);
    }

    public static class RimPunch extends ShieldBash {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            target.addPotionEffect(new EffectInstance(Effects.NAUSEA, 60));
            CombatUtils.knockBack(target, caster, 0.2f, true, false);
        }
    }

    public static class FootSlam extends ShieldBash {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            super.performEffect(caster, target);
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 60));
            target.addPotionEffect(new EffectInstance(WarEffects.DISTRACTION.get(), 60));
        }
    }
}
