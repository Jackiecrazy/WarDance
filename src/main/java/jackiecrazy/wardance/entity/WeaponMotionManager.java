package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.config.weapon.WeaponInteractions;

public record WeaponMotionManager(MotionManager wrap, WeaponInteractions.HitInfo info, double range) implements MotionManager {

    @Override
    public MotionFrame getNextPoint(int elapsedTicks) {
        return wrap.getNextPoint(elapsedTicks);
    }

    @Override
    public int getDuration() {
        return wrap.getDuration();
    }
}
