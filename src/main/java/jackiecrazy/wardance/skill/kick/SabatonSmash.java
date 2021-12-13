package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;

import java.awt.*;

public class SabatonSmash extends Kick {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    protected void additionally(LivingEntity caster, LivingEntity target) {
        CombatData.getCap(caster).consumePosture(1);
        CombatUtils.knockBack(target, caster, 1.6f, true, false);
        mark(caster, target, 30);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        if (target.collidedHorizontally) {
            removeMark(target);
            CombatData.getCap(target).consumePosture(caster.getTotalArmorValue() / 4f);
            target.attackEntityFrom(new CombatDamageSource("fallingBlock",caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), caster.getTotalArmorValue() / 4f);
        }
        return super.markTick(caster, target, sd);
    }
}
