package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GrappleEntity extends FlyingItemEntity {
    public static final int SQDIST = 5;
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
    private BlockHitResult hookedHit = null;
    private double hookLength = 10;//set to 0 when you release Q
    private boolean movePlayer = false;
    private double remainingRopeStrength = 100;

    public GrappleEntity(EntityType<? extends FlyingItemEntity> type,
                         Level level) {
        super(type, level);
        setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
        setHeldItem(new ItemStack(Items.IRON_PICKAXE));
        renderLag = SQDIST;
    }

    public void ropeSwingMode(Entity swinger, Entity anchor, double ropeLength) {
        swinger.fallDistance = 0;
        Vec3 pos = swinger.position();
        Vec3 vel = swinger.getDeltaMovement();
        double spd=vel.length();

        // Direction from hook to player
        Vec3 toPlayer = pos.subtract(anchor.position());
        double dist = toPlayer.length();

        if (dist < 0.0001) return;

        Vec3 ropeDir = toPlayer.normalize();

        // --------------------------------------------------
        // 1. Remove radial velocity (prevents collapsing inward)
        // --------------------------------------------------
        double radialSpeed = vel.dot(ropeDir);
        Vec3 radialVel = ropeDir.scale(radialSpeed);
        Vec3 tangentialVel = vel.subtract(radialVel);

        // --------------------------------------------------
        // 2. Apply gravity only to tangential motion
        // --------------------------------------------------
        tangentialVel = tangentialVel.normalize().scale(vel.length()).add(0, -0.08, 0);

        // --------------------------------------------------
        // 3. Enforce rope length constraint
        // --------------------------------------------------
        Vec3 correctedPos = anchor.position().add(ropeDir.scale(ropeLength));
        //swinger.setPos(correctedPos.x, correctedPos.y, correctedPos.z);

        // --------------------------------------------------
        // 4. Small energy loss (optional, feels good)
        // --------------------------------------------------
        //tangentialVel = tangentialVel.scale(0.995);

        swinger.setDeltaMovement(tangentialVel.normalize().scale(spd*1.0001));
        swinger.hasImpulse = true;
        swinger.hurtMarked = true;
    }

    public boolean hooked(){return hooked;}

    @Override
    public void updateTetheringVelocity() {
        Vec3 offset = getTetheredOffset();
        Entity toBeMoved = getOwner();
        Entity moveTowards = this;
        if (getTetherLength() > 0 && toBeMoved != null && hooked) {
            //chain phase
            if (toBeMoved instanceof LivingEntity) {
                //ropeSwingMode(toBeMoved, moveTowards, getTetherLength());
            }
        } else super.updateTetheringVelocity(); //pull phase
    }

    @Override
    public boolean shouldRepel() {
        return false;
    }

    @Override
    public boolean stopMotion() {
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
        return STATE.THROW_NATURAL;
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
        if (getTetherLength() > 0) return null;
        if (!movePlayer) return getOwner();
        return hookedEntity == null ? this : hookedEntity;
    }

    @Override
    public Entity getTetheringEntity() {
        if (!hooked) return null;
        if (getTetherLength() > 0) return null;
        if (movePlayer || hookedEntity == null) return getOwner();
        return hookedEntity;
    }

    @Override
    public double getTetherLength() {
        return hookLength;
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return super.shouldRender(p_20296_, p_20297_, p_20298_)||getOwner()!=null;
    }

    @Override
    public void remove(RemovalReason p_146834_) {
        super.remove(p_146834_);
    }

    @Override
    public void tick() {
        super.tick();
        if (!transitioning()) {
            renderLag--;
            if (renderLag < 0) renderLag = 0;
        }
        if (!level().isClientSide && isAlive()) {
            //general sanity death checks
            if (getOwner() instanceof Player p) {
                if (p.distanceToSqr(this) > 64 * 64)
                    remove(RemovalReason.DISCARDED);
                if (getTetheringEntity() == p && (p.distanceToSqr(this) < SQDIST || p.isShiftKeyDown())) {
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
                updateEntityHookPosition();
                if (hookedEntity instanceof FlyingWeaponEntity fwe && getOwner() instanceof Player p && fwe.distanceToSqr(p) < SQDIST) {
                    //pick up and spin
                    fwe.pickup(p);
                    remove(RemovalReason.DISCARDED);
                }
                if (hookedEntity instanceof LivingEntity target && getOwner() instanceof Player caster && this.distanceToSqr(getOwner()) < SQDIST) {
                    //kick
                    remove(RemovalReason.DISCARDED);
                    CombatUtils.kick(caster, target);
                }
                if (hookedEntity.isRemoved()) {
                    hookedEntity = null;
                    hooked = false;
                }
            }
            if (movePlayer) getOwner().fallDistance = 1;
        }
    }

    public void yank() {
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
                    ShadowBlockEntity fwe = new ShadowBlockEntity(WarEntities.FLYING_BLOCK.get(), level());
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
        hookLength = 0;
        movePlayer = false;
    }

    public void rappel() {
        hookLength = 0;
        movePlayer = true;
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
        if (hooked) {
            if (hookLength != 0) {
                hookLength = hookedEntity.distanceTo(getOwner());
                movePlayer = false;
            }
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
        if (distanceToSqr(getOwner()) < SQDIST) return;

        BlockHitResult hit = level().clip(new ClipContext(position(), position().add(getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            hooked = true;
            hookedHit = hit;
            setDeltaMovement(Vec3.ZERO);
            setPos(hit.getLocation());
            setTransitioning(false);
            if (hookLength != 0) {
                hookLength = distanceTo(getOwner());
            }
        }
    }
}
