package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class Stagger extends HeavyBlow {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (CasterData.getCap(target).isSkillActive(WarSkills.IRON_GUARD.get())) return;
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof LivingAttackEvent) {
            float mightDiff = (float) MathHelper.clamp(3 / caster.getAttributeValue(Attributes.ATTACK_SPEED), 0.2, 5);// Math.max(CombatData.getCap(caster).getMight() - CombatData.getCap(target).getMight(), 0);
            CombatData.getCap(target).consumePosture(mightDiff);
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 16 + (int) (mightDiff * 2)));
        }
    }
}
