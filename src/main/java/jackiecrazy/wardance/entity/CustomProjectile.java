package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class CustomProjectile extends ThrownWeaponEntity {
    public CustomProjectile(EntityType<? extends FlyingItemEntity> type, Level level) {
        super(type, level);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        this.setRemoved(reason);
        this.invalidateCaps();
    }

    @Override
    public boolean pickup(Player player) {
        if (getInfo() != null) return false;
        this.setUniversalOffset(new Vec3(0, player.getBbHeight(), 0.5));
        setDeltaMovement(Vec3.ZERO);
        setIntangible(true);
        setState(STATE.FOLLOW);
        FlyingWeaponData.getCap(player).setHeldBlock(this);
        MotionManager mm = new MotionManagers.DefinitionMM(
                new MotionGroup(
                        List.of(
                                new MotionFrame(new Vec3(0, 1, 0.4), new Vec3(0, 1, 0), getIdlePose().getStartFrame().renderOrientation()),
                                new MotionFrame(new Vec3(0, 1, -0.4), new Vec3(0, 1, 0), getIdlePose().getStartFrame().renderOrientation())),
                        EasingFunctionEnum.IN_SINE, 20));
        queuePath(mm);
        return true;
    }

    @Override
    public void yeet(Vec3 to, double strength) {
        super.yeet(to, strength);
        setHitInfo(HitInfo.THROWN);
    }

    protected void autoYeet() {

        Vec3 dest = getOwner().getEyePosition().add(getOwner().getLookAngle().scale(32));
        FlyingWeaponData.getCap(getOwner()).yeet(null, dest, 0.5);
    }

    @Override
    public boolean canPickup() {
        return !intangible();
    }
}
