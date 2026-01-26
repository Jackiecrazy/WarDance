package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    private boolean hooked = false;
    private Entity hookedEntity = null;
    private double hookEntityOffset = 0;
    private int heldTime = 10;
    private BlockHitResult hookedHit = null;
    private boolean movePlayer = true;
    private boolean grabBlock = false;

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
        super.moveTargetTowards(toBeMoved, point, force*5.5);
    }

    @Override
    public void updateTetheringVelocity() {
        super.updateTetheringVelocity(); //pull phase
    }

    @Override
    public boolean shouldRepel() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tag.putBoolean("movePlayer", movePlayer);
    }


    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        movePlayer = tag.getBoolean("movePlayer");
    }

    @Override
    public STATE getState() {
        return getMotionTarget()==null? STATE.THROW_NATURAL:STATE.THROW_TRACK;
    }

    @Override
    public ItemStack getHeldItem() {
        return ItemStack.EMPTY;
    }

    /**
     * basically flip them if movePlayer is true
     *
     * @return
     */
    @Override
    public Entity getTetheredEntity() {
        if (!hooked) return null;
        if (hookedHit != null && !movePlayer) return null;
        if (!movePlayer) return getOwner();
        return hookedEntity == null ? this : hookedEntity;
    }

    @Override
    public Entity getTetheringEntity() {
        if (!hooked) return null;
        if (hookedHit != null && !movePlayer) return null;
        if (movePlayer || hookedEntity == null) return getOwner();
        return hookedEntity;
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return super.shouldRender(p_20296_, p_20297_, p_20298_) || getOwner() != null;
    }

    @Override
    public void remove(RemovalReason p_146834_) {
        super.remove(p_146834_);
    }

    @Override
    public void tick() {
        super.tick();
        if (!transitioning()) {//hooked onto something
            renderLag--;
            if (renderLag < 0) renderLag = 0;
        }else{
            //become faster over time
            addDeltaMovement(getDeltaMovement().normalize().scale(0.01));
        }
        //server side velocity stuff
        if (!level().isClientSide && isAlive()) {
            //general sanity death checks
            if (getOwner() instanceof Player p) {
                if (p.distanceToSqr(this) > MAXDIST * MAXDIST)
                    remove(RemovalReason.DISCARDED);
                if ((getTetheringEntity() == p && p.distanceToSqr(this) < SQDIST) || p.isShiftKeyDown()) {
                    remove(RemovalReason.DISCARDED);
                    p.setDeltaMovement(new Vec3(0, 0.5, 0));
                    p.resetFallDistance();
                    TimeSlowData.getCap(p).alterSpeed(40, 0.3);
                }
            }
            if (tickCount > 100 && !hooked)
                remove(RemovalReason.DISCARDED);

            if (!hooked) return;
            if (hookedEntity != null) {
                //pull
                updateEntityHookPosition();
                if (hookedEntity instanceof FlyingWeaponEntity fwe && getOwner() instanceof Player p && fwe.distanceToSqr(p) < SQDIST) {
                    boolean picked=fwe.pickup(p);
                    remove(RemovalReason.DISCARDED);

                    //if you move to the weapon, spin attack
                    if(movePlayer&&picked) {

                    }
                }
                if (hookedEntity instanceof LivingEntity target && getOwner() instanceof Player p && GeneralUtils.getDistSqCompensated(target, getOwner()) < SQDIST) {
                    remove(RemovalReason.DISCARDED);
                    p.resetFallDistance();
                    TimeSlowData.getCap(p).alterSpeed(40, 0.3);

                    //if you move to the mob, dropkick them
                    if (movePlayer)
                        CombatUtils.kick(p, target, true);
                }
                if (hookedEntity.isRemoved()) {
                    hookedEntity = null;
                    hooked = false;
                }
            } else if(!movePlayer) {
                //hooked a block
                heldTime--;
                if (heldTime < 0)
                    ripBlock();
            }
            if (movePlayer) getOwner().fallDistance = 1;
        }
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
                    fwe.setTransitioning(true);
                    level().addFreshEntity(fwe);
                    hookedHit = null;
                    hookedEntity = fwe;
                }
            }
        }
        //in any case, tether length is now 0
        movePlayer = false;
    }

    public void yank() {
        movePlayer = !movePlayer;
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
        if (distanceToSqr(getOwner()) < SQDIST) return false;
        if (hooked) return false;
        if(getMotionTarget()!=getOwner()){
            if(targets.contains(getMotionTarget())) {
                hooked = true;
                hookedEntity = getMotionTarget();
                setMotionTarget(null);
            }
        }else {
            targets.stream().forEach(a -> {
                if (a instanceof FlyingWeaponEntity fwe && fwe.isReal()) {
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
            setTransitioning(false);
            setDeltaMovement(Vec3.ZERO);
            hookEntityOffset = getY() - hookedEntity.getY();
            updateEntityHookPosition();
        }
        return hooked;
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {

    }

    @Override
    protected void handleBlockCollisions() {
        if (hooked) return;
        if(getMotionTarget()!=getOwner())return;
        if (distanceToSqr(getOwner()) < SQDIST) return;

        BlockHitResult hit = level().clip(new ClipContext(position(), position().add(getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            hooked = true;
            movePlayer = !movePlayer;
            grabBlock = true;
            hookedHit = hit;
            setDeltaMovement(Vec3.ZERO);
            setPos(hit.getLocation());
            setTransitioning(false);
        }
    }
}
