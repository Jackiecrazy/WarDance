package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public class Submission extends Grapple {

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, SkillData stats) {
        final float armor = caster.getArmorValue() / 4f;
        float consume = 7 + armor;
        consume *= stats.getEffectiveness();
        caster.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        if (CombatData.getCap(target).consumePosture(caster, consume, 0, true) < 0) {
            CombatData.getCap(caster).addSpirit(1);
        }
        CombatData.getCap(caster).addPosture(armor);
    }
}
