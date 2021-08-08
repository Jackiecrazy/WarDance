package jackiecrazy.wardance.event;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class DodgeEvent extends LivingEvent {
    private double force;
    private Direction side;

    public DodgeEvent(LivingEntity subject, Direction d, double amount) {
        super(subject);
        force = amount;
        side = d;
    }

    public double getForce() {
        return force;
    }

    public void setForce(float force) {
        this.force = force;
    }

    public Direction getDirection() {return side;}

    public void setDirection(Direction d) {side = d;}

    public enum Direction {
        FORWARD,//slide
        BACK,
        LEFT,
        RIGHT
    }
}
