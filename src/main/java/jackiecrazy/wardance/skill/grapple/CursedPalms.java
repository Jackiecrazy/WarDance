package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CursedPalms extends Throw {
    @Override
    protected void targetCollision(LivingEntity caster, LivingEntity target, SkillData stats, Entity collide) {
        caster.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, target.position(), 0, 1, getColor(), 0);
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60));
        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(caster.level, target.getX(), target.getY(), target.getZ());
        areaeffectcloud.setOwner(caster);
        areaeffectcloud.setRadius(5.0F * SkillUtils.getSkillEffectiveness(caster));
        areaeffectcloud.setDuration((int) (140 * SkillUtils.getSkillEffectiveness(caster)));
        for (MobEffectInstance mobeffectinstance : target.getActiveEffects()) {
            if (!mobeffectinstance.getEffect().isBeneficial())
                areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
        }
        caster.level.addFreshEntity(areaeffectcloud);
    }
}
