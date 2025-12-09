package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public interface IFlyingWeapon {
    FlyingWeaponEntity getWeapon(InteractionHand hand);

    void scheduleAction(InteractionHand hand, MotionManager mm, WeaponStats.SweepInfo info, double range, int totalTime);

    void tick();

    void setRender(InteractionHand hand, FlyingWeaponEffect... effects);

    void yeet(InteractionHand hand, Vec3 pos);

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

        @Override
        public void setRender(InteractionHand hand, FlyingWeaponEffect... effects) {

        }

        @Override
        public void yeet(InteractionHand hand, Vec3 pos) {

        }
    }
}
