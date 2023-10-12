package jackiecrazy.wardance.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class FractureEvent extends LivingEvent {
    private int amount;
    private LivingEntity attacker;

    public FractureEvent(LivingEntity to, int amount, LivingEntity attacker) {
        super(to);
        this.amount = amount;
        this.attacker = attacker;
    }

    public int getAmount() {
        return amount;
    }

    public FractureEvent setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public FractureEvent addAmount(int amount) {
        this.amount += amount;
        return this;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }
}
