package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.FrameEffects;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.MovementUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FlyingWeaponEntity extends FlyingItemEntity {
    protected final List<Entity> alreadyHit = new ArrayList<>();
    protected HitInfo cacheInfo;
    protected WeaponStats.AttackType state;
    private boolean fading = false;

    public FlyingWeaponEntity(EntityType<? extends FlyingItemEntity> type, Level level) {
        //keep hitframes separate and logged here.
        //keep defense frames here?
        super(type, level);
        setEffect(FlyingWeaponEffect.BIG_SHADOW, false);
        setInvulnerable(true);
        //wasIdle=false;
    }

    @Nullable
    public HitInfo getInfo() {
        return cacheInfo;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 32 * 32;
    }

    public ItemStack getPickResult() {
        return getHeldItem().copy();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public double getTetherLength() {
        return 0;
    }

    public void invalidateWhenDone() {
        fading = true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fading = tag.getBoolean("fading");
        //tag.putDouble("xROT", this.getXRot());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("fading", fading);
//            todo fix orientation
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isAlive()) {
            if (isIdle()) {//tied to the owner
                if (getOwner() == null || fading) remove(RemovalReason.UNLOADED_WITH_PLAYER);
                if (this.getClass() == FlyingWeaponEntity.class && tickCount % 100 == 40) {
                    boolean valid = false;

                    //todo this check makes grabbing blocks out of the environment not work
                    for (InteractionHand h : InteractionHand.values())
                        if (FlyingWeaponData.getCap(getOwner()).getWeapon(h) == this) valid = true;
                    if (!valid)
                        invalidateWhenDone();
                }
            } else {

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
    protected boolean onHitEntity(List<Entity> targets) {
        boolean ret = false;
        //don't do any of this on the client because that's not good:tm:
        if (level().isClientSide()) return false;
        //normal hits skip hit calculation
        if (getInfo() == null) return false;
        targets = targets.stream().filter(tg -> tg != owner && !alreadyHit.contains(tg) && !TargetingUtils.isAlly(tg, owner) && !tg.getType().is(MobSpecs.IGNORED_BY_SWEEP) && !tg.isInvulnerable()).toList();
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
                    if (!getInfo().canBreach()) {
                        CombatData.getCap(elb).pin(10);
                    }
                }
                if (!alreadyHit.isEmpty()) CombatData.getCap(e).tickProc("oncePerSweep");
                //CombatData.getCap(e).tickProc("qiSpent");
                target.invulnerableTime = 0;
                GeneralUtils.attack(e, target);
                ret = true;
                alreadyHit.add(target);
                extraOnHit(e, target);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            CombatUtils.quickSwap(e, main);
            e.attackStrengthTicker = ticks;
            WeaponStats.info_override = null;
        }
        return ret;
    }

    protected void extraOnHit(LivingEntity e, Entity target) {
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        setDeltaMovement(Vec3.ZERO);
        setPos(location);
        ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), this, this.position(), 0, 3, Color.WHITE, 0);
        List<Entity> selfTarget = level().getEntities(getOwner(), getBoundingBox().inflate(0.3f), e -> e != getOwner() && e.isAlive() && e.isAttackable());
        onHitEntity(selfTarget);
    }

    @Override
    protected void handleBlockCollisions() {
        if (getState() == STATE.THROW_NATURAL) {
            if (getDeltaMovement().lengthSqr() == 0) return;

            BlockHitResult hit = level().clip(new ClipContext(position(), position().add(getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hit.getBlockPos();
                Direction hitFace = hit.getDirection();
                onHitBlock(blockPos, hitFace, hit.getLocation());
            }
        }// else super.handleBlockCollisions();
    }

    public void yeet(Vec3 to, double strength) {
        setHeldItem(getHeldItem().copyWithCount(1));
        getEntityData().set(CURRENT_STATE, STATE.THROW_NATURAL);
        //setEffect(FlyingWeaponEffect.WEAPON);
        entityData.set(IDLE_TICK, 0);
        setDeltaMovement(to.subtract(position()).normalize().scale(strength));
        //setTetheringEntity(getOwner());
        setIntangible(false);
        setInteractionRange(1f);

//        if (CombatData.getCap(getOwner()).consumeSpirit(CombatData.getCap(getOwner()).getMaxSpirit()))
//            cacheInfo = HitInfo.BREACH;
//        else cacheInfo = HitInfo.THROWN;
        //todo remove
    }

    @Override
    protected void updateClientData() {
        super.updateClientData();
    }

    @Override
    public boolean isIdle() {
        return super.isIdle() && getState() == STATE.FOLLOW;
    }

    @Override
    protected void returnToIdle(int ticks) {
        super.returnToIdle(ticks);
        setIntangible(true);//do I need this?
    }

    @Override
    protected void updateFrameEffects(FrameEffects effects) {
        currentEffects = effects;
        if (effects != null) {
            setIntangible(false);
            if (effects.getRange() >= 0) setInteractionRange((float) effects.getRange());
            if (effects.getEffects() != null)
                setEffect(effects.getEffects().toArray(new FlyingWeaponEffect[0]));
            cacheInfo = effects.getHit();
            if (cacheInfo != null && getOwner() != null)
                CombatUtils.applyFrames(getOwner(), cacheInfo);
            if (effects.reset_hit())
                alreadyHit.clear();
            LivingEntity e = getOwner();
            if (e != null)
                MovementUtils.applyVelocity(effects.getVelocity(), e, effects.isSetVelocity());
        }
    }

    @Override
    protected boolean updateMotionTargets(boolean forceskip) {
        //true if a new move started
        boolean ret = super.updateMotionTargets(forceskip);
        if (ret) {
            alreadyHit.clear();
        }
        MotionManager motion = moveQueue.peek();
//        if (motion instanceof WeaponMotionManager wmm) {
//            setInteractionRange((float) wmm.range());
//            setIntangible(false);
//            cacheInfo = wmm.info();
//        } else
        if (moveQueue.isEmpty()) {
            //return on a transition frame
            setEffect(FlyingWeaponEffect.WEAPON);
            setIntangible(true);
        }

//        if (intangible()) {
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

    //TODO add render for aiming
    enum AIM {
        IDLE, FLY
    }
}
