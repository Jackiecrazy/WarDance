package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class ShieldBash extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "boundCast", "normalAttack", "countdown", ProcPoints.recharge_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return offensivePhysical;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
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

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 40);
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        if (getParentCategory() == null) {
            if (CombatUtils.isShield(caster, caster.getHeldItemMainhand()))
                CombatData.getCap(caster).setHandBind(Hand.MAIN_HAND, 0);
            if (CombatUtils.isShield(caster, caster.getHeldItemOffhand()))
                CombatData.getCap(caster).setHandBind(Hand.OFF_HAND, 0);
        }
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 4);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(target).consumePosture(caster, CombatUtils.getShieldStats(caster.getHeldItemMainhand()).getA() / 20f);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
        if (procPoint instanceof LivingAttackEvent && CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource()))) {
            performEffect(caster, target);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            markUsed(caster);
        }
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
