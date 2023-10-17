package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class SelfishMascot extends Skill {
    private static final UUID uid = UUID.fromString("CC5AF142-2BD2-4215-B636-2605AED11727");//this is the same as the unluck effect
    private static final AttributeModifier unluck = new AttributeModifier(uid, "selfish mascot", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);//this is the same as the unluck effect


    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        boolean prev = stats.isCondition();
        if (caster.tickCount % 40 == 0) {
            double stacks = 0;
            for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(6))) {
                if (e == caster) continue;
                if (caster.getHealth() > caster.getMaxHealth() / 2) {
                    stats.flagCondition(false);
                    //give luck
                    if (TargetingUtils.isAlly(caster, e))
                        e.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1));
                } else {
                    stats.flagCondition(true);
                    //absorb luck in a really weird and funky way
                    e.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 60));
                    e.getAttribute(Attributes.LUCK).removeModifier(uid);
                    stacks += e.getAttributeValue(Attributes.LUCK);
                    if(stacks>=20)
                        completeChallenge(caster);
                    e.getAttribute(Attributes.LUCK).addPermanentModifier(unluck);
                    SkillUtils.modifyAttribute(caster, Attributes.LUCK, uid, stacks * SkillUtils.getSkillEffectiveness(caster), AttributeModifier.Operation.ADDITION);
                }
            }
        }
        return prev != stats.isCondition();
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.LUCK).removeModifier(uid);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return stats.isCondition();
    }
}
