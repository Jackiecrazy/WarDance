package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class FightingSpirit extends Skill {
    private static final AttributeModifier wrap = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b1"), "bandaging wounds", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", SkillTags.on_being_hurt, SkillTags.countdown, SkillTags.recharge_time, SkillTags.recharge_sleep)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public ITextComponent getDisplayName(LivingEntity caster) {
        if (caster != null) {
            ICombatCapability cap = CombatData.getCap(caster);
            if (cap.getMight() == 0 && cap.getPosture() == cap.getMaxPosture() && cap.getSpirit() == cap.getMaxSpirit()) {
                return new TranslationTextComponent("wardance:fighting_spirit.sleep.name");
            }
        }
        return new TranslationTextComponent(this.getRegistryName().toString() + ".name");
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == FightingSpirit.class ? null : WarSkills.FIGHTING_SPIRIT.get();
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        return CasterData.getCap(caster).isSkillCoolingDown(this) ? CastStatus.COOLDOWN : CastStatus.ALLOWED;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        final ICombatCapability cap = CombatData.getCap(caster);
        if (cap.getMight() == 0 && cap.getPosture() == cap.getMaxPosture() && cap.getSpirit() == cap.getMaxSpirit()) {
            activate(caster, 100);
            CasterData.getCap(caster).getActiveSkill(this).ifPresent((a) -> a.flagCondition(true));
            caster.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(wrap);
        } else {
            evoke(caster);
            activate(caster, getDuration());
        }
        return true;
    }

    protected int getDuration() {
        return 200;
    }

    protected void evoke(LivingEntity caster) {
        caster.addPotionEffect(new EffectInstance(Effects.REGENERATION, getDuration()));
        caster.addPotionEffect(new EffectInstance(Effects.RESISTANCE, getDuration()));
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (d.getDuration() == 0 && d.isCondition()) {
            if (caster instanceof PlayerEntity)
                ForgeEventFactory.onPlayerWakeup(((PlayerEntity) caster), false, false);
        }
        return super.activeTick(caster, d);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(wrap);
        setCooldown(caster, 6000);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            markUsed(caster);
        }
    }
}
