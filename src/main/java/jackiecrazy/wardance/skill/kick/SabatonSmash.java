package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;

public class SabatonSmash extends Kick {

    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
        CombatData.getCap(caster).consumePosture(1);
        CombatUtils.knockBack(target, caster, 1.6f, true, false);
        mark(caster, target, 30);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        if (target.horizontalCollision) {
            removeMark(target);
            if (caster != null) {
                CombatData.getCap(target).consumePosture(caster.getArmorValue() * SkillUtils.getSkillEffectiveness(caster) / 4f);
                target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), caster.getArmorValue() * SkillUtils.getSkillEffectiveness(caster) / 4f);

            }
        }
        return super.markTick(caster, target, sd);
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }
}
