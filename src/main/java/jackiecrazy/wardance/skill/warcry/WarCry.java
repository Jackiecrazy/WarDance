package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.tags.Tag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class WarCry extends Skill {
    private static final AttributeModifier wrap = new AttributeModifier(UUID.fromString("4b342542-fcfb-47a8-8da8-4f57588f7003"), "bandaging wounds", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final Tag<String> procs = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList(SkillTags.chant, SkillTags.melee, SkillTags.state)));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return procs;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return state;
    }

    @Override
    public ITextComponent getDisplayName(LivingEntity caster) {
        if (caster != null) {
            ICombatCapability cap = CombatData.getCap(caster);
            if (cap.getMight() == 0 && cap.getPosture() == cap.getMaxPosture() && cap.getSpirit() == cap.getMaxSpirit()) {
                return new TranslationTextComponent("wardance:war_cry.sleep.name", ((int)(CombatData.getCap(caster).getResolve()*10))/10f);
            } else return new TranslationTextComponent(getRegistryName().toString() + ".name");
        }
        return new TranslationTextComponent(getRegistryName().toString() + ".name");
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.war_cry;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        return super.castingCheck(caster);
    } //why did I do this? Weird.

    @Override
    public boolean onCast(LivingEntity caster) {
        final ICombatCapability cap = CombatData.getCap(caster);
        if (cap.getMight() == 0 && cap.getPosture() == cap.getMaxPosture() && cap.getSpirit() == cap.getMaxSpirit()) {
            activate(caster, 100, true);
            caster.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(wrap);
        } else {
            evoke(caster);
            activate(caster, getDuration(CombatData.getCap(caster).getMight()));
        }
        return true;
    }

    protected int getDuration(float might) {
        return 60 + (int) (might * 20);
    }

    protected void evoke(LivingEntity caster) {
        if (getParentCategory() == null) {
            final float might = CombatData.getCap(caster).getMight();
            caster.addPotionEffect(new EffectInstance(Effects.REGENERATION, getDuration(might)));
            if (might > 5) {
                caster.addPotionEffect(new EffectInstance(Effects.RESISTANCE, getDuration(might)));
                caster.addPotionEffect(new EffectInstance(Effects.ABSORPTION, getDuration(might), 1));
            } else caster.addPotionEffect(new EffectInstance(Effects.ABSORPTION, getDuration(might)));
        }
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (d.getDuration() > 0) {
            d.decrementDuration();
            if (d.getDuration() <= 0) markUsed(caster);
            return true;
        }
        return false;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        if (caster instanceof PlayerEntity && (caster.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(wrap) || stats.isCondition())) {
            //TODO remove when flattening
            ForgeEventFactory.onPlayerWakeup(((PlayerEntity) caster), false, false);
            ((PlayerEntity) caster).takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            setCooldown(caster, 60);
        } else setCooldown(caster, 60);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(wrap);
    }

    @Override
    public boolean onCooldownProc(LivingEntity caster, SkillCooldownData stats, Event procPoint) {
        stats.decrementDuration(0.05f);
        int round = (int) (stats.getDuration() * 20);
        return stats.getDuration() < 5 || round % 20 == 0;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent && stats.isCondition() && ((LivingHurtEvent) procPoint).getEntityLiving() == caster) {
            markUsed(caster);
        }
        if (procPoint instanceof LivingHealEvent) {
            ((LivingHealEvent) procPoint).setAmount(((LivingHealEvent) procPoint).getAmount() * 1.5f);
        }
    }
}
