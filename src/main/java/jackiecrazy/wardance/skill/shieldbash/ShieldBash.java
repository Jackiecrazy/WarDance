package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class ShieldBash extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "normalAttack", "countdown", ProcPoint.recharge_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == ShieldBash.class ? null : WarSkills.SHIELD_BASH.get();
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 40);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 4);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent && CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource()))) {
            performEffect(caster, target);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ITEM_SHIELD_BLOCK , SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f+WarDance.rand.nextFloat() * 0.5f);
            CombatData.getCap(target).consumePosture(CombatUtils.getShieldStats(CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource())).getA() / 20f);
            markUsed(caster);
        }
    }

    public static class RimPunch extends ShieldBash {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            target.addPotionEffect(new EffectInstance(Effects.NAUSEA, 40));
        }
    }

    public static class FootSlam extends ShieldBash {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        protected void performEffect(LivingEntity caster, LivingEntity target) {
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 60));
            target.addPotionEffect(new EffectInstance(WarEffects.CONFUSION.get(), 60));
        }
    }
}
