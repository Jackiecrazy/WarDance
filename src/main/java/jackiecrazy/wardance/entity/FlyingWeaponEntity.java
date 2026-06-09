package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.DefenseType;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.move.motionframe.render.RenderItemGroup;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.MovementUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.api.IDrag;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4d;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlyingWeaponEntity extends FlyingItemEntity implements IDrag {
    protected static final EntityDataAccessor<Float> DRAG_STRENGTH = SynchedEntityData.defineId(FlyingWeaponEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> DRAG_TIME = SynchedEntityData.defineId(FlyingWeaponEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<MotionManager> DRAG_POSE = SynchedEntityData.defineId(FlyingWeaponEntity.class, MotionManager.SERIALIZER);
    protected static final EntityDataAccessor<Vector3f> DRAG_OFFSET = SynchedEntityData.defineId(FlyingWeaponEntity.class, EntityDataSerializers.VECTOR3);
    protected final List<Entity> alreadyHit = new ArrayList<>();
    protected HashMap<Entity, Integer> dragging = new HashMap<>();
    protected HitInfo cacheInfo;
    protected HitEffects terrainEffects=null;
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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DRAG_STRENGTH, 3f);
        this.entityData.define(DRAG_TIME, 0);
        this.entityData.define(DRAG_OFFSET, new Vector3f(0, 0, 0));
        this.entityData.define(DRAG_POSE, new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)), 5));
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
            final int dragging = getEntityData().get(DRAG_TIME) - 1;
            if (dragging < 0 && shouldDrag()) unDrag();
            getEntityData().set(DRAG_TIME, dragging);
            if (isIdle()) {//tied to the owner
                if (getOwner() == null || fading) remove(RemovalReason.UNLOADED_WITH_PLAYER);
                if (this.getClass() == FlyingWeaponEntity.class && tickCount % 100 == 40) {
                    flushTrailHistory();
                    boolean valid = false;
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
        targets = targets.stream().distinct().filter(tg -> tg != owner && !alreadyHit.contains(tg) && !TargetingUtils.isAlly(tg, owner) && !tg.getType().is(MobSpecs.IGNORED_BY_SWEEP) && !tg.isInvulnerable()).toList();
        LivingEntity e = getOwner();
        int ticks = e.attackStrengthTicker;
        if (targets.isEmpty()) return ret;
        ItemStack main = e.getMainHandItem();
        try {
            CombatUtils.quickSwap(e, getHeldItem());
            WeaponStats.info_override = getInfo();
            for (Entity target : targets) {
                e.attackStrengthTicker = 99999;
                if (!alreadyHit.isEmpty()){
                    CombatData.getCap(e).tickProc("oncePerAttack");
                    CombatData.getCap(e).tickProc("durabilityConsumed");
                }
                alreadyHit.add(target);//it used to be lower but this allows you to chain attacks properly
                //CombatData.getCap(e).tickProc("oncePerAttack");
                target.invulnerableTime = 0;
                CombatData.getCap(e).setOffhandAttack(flipClientRender());
                CombatDamageSource cds = damageSource();
                GeneralUtils.attack(e, target, cds);
                ret = true;
//                alreadyHit.add(target);
                extraOnHit(e, target);
                CombatData.getCap(e).setOffhandAttack(false);
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

    protected CombatDamageSource damageSource() {
        return new CombatDamageSource(getOwner(), this, position()).flagBreach(false).flag(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).setAttackingHand(flipClientRender()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND).setDamageDealer(getHeldItem()).setProcNormalEffects(true).setProcAttackEffects(true).setDamageTyping(FootworkDamageArchetype.PHYSICAL);
    }


    protected void extraOnHit(LivingEntity e, Entity target) {

    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if (intangible()) return;
        if(terrainEffects!=null) {
            terrainEffects.runEffects(getOwner(), this, flipClientRender()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND, getHeldItem());
            terrainEffects=null;//reset after one impact until next terrain effect comes in
        }
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
        } else super.handleBlockCollisions();
    }

    public void yeet(Vec3 to, double strength) {
        //needed because setting the held item resets the cosmetic item.
        RenderItemGroup temp = getCosmeticItem();
        setHeldItem(getHeldItem().copyWithCount(1));
        setCosmeticItem(temp);
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
    public @org.jetbrains.annotations.Nullable Entity getTetheredEntity() {
        return getOwner();
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
        setDeltaMovement(Vec3.ZERO);
        setIntangible(true);//this is needed to prevent the weapon hitting stuff when idle
        //unDrag();
    }

    @Override
    protected void updateFrameEffects(FrameEffects effects) {
        currentEffects = effects;
        if (effects != null&& getOwner()!=null) {
            setIntangible(false);
            if (effects.getRange() >= 0) setInteractionRange((float) effects.getRange());
            //special handling for vector adjustments on initial orientation lock
            if (effects.getEffects() != null) {
                if (effects.getEffects().contains(FlyingWeaponEffect.LOCK_ORIENTATION)) {
                    lockLook(stateDependentPositionLook().getB().multiply(effects.modify_initial_rotation().x, effects.modify_initial_rotation().y, effects.modify_initial_rotation().z));
                }
                setEffect(effects.getEffects().toArray(new FlyingWeaponEffect[0]));
            }
            cacheInfo = effects.getHit();
            if (effects.reset_hit())
                alreadyHit.clear();
            if (effects.shouldUndrag())
                unDrag();
            LivingEntity e = getOwner();
            if (e != null)
                effects.runEffects(e, e, flipClientRender()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND, getHeldItem());
            if(effects.getDisplayItems()!=null)
                setCosmeticItem(effects.getDisplayItems().resolve(new ArgumentContext(getOwner(), getOwner())));
            if(effects.getColor()!=null)
                setTrailColor(effects.getColor());
            if(effects.getTerrainEffects()!=null)
                terrainEffects=effects.getTerrainEffects();
        }
    }

    @Override
    public void clearPath() {
        super.clearPath();
        alreadyHit.clear();
    }

    @Override
    protected void updateSpin(MotionManager cur) {
        super.updateSpin(cur);
    }

    @Override
    protected boolean updateMotionTargets(boolean forceskip) {
        //true if a new move started
        boolean ret = super.updateMotionTargets(forceskip);
        if (ret) {
            alreadyHit.clear();
        }
        if (moveQueue.isEmpty()) {
            //return on a transition frame
            setEffect(FlyingWeaponEffect.WEAPON);
            setIntangible(true);//fixme becoming intangible here makes weapons not have the chance to proc hiteffects on the last frame
            //solution: move these to the very very end of tick
            //except that will cause all thrown items to break.
            //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            setCosmeticItem(getHeldItem());
            unlock();
        }
        if (!isIdle()) {
        } else if (getOwner() != null) {
            setInteractionRange((float) getOwner().getAttributeValue(ForgeMod.ENTITY_REACH.get()));
            //setTrailColor(DEFAULT_TRAIL_COLOR);
        }
        return ret;
    }

    public void inheritDrag(FlyingWeaponEntity from) {
        setTetheringEntity(from.getTetheringEntity());
        alreadyHit.add(getTetheringEntity());
        getEntityData().set(DRAG_TIME, from.getEntityData().get(DRAG_TIME));
        getEntityData().set(DRAG_OFFSET, from.getEntityData().get(DRAG_OFFSET));
        getEntityData().set(DRAG_POSE, from.getEntityData().get(DRAG_POSE));
        getEntityData().set(DRAG_STRENGTH, from.getEntityData().get(DRAG_STRENGTH));
        from.unDrag();
    }

    public void drag(Entity target, double strength, int duration) {
        if ((getTetheringEntity() == target&&shouldDrag()) || strength < 0) return;
        setTetheringEntity(target);
        getEntityData().set(DRAG_TIME, duration);
        int snapTime = 5;
        if (target instanceof LivingEntity e) {
            double fighting = Mth.clamp(CombatData.getCap(e).getMaxPosture() / (CombatData.getCap(getOwner()).getMaxPosture() * strength), 1, 20);
            fighting *= fighting;
            snapTime = (int) (fighting * 6);
            strength = 1 / fighting;
            CombatData.getCap(e).bindHands(duration);
        }
        getEntityData().set(DRAG_STRENGTH, (float) strength);
        getEntityData().set(DRAG_OFFSET, target.position().subtract(this.position()).multiply(0, 1, 0).toVector3f());
        getEntityData().set(DRAG_POSE, new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, getInteractionRange())), snapTime));
    }

    @Override
    public void updateTetheringVelocity() {
        if (shouldDrag()) {
            final Vec3 offset = new Vec3(getEntityData().get(DRAG_OFFSET));
            final Vec3 targetPoint = position().add(offset);
            //I can also use getIdlePose().getStartFrame().resolveTargetOffset(getOwner(), dragging, 1);
            //for (Entity theMob:dragging.keySet()) {
            Entity theMob = getTetheringEntity();
            moveTargetTowards(theMob, targetPoint, 10);
//            if (!isIdle() && GeneralUtils.getDistSqCompensated(theMob, this) > 0)
//                unDrag();
            //}
//            if(!isIdle()) {
//                dragging.replaceAll((entity, value) -> value - 1);
//                dragging.entrySet().removeIf(entry -> entry.getValue() < 0);
//            }
        }
    }

    @Override
    public @NotNull Vec3 getTetheredOffset() {
        return new Vec3(0, 0, 2);
        //return getIdlePose().getStartFrame().resolveTargetOffset(getOwner(), new Vec3(getEntityData().get(DRAG_OFFSET)), getInteractionRange());
    }

    public void unDrag() {
        getEntityData().set(DRAG_TIME, -1);
        setTetheringEntity(null);
        setIdlePose(getIdlePose());
    }

    @Override
    public MotionManager getIdlePose() {
        //to avoid actually deleting what it's supposed to be doing
        if (shouldDrag()) return getEntityData().get(DRAG_POSE);
        return super.getIdlePose();
    }

    @Override
    public double getDragStrength() {
        //return getEntityData().get(DRAG_STRENGTH);
        return 10;
    }

    @Override
    public int getDragDuration() {
        return getEntityData().get(DRAG_TIME);
    }
}
