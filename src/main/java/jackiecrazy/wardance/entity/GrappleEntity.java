package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.movement.ResetAirJumpPacket;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class GrappleEntity extends FlyingItemEntity {
    public static final int SQDIST = 5;
    public static final int MAXDIST = 32;
    /*
    Q+mmb will throw a grapple.
    The grapple latches to the first block, mob, or thrown weapon that it contacts.
    Hold Q will maintain the current length. In this case the anchor will be whoever is bigger.
    Release Q to rappel to the point.
        If the point is a mob you kick it, if the point is a weapon you pick it and spin, if the point is a block you jump up.
        You can also cancel the velocity by jumping.
    MMB again to pull the point to you. If the point

    gonna be a bit creative with the fields here.
     */
    protected static final EntityDataAccessor<Float> TETHER_LENGTH = SynchedEntityData.defineId(GrappleEntity.class, EntityDataSerializers.FLOAT);
    private boolean hooked = false;
    private Entity hookedEntity = null;
    private double hookEntityOffset = 0;
    private BlockHitResult hookedHit = null;
    private ACTION retractAction = null;
    private double hookStrength = 2;

    public GrappleEntity(EntityType<? extends FlyingItemEntity> type,
                         Level level) {
        super(type, level);
        renderLag = SQDIST;
    }

    public boolean hooked() {
        return hooked;
    }

    @Override
    public boolean stopMotion() {
        return true;
    }

    @Override
    public void moveTargetTowards(Entity toBeMoved, Vec3 point, double force) {
        super.moveTargetTowards(toBeMoved, point, hookStrength);
    }

    @Override
    public void updateTetheringVelocity() {
        if (retractAction != null) {
            super.updateTetheringVelocity(); //pull phase
        }
    }

    @Override
    public double getTetherLength() {
        return entityData.get(TETHER_LENGTH);
    }

    public void setTetherLength(double dist) {
        entityData.set(TETHER_LENGTH, (float) dist);
    }

    @Override
    public boolean shouldRepel() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        //tag.putBoolean("movePlayer", movePlayer);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        //movePlayer = tag.getBoolean("movePlayer");
    }

    @Override
    public STATE getState() {
        return getMotionTarget() != getOwner() ? STATE.THROW_TRACK : STATE.THROW_NATURAL;
    }

    @Override
    public ItemStack getHeldItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TETHER_LENGTH, 0f);
    }

    /**
     * basically flip them if movePlayer is true
     *
     * @return
     */
    @Override
    public Entity getTetheredEntity() {
        if (!hooked) return null;
        if (retractAction == ACTION.ZIP) return this;
        if (retractAction == ACTION.YANK) return getOwner();
        return hookedEntity == null ? this : hookedEntity;
    }

    @Override
    public Entity getTetheringEntity() {
        if (!hooked) return null;
        if (retractAction == ACTION.ZIP) return getOwner();
        if (retractAction == ACTION.YANK) return hookedEntity;
        return getOwner();
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return super.shouldRender(p_20296_, p_20297_, p_20298_) || getOwner() != null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!intangible()) {//hooked onto something
            renderLag--;
            if (renderLag < 0) renderLag = 0;
        } else {
            //become faster over time
            //addDeltaMovement(getDeltaMovement().normalize().scale(0.01));
        }
        if (level().isClientSide) {
            updateTetheringVelocity();
            handleEntityCollisions();
            handleBlockCollisions();
        }
        //server side velocity stuff
        if (isAlive()) {//!level().isClientSide &&
            //general sanity death checks
            if (getOwner() instanceof Player p) {
                if (p.distanceToSqr(this) > MAXDIST * MAXDIST || p.isShiftKeyDown())
                    remove(RemovalReason.DISCARDED);
                else if ((getTetheringEntity() == p && retractAction != null && p.distanceToSqr(this) < SQDIST)) {
                    remove(RemovalReason.DISCARDED);
                    unhookAndJump(p, false);
                }
            }
            if (tickCount > 60 && !hooked)
                remove(RemovalReason.DISCARDED);

            //server only stuff: pick up, kick, rip block
            if (!hooked || level().isClientSide) return;
            if (hookedEntity != null) {
                //pull
                updateEntityHookPosition();
                //give guard frames for your rush
                if(retractAction==ACTION.ZIP)
                    CombatData.getCap(getOwner()).setGuardTime(3);
                if (hookedEntity instanceof ThrownWeaponEntity fwe && retractAction != null && getOwner() instanceof Player p && fwe.distanceToSqr(p) < SQDIST) {
                    boolean picked = fwe.pickup(p);
                    remove(RemovalReason.DISCARDED);

                    //if you move to the weapon, slow gravity a bit
                    if (retractAction == ACTION.ZIP && picked) {
                        AerialModeData.getCap(p).alterGravity(40, 0.3);
                        AerialModeData.getCap(p).setAerialMode(true);
                        p.setDeltaMovement(new Vec3(0, 0.1, 0));
                    }
                    if (p instanceof ServerPlayer sp)
                        CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new ResetAirJumpPacket());
                }
                if (hookedEntity instanceof LivingEntity target && retractAction != null && getOwner() instanceof Player p && GeneralUtils.getDistSqCompensated(target, getOwner()) < SQDIST) {
                    unhookAndJump(p, true);
                    target.setDeltaMovement(target.getDeltaMovement().normalize().scale(0.4));
                    //if you move to the mob, dropkick them
                    if (retractAction == ACTION.ZIP)
                        CombatUtils.kick(p, target, CombatData.getCap(p).consumeSpirit(CombatData.getCap(p).getMaxSpirit()));
                }
                if (hookedEntity.isRemoved()) {
                    hookedEntity = null;
                    hooked = false;
                    remove(RemovalReason.DISCARDED);
                }
            } else if (hookedHit != null && retractAction == ACTION.YANK) {
                //hooked a block
                ripBlock();
            }
            getOwner().fallDistance = 1;
        }
    }

    private void unhookAndJump(LivingEntity p, boolean resetVelocity) {
        remove(RemovalReason.DISCARDED);
        if (resetVelocity)
            p.setDeltaMovement(new Vec3(0, 0.5, 0));
        else p.setDeltaMovement(p.getDeltaMovement().multiply(1, 0, 1).add(0, 0.5, 0));
        p.resetFallDistance();
        AerialModeData.getCap(p).alterGravity(20, 0.3);
        AerialModeData.getCap(p).setAerialMode(true);
        if (p instanceof ServerPlayer sp)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new ResetAirJumpPacket());
    }

    public void ripBlock() {
        if (!hooked) {
            remove(RemovalReason.DISCARDED);
            return;
        }
        //what exactly did we hook?
        //if it's a blockstate, pull a copy of the block back instead
        //if it's an entity, just make sure the tether knows about it
        if (hookedHit != null && !level().isClientSide() && getOwner() instanceof Player p) {
            BlockState hookedBlockState = level().getBlockState(hookedHit.getBlockPos());
            if (!hookedBlockState.isAir() && level().getBlockEntity(hookedHit.getBlockPos()) == null) {
                //create the new hooked entity
                final ItemStack picked = hookedBlockState.getCloneItemStack(hookedHit, level(), hookedHit.getBlockPos(), p);
                if (!picked.isEmpty()) {
                    GhostBlockEntity fwe = new GhostBlockEntity(WarEntities.FLYING_BLOCK.get(), level());
                    fwe.setHeldItem(picked);
                    //level().destroyBlock(hookedHit.getBlockPos(), false, p);
                    Vec3 pos = hookedHit.getBlockPos().getCenter();
                    fwe.setOwner(p);
                    fwe.setPosRaw(pos.x, pos.y, pos.z);
                    fwe.setInteractionRange(1);
                    fwe.setState(STATE.THROW_NATURAL);
                    fwe.setIntangible(true);
                    level().addFreshEntity(fwe);
                    hookedHit = null;
                    hookedEntity = fwe;
                }
            }
        }
        //in any case, tether length is now 0
        setTetherLength(0);
    }

    public boolean swinging() {
        return !isRemoved() && hooked && retractAction == null;
    }


    public void retract(ACTION act) {
        retractAction = act;
        switch (act) {
            case RELEASE -> this.remove(RemovalReason.DISCARDED);//ezpz no more vector changes
            case JUMP -> unhookAndJump(getOwner(), false);
            case ZIP -> setTetherLength(0);
            case YANK -> {
                setTetherLength(0);
                if (hookedEntity instanceof LivingEntity h && getOwner() != null) {
                    //battle of strength!
                    final float strMod = Math.min(1, CombatData.getCap(getOwner()).getMaxPosture() / CombatData.getCap(h).getMaxPosture());
                    hookStrength = strMod * strMod;
                }
            }
        }
    }

    protected void handleEntityCollisions() {
        final float range = getInteractionRange();
        List<Entity> selfTarget = level().getEntities(getOwner(), getBoundingBox().inflate(1f), e -> e != getOwner() && e.isAlive() && e.isAttackable());
        List<Entity> viewTarget = level().getEntities(getOwner(), getBoundingBox().expandTowards(getLookAngle().scale(2)).inflate(1f), e -> e != getOwner() && e.isAlive() && e.isAttackable());

        selfTarget.addAll(viewTarget);
        onHitEntity(selfTarget);
    }

    private void updateEntityHookPosition() {
        if (hookedEntity == null) return;
        setPos(hookedEntity.position().add(0, hookEntityOffset, 0));
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        //if (distanceToSqr(getOwner()) < 0.225) return false;
        if (hooked) return false;
        if (getMotionTarget() != getOwner()) {
            if (targets.contains(getMotionTarget())) {
                hooked = true;
                hookedEntity = getMotionTarget();
                setMotionTarget(null);
            }
        }
        //this is freeform hook entity code
        else {
            targets.stream().forEach(a -> {
                if (a instanceof ThrownWeaponEntity fwe && fwe.isReal()) {
                    hookedEntity = fwe;
                    hooked = true;
                }
            });
            if (!hooked) {
                targets.stream().filter(a -> !(a instanceof FlyingItemEntity)).sorted((a, b) -> (int) (a.distanceToSqr(this) - b.distanceToSqr(this))).findFirst().ifPresent(a -> {
                    hookedEntity = a;
                    hooked = true;
                });
            }
        }

        if (hooked) {
            setIntangible(false);
            setDeltaMovement(Vec3.ZERO);
            hookEntityOffset = Mth.clamp(getY() - hookedEntity.getY(), 0, hookedEntity.getBbHeight());
            updateEntityHookPosition();
            setTetherLength(Math.sqrt(distanceToSqr(getOwner().getEyePosition())));
        }
        if (hookedEntity instanceof ThrownWeaponEntity fwe) {

            fwe.setState(STATE.THROW_NATURAL);
            hookEntityOffset = 0;
        }
        return hooked;
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {

    }

    @Override
    protected void handleBlockCollisions() {
        if (hooked) return;
        if (getMotionTarget() != getOwner()) return;
        //if (distanceToSqr(getOwner()) < 0.225) return;

        BlockHitResult hit = level().clip(new ClipContext(position(), position().add(getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            hooked = true;
            //movePlayer = !movePlayer;
            hookedHit = hit;
            setDeltaMovement(Vec3.ZERO);
            hurtMarked = true;
            setPos(hit.getLocation());
            setTetherLength(Math.sqrt(distanceToSqr(getOwner().getEyePosition())));
            setIntangible(false);
        }
    }

    @Override
    protected void returnToIdle(int duration) {

    }

    public enum ACTION {
        RELEASE,
        JUMP,
        YANK,
        ZIP
    }
}
