package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potions;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

public class Stagger extends HeavyBlow {
    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof LivingAttackEvent && CombatData.getCap(caster).consumeMight(1)) {
            float mightDiff = Math.max(CombatData.getCap(caster).getMight() - CombatData.getCap(target).getMight(), 0);
            CombatData.getCap(target).addFatigue(3 + mightDiff / 2);
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 16 + (int) (mightDiff * 2)));
        }
    }
}
