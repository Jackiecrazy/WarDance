package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FlyingWeaponEntity extends FlyingItemEntity {
    private final List<Entity> alreadyHit = new ArrayList<>();
    private WeaponStats.SweepInfo cacheInfo;
    private WeaponStats.SWEEPSTATE state;

    public FlyingWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        //keep hitframes separate and logged here.
        //keep defense frames here?
        super(type, level);
        setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
    }

    @Nullable
    public WeaponStats.SweepInfo getInfo() {
        return cacheInfo;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (isIdle()) {
                if (getOwner() == null)
                    remove(RemovalReason.DISCARDED);
                boolean valid = false;
                for (InteractionHand h : InteractionHand.values())
                    if (FlyingWeaponData.getCap(getOwner()).getWeapon(h) == this)
                        valid = true;
                if (!valid)//reasonably sure the player doesn't need it anymore
                    remove(RemovalReason.DISCARDED);
            }
        }
        //clear trail history on tick 1

//        if (level() instanceof ServerLevel s&&!isIdle()&&tickCount%3==0) {
//            Vec3 vec= getPosition(0);
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.WHITE),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//            vec= getPosition(1);
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.BLACK),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//            vec= getPosition(0).add(getViewVector(0).scale(getInteractionRange()));
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.GREEN),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//            vec= getPosition(1).add(getViewVector(1).scale(getInteractionRange()));
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.BLUE),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//        }
    }

    @Override
    protected void onHitEntity(List<Entity> targets) {
        //don't do any of this on the client because that's not good:tm:
        if (level().isClientSide()) return;
        //normal hits skip hit calculation
        if (getInfo() == null) return;
        targets = targets.stream().filter(tg -> tg != owner &&
                !alreadyHit.contains(tg) &&
                !TargetingUtils.isAlly(tg, owner) &&
                !tg.isInvulnerable()).toList();
        LivingEntity e = getOwner();
        int ticks = e.attackStrengthTicker;
        ItemStack main = e.getMainHandItem();
        try {
            CombatUtils.quickSwap(e, getHeldItem());
            WeaponStats.info_override = getInfo();
            for (Entity target : targets) {
                e.attackStrengthTicker = 99999;
                //temporary pin code
                if (target instanceof LivingEntity elb) {
                    if (getInfo().canBreach()) {
                        CombatData.getCap(elb).pin(0);
                    } else {
                        CombatData.getCap(elb).pin(20);
                        //CombatData.getCap(elb).startRecordingDamage(20);
                        TimeSlowData.getCap(elb).alterSpeed(20, 0.1);
                    }
                }
                if (!alreadyHit.isEmpty())
                    CombatData.getCap(e).tickProc("oncePerSweep");
                CombatData.getCap(e).tickProc("noFinisherCharge");
                target.invulnerableTime = 0;
                GeneralUtils.attack(e, target);
                alreadyHit.add(target);
                if (target instanceof LivingEntity elb && getInfo().canBreach()) {
                    CombatData.getCap(elb).stopRecording(new CombatDamageSource(e).setDamageTyping(FootworkDamageArchetype.PHYSICAL));
                    TimeSlowData.getCap(elb).resetSpeed();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            CombatUtils.quickSwap(e, main);
            e.attackStrengthTicker = ticks;
            WeaponStats.info_override = null;
        }
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        //todo impact sweep, store the sweep type and on hit stats to overwrite on combathandler
    }

    @Override
    protected void updateClientData() {
        if (transitioning())
            trailHistory.clear();
        renderLag = 0;
        super.updateClientData();
    }

    @Override
    protected void returnToIdle(int ticks) {
        super.returnToIdle(ticks);
    }

    @Override
    protected boolean updateMotionTargets(boolean forceskip) {
        //fixme gets stuck
        //true if a new move started
        boolean ret = super.updateMotionTargets(forceskip);
        if (ret) {
            alreadyHit.clear();
        }
        MotionManager motion = moveQueue.peek();
        if (motion instanceof WeaponMotionManager wmm) {
            setInteractionRange((float) wmm.range());
            setTransitioning(false);
            cacheInfo = wmm.info();

            if (getInfo() == null) {
                //special case, do not render big weapon
                setShouldRender(FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON);
            } else {
                //attacking, on a finisher
                setShouldRender(FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
        } else {
            //return on a transition frame
            setShouldRender(FlyingWeaponEffect.WEAPON);
            setTransitioning(true);
        }

//        if (transitioning()) {
//            //not attacking
//            setShouldRender(FlyingWeaponEffect.TRAIL, false);
//            setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
//            setShouldRender(FlyingWeaponEffect.WEAPON, true);
//        }
        if (!isIdle()) {
            //setUniversalOffset(Vec3.ZERO);
        } else if (getOwner() != null) {
            setInteractionRange((float) getOwner().getAttributeValue(ForgeMod.ENTITY_REACH.get()));
        }
        return ret;
    }
}
