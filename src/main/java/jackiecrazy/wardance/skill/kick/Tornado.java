package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.ParticleUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeMod;

import java.util.HashSet;

public class Tornado extends Kick {
    private final HashSet<String> tag = makeTag("physical", "melee", "sweep", "boundCast", "normalAttack", "countdown", "rechargeWithAttack");
    private final HashSet<String> no = makeTag("normalAttack", "sweep");

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && cast(caster)) {
            for (Entity t : caster.level.getEntities(caster, caster.getBoundingBox().inflate(caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get())), (a -> !TargetingUtils.isAlly(a, caster))))
                if (t instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) t;
                    CombatData.getCap(target).consumePosture(caster, 4);
                    target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                    if (target.getLastHurtByMob() == null)
                        target.setLastHurtByMob(caster);
                }
            //play little sweep animation
            caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            int radius = 1;
            ParticleUtils.playSweepParticle(caster, 30, radius, 0.1);
            ParticleUtils.playSweepParticle(caster, 120, radius, 0.1);
            ParticleUtils.playSweepParticle(caster, 210, radius, 0.1);
            ParticleUtils.playSweepParticle(caster, 300, radius, 0.1);
            radius = 2;
            ParticleUtils.playSweepParticle(caster, 45, radius, caster.getBbHeight() / 2);
            ParticleUtils.playSweepParticle(caster, 135, radius, caster.getBbHeight() / 2);
            ParticleUtils.playSweepParticle(caster, 225, radius, caster.getBbHeight() / 2);
            ParticleUtils.playSweepParticle(caster, 315, radius, caster.getBbHeight() / 2);
            radius = 3;
            ParticleUtils.playSweepParticle(caster, 60, radius, caster.getBbHeight());
            ParticleUtils.playSweepParticle(caster, 150, radius, caster.getBbHeight());
            ParticleUtils.playSweepParticle(caster, 240, radius, caster.getBbHeight());
            ParticleUtils.playSweepParticle(caster, 330, radius, caster.getBbHeight());
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 4);
            return true;
        }
        return boundCast(prev, from, to);
    }

    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {

    }

}
