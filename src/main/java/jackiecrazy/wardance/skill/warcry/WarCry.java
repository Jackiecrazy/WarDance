package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.SetTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class WarCry extends Skill {
    private static final AttributeModifier wrap = new AttributeModifier(UUID.fromString("4b342542-fcfb-47a8-8da8-4f57588f7003"), "bandaging wounds", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final SetTag<String> procs = SetTag.create(new HashSet<>(Arrays.asList("chant", ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final SetTag<String> tag = SetTag.create(new HashSet<>(Arrays.asList(SkillTags.chant, SkillTags.melee, SkillTags.state)));

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return state;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 5;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.war_cry;
    }

    protected int getDuration(float might) {
        return Math.max(0, (int) (might * 1.5));
    }

    protected void evoke(LivingEntity caster) {
        CombatData.getCap(caster).addMight(mightConsumption(caster));
        caster.level.playSound(null, caster, SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat());
        if (this == WarSkills.REJUVENATE.get()) {
            final float might = CombatData.getCap(caster).getMight();
            final int duration = getDuration(might) * 20;
            caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration));
            if (might > 7) {
                caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration));
                caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 1));
            } else caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration));
            markUsed(caster);
        }
        CombatData.getCap(caster).setMight(0);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingHealEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            ((LivingHealEvent) procPoint).setAmount(((LivingHealEvent) procPoint).getAmount() * 1.5f);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.HOLSTERED && cast(caster, getDuration(CombatData.getCap(caster).getMight())))
            evoke(caster);
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setDuration(0);
        }
        return instantCast(prev, from, to);
    }
}
