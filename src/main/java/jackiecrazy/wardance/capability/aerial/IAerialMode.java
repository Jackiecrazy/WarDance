package jackiecrazy.wardance.capability.aerial;

public interface IAerialMode {
    void alterGravity(int ticks, double speed);
    void tick();
    void resetSpeed();
    double getEffectiveSpeed();
    int getTimeRemaining();
}
