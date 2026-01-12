package jackiecrazy.wardance.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class DamageRetconEvent extends LivingEvent {
    private float amount;
    private LivingEntity target;

    public DamageRetconEvent(LivingEntity to, float amount) {
        super(to);
        this.amount = amount;
        this.target = target;
    }

    public float getAmount() {
        return amount;
    }

    public DamageRetconEvent setAmount(float amount) {
        this.amount = amount;
        return this;
    }

    public DamageRetconEvent addAmount(float amount) {
        this.amount += amount;
        return this;
    }

    public LivingEntity getTarget() {
        return target;
    }
}
