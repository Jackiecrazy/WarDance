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
        if (target.horizontalCollision) {
            removeMark(target);
            if (caster != null) {
                if(CombatData.getCap(target).consumePosture(caster.getArmorValue() * SkillUtils.getSkillEffectiveness(caster) / 4f)!=0)
                    completeChallenge(caster);
                target.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setProcAttackEffects(true), caster.getArmorValue() * SkillUtils.getSkillEffectiveness(caster) / 4f);

            }
        }
        return markTickDown(sd);
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }
}
