package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.advancement.WarAdvancements;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class GrenadeEntity extends BaseballEntity {
    public GrenadeEntity(EntityType<? extends FlyingItemEntity> type,
                         Level level) {
        super(type, level);
        activeSkill= WarSkills.CURSED_PALMS.get();
    }
    @Override
    protected void landed(@Nullable LivingEntity on) {
        Entity t = getTetheringEntity(), c = getOwner();
        if (t instanceof LivingEntity target && c instanceof LivingEntity caster) {
            caster.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, target.position(), 0, 1, SkillColors.purple.getColor(), 0);
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60));
            AreaEffectCloud areaeffectcloud = new AreaEffectCloud(caster.level(), target.getX(), target.getY(), target.getZ());
            areaeffectcloud.setOwner(caster);
            areaeffectcloud.setRadius(5.0F * SkillUtils.getSkillEffectiveness(caster));
            areaeffectcloud.setDuration((int) (140 * SkillUtils.getSkillEffectiveness(caster)));
            int counter = 0;
            for (MobEffectInstance mobeffectinstance : target.getActiveEffects()) {
                if (!mobeffectinstance.getEffect().isBeneficial()) {
                    areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
                    counter++;
                }
            }
            if (caster instanceof ServerPlayer sp &&counter >= 4)
                WarAdvancements.CHALLENGE_ONLY.trigger(sp, CasterData.getCap(caster).getSkillData(activeSkill).orElse(SkillData.DUMMY));
            if(friendly)
                target.removeAllEffects();
            caster.level().addFreshEntity(areaeffectcloud);
        }
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        alreadyHit.add(getTetheringEntity());
        return super.onHitEntity(targets);
    }
}
