package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class FrostFang extends FightingSpirit {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", SkillTags.melee, SkillTags.on_being_hurt, SkillTags.countdown, SkillTags.recharge_time, SkillTags.recharge_sleep)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList(SkillTags.melee)));
    private static final AttributeModifier luck =new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b4"), "frost fang bonus", 2, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier speed =new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b4"), "frost fang bonus", 0.4, AttributeModifier.Operation.MULTIPLY_BASE);

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    protected void evoke(LivingEntity caster) {
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(speed);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(speed);
    }

    protected int getDuration() {
        return 400;
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.LUCK).removeModifier(luck);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(speed);
        super.onEffectEnd(caster, stats);
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        caster.getAttribute(Attributes.LUCK).removeModifier(luck.getID());
        caster.getAttribute(Attributes.LUCK).applyPersistentModifier(luck);
        super.onCooledDown(caster, overflow);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if(procPoint instanceof LivingAttackEvent) {
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 60));
            if(CombatUtils.getAwareness(caster, target)== CombatUtils.AWARENESS.ALERT){
                target.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 20));
            }
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }
}
