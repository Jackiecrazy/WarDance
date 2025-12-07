package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.config.WeaponStats;
import net.minecraft.world.InteractionHand;

public interface IFlyingWeapon {
    FlyingItemEntity getWeapon(InteractionHand hand);

    void scheduleAction(InteractionHand hand, MotionManager mm, WeaponStats.SweepInfo info, double range, int totalTime);

    void tick();

    void setRender(InteractionHand hand, FlyingWeaponEffect... effects);

    class DummyFlyingWeapon implements IFlyingWeapon{

        @Override
        public FlyingItemEntity getWeapon(InteractionHand hand) {
            return null;
        }

        @Override
        public void scheduleAction(InteractionHand hand, MotionManager mm, WeaponStats.SweepInfo info, double range, int totalTime) {

        }

        @Override
        public void tick() {

        }

        @Override
        public void setRender(InteractionHand hand, FlyingWeaponEffect... effects) {

        }
    }
}
