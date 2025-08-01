package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import net.minecraft.world.InteractionHand;

public interface IFlyingWeapon {
    FlyingWeaponEntity getWeapon(InteractionHand hand);

    void scheduleAction(InteractionHand hand, MotionManager mm, WeaponStats.SweepInfo info, double range, int totalTime);

    void tick();

    class DummyFlyingWeapon implements IFlyingWeapon{

        @Override
        public FlyingWeaponEntity getWeapon(InteractionHand hand) {
            return null;
        }

        @Override
        public void scheduleAction(InteractionHand hand, MotionManager mm, WeaponStats.SweepInfo info, double range, int totalTime) {

        }

        @Override
        public void tick() {

        }
    }
}
