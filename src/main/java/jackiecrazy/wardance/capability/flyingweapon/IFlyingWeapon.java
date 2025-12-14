package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public interface IFlyingWeapon {
    FlyingWeaponEntity getWeapon(InteractionHand hand);

    ThrownWeaponEntity getHeldBlock();

    void setHeldBlock(ThrownWeaponEntity sb);

    GrappleEntity getGrapple();

    void launchGrapple(Vec3 to);

    void scheduleAction(InteractionHand hand,
                        MotionManager mm,
                        WeaponStats.SweepInfo info,
                        double range,
                        int totalTime);

    void tick();

    void setRender(InteractionHand hand, FlyingWeaponEffect... effects);

    void yeet(InteractionHand hand, Vec3 pos);

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
                                   MotionManager mm,
                                   WeaponStats.SweepInfo info,
                                   double range,
                                   int totalTime) {

        }

        @Override
        public void tick() {

        }

        @Override
        public void setRender(InteractionHand hand, FlyingWeaponEffect... effects) {

        }

        @Override
        public void yeet(InteractionHand hand, Vec3 pos) {

        }
    }
}
