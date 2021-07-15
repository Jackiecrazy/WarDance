package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class PoundOfFlesh extends MementoMori {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList(SkillTags.melee, SkillTags.on_hurt, SkillTags.state, SkillTags.on_being_parried)));

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, CombatData.getCap(caster).getSpirit() * 20);
        CombatData.getCap(caster).consumeSpirit(CombatData.getCap(caster).getSpirit());
        return true;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof ParryEvent) {
            //TODO mark all skill damage appropriately
            caster.hurtResistantTime=0;
            caster.attackEntityFrom(CombatDamageSource.inflictSelfHarm(null).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).setDamageBypassesArmor().setDamageIsAbsolute(), GeneralUtils.getMaxHealthBeforeWounding(caster) * 0.05f);
            ((ParryEvent) procPoint).setPostureConsumption(((ParryEvent) procPoint).getPostureConsumption() + CombatData.getCap(target).getTrueMaxPosture() * 0.1f);
        } else if (procPoint instanceof LivingHurtEvent && (!(((LivingHurtEvent) procPoint).getSource() instanceof CombatDamageSource) || ((CombatDamageSource) ((LivingHurtEvent) procPoint).getSource()).getSkillUsed() != this)) {
            caster.hurtResistantTime=0;
            caster.attackEntityFrom(CombatDamageSource.inflictSelfHarm(null).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).setDamageBypassesArmor().setDamageIsAbsolute(), GeneralUtils.getMaxHealthBeforeWounding(caster) * 0.05f);
            ((LivingHurtEvent) procPoint).setAmount(((LivingHurtEvent) procPoint).getAmount() + GeneralUtils.getMaxHealthBeforeWounding(target) * 0.05f);
        }
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
//        if (CombatData.getCap(caster).getSpiritGrace() <= 0)
//            markUsed(caster);
        d.decrementDuration();
        return super.activeTick(caster, d);
    }
}
