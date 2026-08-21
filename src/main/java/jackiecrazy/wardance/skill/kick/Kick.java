package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
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
    public float spiritGain(LivingEntity caster) {
        return 9;
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

//    @Override
//    public boolean fakeMark(LivingEntity caster, LivingEntity target, SkillData stats) {
//        return getDamage(stats, target) >= CombatData.getCap(target).getPosture();
//    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        attackCooldown(procPoint, caster, stats);
    }

    public int getAimRange(LivingEntity caster, SkillData sd) {
        return 3;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster, getAimRange(caster, prev));
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target != null && cast(caster, target)) {
            CombatUtils.kick(caster, target, false);
            additionally(caster, target, prev);
            return true;
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 4);
            return true;
        }
        return boundCast(prev, from, to);
    }

    private float getDamage(SkillData prev, LivingEntity target) {
        return 4 * prev.getEffectiveness();
    }

    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
    }

    public static class Backflip extends Kick {

        protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
            final Vec3 vec = caster.position().vectorTo(target.position());
            final Vec3 noY = new Vec3(vec.x, 0, vec.z).normalize().scale(-1);
            caster.setDeltaMovement(caster.getDeltaMovement().add(noY.x, 0.4, noY.z));
            caster.hurtMarked = true;
            final ICombatCapability cap = CombatData.getCap(caster);
            if (caster.getY() > 320 && target instanceof Phantom)
                completeChallenge(caster);
            StylishData.getCap(caster).addCombo(0.1f, getRegistryName().toString());
            cap.addPosture(0.3f * sd.getEffectiveness() * (cap.getPosture() / cap.getMaxPosture()));
            AerialModeData.getCap(caster).alterGravity(50, 0.3);
            AerialModeData.getCap(caster).setAerialMode(50);
        }
    }
}
