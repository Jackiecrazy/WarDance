package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThrownWeaponEntity extends FlyingWeaponEntity {
    private boolean dormant = false;
    private boolean falling = false;

    public ThrownWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        super(type, level);
        setInteractionRange(1);
        setShouldRender(FlyingWeaponEffect.WEAPON);
        setTransitioning(false);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (falling)
            addDeltaMovement(new Vec3(0, -0.02, 0));
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (transitioning()) return false;
        boolean ret = super.onHitEntity(targets);
        if (getHeldItem().getItem() instanceof BlockItem)
            for (Entity a : alreadyHit) {
                a.setDeltaMovement(getDeltaMovement());
            }
        if (ret) {
            //lose all velocity and start dropping to the ground
            setDeltaMovement(Vec3.ZERO);
            falling = true;
        }
        return ret;
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if(transitioning())return;
        super.onHitBlock(blockPos, hitFace, location);
        setTransitioning(true);
        falling = false;
    }
}
