package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IFlyingWeapon {
    FlyingWeaponEntity getWeapon(InteractionHand hand);

    ThrownWeaponEntity getHeldBlock();

    void setHeldBlock(ThrownWeaponEntity sb);

    default boolean hasGrapple(){
        return getGrapple()!=null&&!getGrapple().isRemoved();
    }

    GrappleEntity getGrapple();

    void launchGrapple(Vec3 to);

    void scheduleAction(InteractionHand hand,
                        MotionManager mm);

    void tick();

    void setRender(InteractionHand hand, FlyingWeaponEffect... effects);

    void forceRefreshWeapon(InteractionHand hand);
    default void forceRefreshWeapons(){
        forceRefreshWeapon(InteractionHand.MAIN_HAND);
        forceRefreshWeapon(InteractionHand.OFF_HAND);
    }

    public CompoundTag write() ;
    public void read(Level l, CompoundTag t) ;

    ThrownWeaponEntity yeet(InteractionHand hand, Vec3 pos, double strength);

    class DummyFlyingWeapon implements IFlyingWeapon {

        @Override
        public FlyingWeaponEntity getWeapon(InteractionHand hand) {
            return null;
        }

        @Override
        public ThrownWeaponEntity getHeldBlock() {
            return null;
        }

        @Override
        public void setHeldBlock(ThrownWeaponEntity sb) {

        }

        @Override
        public GrappleEntity getGrapple() {
            return null;
        }

        @Override
        public void launchGrapple(Vec3 to) {

        }

        @Override
        public void scheduleAction(InteractionHand hand,
                                   MotionManager mm) {

        }

        @Override
        public void tick() {

        }

        @Override
        public void setRender(InteractionHand hand, FlyingWeaponEffect... effects) {

        }

        @Override
        public void forceRefreshWeapon(InteractionHand hand) {

        }

        @Override
        public CompoundTag write() {
            return new CompoundTag();
        }

        @Override
        public void read(Level l, CompoundTag t) {

        }

        @Override
        public ThrownWeaponEntity yeet(InteractionHand hand, Vec3 pos, double strength) {
            return null;
        }
    }
}
