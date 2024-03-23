package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeMod;

import java.awt.*;
import java.util.HashSet;

public class Tornado extends Kick {
    private final HashSet<String> tag = makeTag("physical", "melee", "sweep", "boundCast", "normalAttack", "countdown", "rechargeWithAttack");
    private final HashSet<String> no = makeTag("normalAttack", "sweep");

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && cast(caster)) {
            int counter = 0;
            for (Entity t : caster.level().getEntities(caster, caster.getBoundingBox().inflate(3), (a -> !TargetingUtils.isAlly(a, caster))))
                if (t instanceof LivingEntity) {
                    counter++;
                    LivingEntity target = (LivingEntity) t;
                    CombatData.getCap(target).consumePosture(caster, 4);
                    target.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setSkillUsed(this).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                    if (target.getLastHurtByMob() == null)
                        target.setLastHurtByMob(caster);
                }
            if (counter >= 6)
                completeChallenge(caster);
            //play little sweep animation
            caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
            for (int radius = 1; radius < 4; ++radius)
                ParticleUtils.playSweepParticle(FootworkParticles.CIRCLE.get(), caster, caster.position(), 0, radius, Color.CYAN, 0.1 + radius);
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
