package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.utils.SweepActions;

public record WeaponMotionManager(MotionManager wrap, SweepActions.SweepInfo info, double range) implements MotionManager {

    @Override
    public MotionFrame getNextPoint(int elapsedTicks) {
        return wrap.getNextPoint(elapsedTicks);
    }

    @Override
    public int getDuration() {
        return wrap.getDuration();
    }
}
