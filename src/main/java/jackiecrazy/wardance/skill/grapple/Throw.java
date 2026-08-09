package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.skill.BaseballEntity;
import jackiecrazy.wardance.entity.skill.GrenadeEntity;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Throw extends Skill {
    private final HashSet<String> unarm = makeTag(SkillTags.offensive, SkillTags.physical, SkillTags.unarmed);

//    @Override
//    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
//        if (state == STATE.HOLSTERED && ) {
//            if(procPoint instanceof PlayInteractionEvent.Pre pie)
//                pie.setInteraction(WeaponStats.getSweepInfo(ItemStack.EMPTY, caster, WeaponStats.AttackType.STANDING, true, InteractionHand.MAIN_HAND));
//            if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() != caster && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
//                if(CombatData.getCap(target).isStunned())
//
//            } else if (procPoint instanceof StunEvent se && se.getEntity() == target && se.getPhase() == EventPriority.LOWEST)
//                performEffect(caster, target, stats);
//        }
//        attackCooldown(procPoint, caster, stats);
//    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.ACTIVE) {
            if (FlyingWeaponData.getCap(caster).getHeldBlock() == null) {
                LivingEntity target = SkillUtils.aimLiving(caster);
                if (target != null) {
                    boolean stunned = CombatData.getCap(target).isStunned();
                    boolean friendly = (TargetingUtils.isAlly(target, caster) && (!(target instanceof Player p) || p.isShiftKeyDown()));
                    if ((stunned || friendly) && cast(caster)) {
                        CombatData.getCap(target).bindHands(40);
                        caster.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                        //auto pickup for friendly throw
                        ThrownWeaponEntity baseball = createAnchor(caster, friendly);
                        baseball.setOwner(caster);
                        baseball.moveTo(caster.getX(), caster.getY() + caster.getBbHeight() + 1, caster.getZ());
                        if (caster instanceof ServerPlayer p)
                            baseball.pickup(p);
                        baseball.drag(target, 90, 1000);
                        baseball.setGravity(-0.08);
                        baseball.setInteractionRange(1);
                        baseball.setIntangible(true);
                        if (!caster.level().isClientSide())
                            caster.level().addFreshEntity(baseball);
                        return true;
                    }
                }
            }
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 1);
        }
        return boundCast(prev, from, to);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return cooldownTick(stats);
    }

    protected @NotNull ThrownWeaponEntity createAnchor(LivingEntity caster, boolean friendly) {
        return new BaseballEntity(WarEntities.BASEBALL.get(), caster.level()).setFriendly(friendly);
    }

    @Override
    public HashSet<String> getTags() {
        return unarm;
    }

    public static class CursedPalms extends Throw {
        @Override
        protected @NotNull ThrownWeaponEntity createAnchor(LivingEntity caster, boolean friendly) {
            return new GrenadeEntity(WarEntities.GRENADE.get(), caster.level()).setFriendly(friendly);
        }
    }
}
