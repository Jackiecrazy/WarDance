package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.api.CombatDamageSource;
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
        if (target.horizontalCollision) {
            removeMark(target);
            CombatData.getCap(target).consumePosture(caster.getArmorValue() / 4f);
            target.hurt(new CombatDamageSource("fallingBlock",caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), caster.getArmorValue() / 4f);
        }
        return super.markTick(caster, target, sd);
    }
}
