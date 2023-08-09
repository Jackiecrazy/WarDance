package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;

public class Kick extends Skill {
    private final HashSet<String> tag = makeTag("physical", "melee", "noDamage", "boundCast", "normalAttack", "countdown", "rechargeWithAttack");
    private final HashSet<String> no = makeTag((("normalAttack")));

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.kick;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public HashSet<String> getTags() {
        return offensivePhysical;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        attackCooldown(procPoint, caster, stats);
        if (procPoint instanceof StunEvent e && state == STATE.ACTIVE && this == WarSkills.TRAMPLE.get() && e.getPhase() == EventPriority.LOWEST) {
            e.setKnockdown(true);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = GeneralUtils.raytraceLiving(caster, distance());
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target != null && cast(caster, target, -999)) {
            float amount = 4 * prev.getEffectiveness();
            amount = amount * 1.5f * prev.getEffectiveness() > CombatData.getCap(target).getPosture() ? amount * 1.5f * prev.getEffectiveness() : amount;
            CombatData.getCap(target).consumePosture(caster, amount);
            if (caster instanceof Player)
                ((Player) caster).sweepAttack();
            additionally(caster, target, prev);
            target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2 * prev.getEffectiveness());
            if (target.getLastHurtByMob() == null)
                target.setLastHurtByMob(caster);
            caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 4);
            return true;
        }
        return boundCast(prev, from, to);
    }

    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
    }

    protected int distance() {
        return 3;
    }

    public static class Backflip extends Kick {
        protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
            final Vec3 vec = caster.position().vectorTo(target.position());
            final Vec3 noy = new Vec3(vec.x, 0, vec.z).normalize().scale(-1);
            caster.setDeltaMovement(caster.getDeltaMovement().add(noy.x, 0.4, noy.z));
            caster.hurtMarked = true;
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addRank(0.3f);
            cap.addPosture(0.3f * sd.getEffectiveness() * (cap.getPosture() / cap.getMaxPosture()));
        }
    }
}
