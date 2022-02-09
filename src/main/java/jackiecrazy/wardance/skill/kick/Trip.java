package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Trip extends Kick {

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 2;
    }

    protected void additionally(LivingEntity caster, LivingEntity target) {
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (target.isOnGround()) {
            super.onProc(caster, procPoint, state, stats, target);
            target.addPotionEffect(new EffectInstance(WarEffects.EXHAUSTION.get(), CombatData.getCap(target).getPostureGrace() * 2, 2));
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, CombatData.getCap(target).getPostureGrace() * 2, 0));
        }
    }
}
