package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.utils.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.List;

public class GrappleEntity extends FlyingItemEntity {
    private boolean hooked = false;
    private Entity hookedEntity = null;
    private double hookEntityOffset = 0;
    private BlockHitResult hookedHit = null;
    private int hookTick = 10;
    private boolean movePlayer = false;

    public GrappleEntity(EntityType<? extends FlyingItemEntity> type,
                         Level level) {
        super(type, level);
        setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
        noPhysics = true;
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
        if (!movePlayer) return getOwner();
        return hookedEntity == null || hookTick > 0 ? this : hookedEntity;
    }

    @Override
    public Entity getTetheringEntity() {
        if (!hooked) return null;
        if (movePlayer) return getOwner();
        return hookTick > 0 ? null : hookedEntity;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isAlive()) {
            //general sanity death checks
            if (getOwner() instanceof Player p) {
                if (p.distanceToSqr(this) > 64 * 64)
                    remove(RemovalReason.DISCARDED);
                if (getTetheringEntity() == p && (p.distanceToSqr(this) < 5 || p.isShiftKeyDown())) {
                    remove(RemovalReason.DISCARDED);
                    p.setDeltaMovement(new Vec3(0, 1, 0));
                    p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false, false));
                }
            }
            if (tickCount > 100 && !hooked)
                remove(RemovalReason.DISCARDED);

            if (!hooked) return;
            if (hookedEntity != null) {
                updateEntityHookPosition();
                if(getTetheringEntity() instanceof FlyingWeaponEntity fwe && getTetheredEntity() instanceof Player p && fwe.distanceToSqr(p)<5){
                    fwe.pickup(p);
                    remove(RemovalReason.DISCARDED);
                }
                if(hookedEntity.isRemoved()) {
                    hookedEntity = null;
                    hooked=false;
                }
            }
            //if still alive, start pulling
            if(movePlayer)getOwner().fallDistance=0;
            //pull back code
            if (!movePlayer && getOwner() instanceof Player p) {
                hookTick--;
                if (hookTick < 0) {
                    //what exactly did we hook?
                    //if it's a blockstate, pull a copy of the block back instead
                    //if it's an entity, just make sure the tether knows about it
                    if (hookedHit != null && !level().isClientSide()) {
                        BlockState hookedBlockState = level().getBlockState(hookedHit.getBlockPos());
                        if (!hookedBlockState.isAir()) {
                            //create the new hooked entity
                            FlyingWeaponEntity fwe = new FlyingWeaponEntity(WarEntities.WEAPON.get(), level());
                            fwe.setHeldItem(hookedBlockState.getCloneItemStack(hookedHit, level(), hookedHit.getBlockPos(), p));
                            Vec3 pos = hookedHit.getBlockPos().getCenter();
                            fwe.setOwner(p);
                            fwe.setPosRaw(pos.x, pos.y, pos.z);
                            fwe.setInteractionRange(1);
                            level().addFreshEntity(fwe);
                            hookedHit = null;
                            hookedEntity = fwe;
                        }
                    }
                    if (hookedEntity != null) {
                        //set it as the
                    }
                }
            }
        }
    }

    private void updateEntityHookPosition() {
        if (hookedEntity == null) return;
        setPos(hookedEntity.position().add(0, hookEntityOffset, 0));
    }

    @Override
    protected void onHitEntity(List<Entity> targets) {
        if (hooked) return;
        targets.stream().forEach(a -> {
            if (a instanceof FlyingWeaponEntity fwe && fwe.isReal()) {
                hookedEntity = fwe;
                hooked = true;
            }
        });
        if (!hooked) {
            targets.stream().filter(a->!(a instanceof FlyingItemEntity)).sorted((a, b) -> (int) (a.distanceToSqr(this) - b.distanceToSqr(this))).findFirst().ifPresent(a -> {
                hookedEntity = a;
                hooked = true;
            });
        }
        if (hooked) {
            movePlayer=false;
            setDeltaMovement(Vec3.ZERO);
            hookEntityOffset = hookedEntity.getY() - getY();
            updateEntityHookPosition();
        }
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {

    }

    @Override
    protected void handleBlockCollisions() {
        if (hooked) return;

        BlockHitResult hit = level().clip(new ClipContext(position(), position().add(getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            hooked = true;
            hookedHit = hit;
            setDeltaMovement(Vec3.ZERO);
            setPos(hit.getLocation());
        }
    }
}
