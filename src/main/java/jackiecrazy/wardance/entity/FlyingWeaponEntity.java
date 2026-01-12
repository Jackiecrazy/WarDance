package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
    protected WeaponStats.SweepInfo cacheInfo;
    protected WeaponStats.AttackType state;

    public FlyingWeaponEntity(EntityType<? extends FlyingItemEntity> type, Level level) {
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
    public boolean isPickable() {
        return isReal() && !this.isRemoved();
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

    public boolean isReal() {
        return getState() == STATE.THROW_NATURAL || getState() == STATE.THROW_TRACK;
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        if (!ent.level().isClientSide && ent instanceof Player p && p.getMainHandItem().isEmpty() && isReal()) {
            return pickup(p);
        }
        return false;
    }

    public boolean pickup(Player p) {
        //if holding nothing, prioritize this slot
        int slot = -1;
        if (p.getMainHandItem().isEmpty()) slot = p.getInventory().selected;
        else if (p.getOffhandItem().isEmpty()) slot = Inventory.SLOT_OFFHAND;
        boolean success = p.getAbilities().instabuild;
        CombatUtils.allowCombatHotbarPickup = true;
        if (!success)
            success = p.getInventory().add(slot, getPickResult());
        CombatUtils.allowCombatHotbarPickup = false;
        if (success)
            this.remove(RemovalReason.KILLED);
        return !success;
    }

    @Override
    public double getTetherLength() {
        return 0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        //tag.putDouble("xROT", this.getXRot());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
//        if(isReal()){
//            todo fix orientation
//        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isAlive()) {
            if (isIdle()) {//tied to the owner
//                if (getOwner() == null) remove(RemovalReason.DISCARDED);
//                boolean valid = false;
//
//                //todo this check makes grabbing blocks out of the environment not work
//                for (InteractionHand h : InteractionHand.values())
//                    if (FlyingWeaponData.getCap(getOwner()).getWeapon(h) == this) valid = true;
//
//                if (tickCount>100)//reasonably sure the player doesn't need it anymore
//                    remove(RemovalReason.DISCARDED);
            } else {
                if (getOwner() instanceof Player p) {
                    if (p.distanceToSqr(this) > 64 * 64)
                        pickup(p);
                }
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
        targets = targets.stream().filter(tg -> tg != owner && !alreadyHit.contains(tg) && !TargetingUtils.isAlly(tg, owner) && !tg.isInvulnerable()).toList();
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
                        CombatData.getCap(elb).pin(10);
                    }
                }
                if (!alreadyHit.isEmpty()) CombatData.getCap(e).tickProc("oncePerSweep");
                CombatData.getCap(e).tickProc("noFinisherCharge");
                target.invulnerableTime = 0;
                GeneralUtils.attack(e, target);
                ret = true;
                alreadyHit.add(target);
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

    public void yeet(Vec3 to) {
        setHeldItem(getHeldItem().copyWithCount(1));
        getEntityData().set(CURRENT_STATE, STATE.THROW_NATURAL);
        setShouldRender(FlyingWeaponEffect.WEAPON);
        entityData.set(IDLE_TICK, 0);
        setDeltaMovement(to.subtract(position()).normalize().scale(2));
        //setTetheringEntity(getOwner());
        setTransitioning(false);
        setInteractionRange(0.5f);
        cacheInfo = WeaponStats.SweepInfo.BREACHER;
        //setDeltaMovement(new Vec3(0,1,0));
    }

    @Override
    protected void updateClientData() {
        super.updateClientData();
    }

    @Override
    public boolean isIdle() {
        return super.isIdle();
    }

    @Override
    protected void returnToIdle(int ticks) {
        super.returnToIdle(ticks);
        setTransitioning(true);
    }

    @Override
    protected boolean updateMotionTargets(boolean forceskip) {
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
                //simple basic attacks
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

    //TODO add render for aiming
    enum AIM {
        IDLE, FLY
    }
}
