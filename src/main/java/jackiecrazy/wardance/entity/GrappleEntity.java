package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
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

import java.util.List;

public class GrappleEntity extends FlyingItemEntity {
    private boolean hooked = false;
    private Entity hookedEntity = null;
    private BlockPos hookedBlockPos = null;
    private BlockState hookedBlockState = null;
    private int hookTick = 20;
    private boolean movePlayer = false;

    public GrappleEntity(EntityType<? extends FlyingItemEntity> type,
                         Level level) {
        super(type, level);
    }

    @Override
    public STATE getState() {
        return STATE.THROW_NATURAL;
    }

    @Override
    public ItemStack getHeldItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Entity getTetheredEntity() {
        return hookedEntity == null ? this : hookedEntity;
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
            //if still alive, start pulling
            //grapple forward mode
            if (movePlayer) {
            }
            //pull back code
            else {
                hookTick--;
                if (hookTick < 0) {
                    //what exactly did we hook?
                    //if it's a blockstate, pull a copy of the block back instead
                    //if it's an entity, just make sure the tether knows about it
                }
            }
        }
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
            targets.stream().sorted((a, b) -> {
                return (int) (a.distanceToSqr(this) - b.distanceToSqr(this));
            }).findFirst().ifPresent(a -> {
                hookedEntity = a;
                hooked = true;
            });
        }
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        hooked = true;
        hookedBlockPos = blockPos;
        hookedBlockState = level().getBlockState(blockPos);
    }

    @Override
    protected void handleBlockCollisions() {
        if (hooked) return;

        BlockHitResult hit = level().clip(new ClipContext(position(), position().add(getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hit.getBlockPos();
            Direction hitFace = hit.getDirection();
            onHitBlock(blockPos, hitFace, hit.getLocation());
        }
    }
}
