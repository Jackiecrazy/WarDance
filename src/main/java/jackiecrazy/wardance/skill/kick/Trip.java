package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Trip extends Kick {
    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 3);
    }

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public boolean canCast(LivingEntity caster) {
        return super.canCast(caster) && caster.isOnGround();
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (target.isOnGround()) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            target.addPotionEffect(new EffectInstance(WarEffects.EXHAUSTION.get(), CombatData.getCap(target).getPostureGrace() * 2, 2));
        }
    }
}
