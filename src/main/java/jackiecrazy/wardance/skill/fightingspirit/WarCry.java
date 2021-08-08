package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.WarDance;
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
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class WarCry extends Skill {
    private static final AttributeModifier wrap = new AttributeModifier(UUID.fromString("4b342542-fcfb-47a8-8da8-4f57588f7003"), "bandaging wounds", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", SkillTags.on_being_hurt, SkillTags.countdown, SkillTags.recharge_time, SkillTags.recharge_sleep)));
    private final Tag<String> no = Tag.getEmptyTag();

    @SubscribeEvent
    public static void fightingspirit(LivingHealEvent e) {
        if (e.getEntityLiving() != null && !CasterData.getCap(e.getEntityLiving()).isSkillCoolingDown(WarSkills.WAR_CRY.get())) {
            e.setAmount(e.getAmount() * 1.5f);
        }
    }

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
                return new TranslationTextComponent("wardance:war_cry.sleep.name");
            } else return new TranslationTextComponent("wardance:war_cry.nosleep.name");
        }
        return new TranslationTextComponent(this.getRegistryName().toString() + ".name");
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == WarCry.class ? null : WarSkills.WAR_CRY.get();
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
        if(getParentSkill()==null) {
            caster.addPotionEffect(new EffectInstance(Effects.REGENERATION, getDuration()));
            caster.addPotionEffect(new EffectInstance(Effects.RESISTANCE, getDuration()));
        }
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        return super.activeTick(caster, d);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        if (caster instanceof PlayerEntity && (caster.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(wrap) || stats.isCondition()))
            ForgeEventFactory.onPlayerWakeup(((PlayerEntity) caster), false, false);
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
