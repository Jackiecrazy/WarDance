package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEntity;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import net.minecraft.world.InteractionHand;

public interface IFlyingWeapon {
    FlyingWeaponEntity getWeapon(InteractionHand hand);

    void scheduleAction(InteractionHand hand, MotionManager mm, double range, int totalTime);

    void tick();

    class DummyFlyingWeapon implements IFlyingWeapon{

        @Override
        public FlyingWeaponEntity getWeapon(InteractionHand hand) {
            return null;
        }

        @Override
        public void scheduleAction(InteractionHand hand, MotionManager mm, double range, int totalTime) {

        }

        @Override
        public void tick() {

        }
    }
}
