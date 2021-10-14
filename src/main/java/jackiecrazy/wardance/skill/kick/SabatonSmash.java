package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

import java.awt.*;

public class SabatonSmash extends Kick {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    protected void additionally(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(caster).consumePosture(1);
        CombatUtils.knockBack(target, caster, 1.6f, true, false);
        afflict(caster, target, 30);
    }

    @Override
    public boolean statusTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        if (target.collidedHorizontally) {
            endAffliction(target);
            CombatData.getCap(target).consumePosture(caster.getTotalArmorValue() / 4f);
            target.attackEntityFrom(DamageSource.FALLING_BLOCK, caster.getTotalArmorValue() / 4f);
        }
        return super.statusTick(caster, target, sd);
    }
}
