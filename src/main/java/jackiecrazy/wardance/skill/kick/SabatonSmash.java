package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.NewCombatCapability;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.MobilityUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;

public class SabatonSmash extends Kick {

    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
        MobilityUtils.knockBack(target, caster, 1.6f, true, false);
        mark(caster, target, 2, 0, CombatData.getCap(caster).consumeSpirit(NewCombatCapability.FINISHER_THRESHOLD));
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (target.horizontalCollision) {
            removeMark(target);
            if (caster != null) {
                if(CombatData.getCap(target).consumePosture(caster, caster.getArmorValue() * SkillUtils.getSkillEffectiveness(caster), 0, sd.isCondition())!=0)
                    completeChallenge(caster);
                target.hurt(new CombatDamageSource(caster).setDamageTyping(FootworkDamageArchetype.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setProcAttackEffects(true), caster.getArmorValue() * SkillUtils.getSkillEffectiveness(caster) / 4f);

            }
        }
        return markTickDown(sd);
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }
}
