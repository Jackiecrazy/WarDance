package jackiecrazy.wardance.skill.guillotine;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.List;

public class CrowdPleaser extends Judgment {
    @Override
    public Color getColor() {
        return Color.MAGENTA;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof SkillCastEvent && ((SkillCastEvent) procPoint).getSkill() != this && procPoint.getPhase() == EventPriority.HIGHEST) {
            stats.setArbitraryFloat(stats.getArbitraryFloat() + 1);
        } else super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        super.performEffect(caster, target, stack, sd);
        int buff = 0;
        if (CasterData.getCap(caster).getSkillData(this).isPresent())
            buff = (int) CasterData.getCap(caster).getSkillData(this).get().getArbitraryFloat();
        final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isAlly(a, caster));
        for (LivingEntity pet : list) {
            if (stack == 1) {
                pet.addPotionEffect(new EffectInstance(Effects.SPEED, buff * 40 + 60));
                pet.addPotionEffect(new EffectInstance(Effects.LUCK, buff * 40 + 60));
            } else if (stack == 2) {
                pet.addPotionEffect(new EffectInstance(Effects.STRENGTH, buff * 40 + 60));
                pet.addPotionEffect(new EffectInstance(Effects.HASTE, buff * 40 + 60));
            } else {
                pet.addPotionEffect(new EffectInstance(Effects.REGENERATION, buff * 40 + 60));
                pet.addPotionEffect(new EffectInstance(Effects.RESISTANCE, buff * 40 + 60));
            }
        }
    }
}
