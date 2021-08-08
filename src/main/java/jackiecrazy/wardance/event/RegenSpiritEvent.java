package jackiecrazy.wardance.event;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class RegenSpiritEvent extends LivingEvent {
    private float quantity;
    public RegenSpiritEvent(LivingEntity subject, float amount) {
        super(subject);
        quantity=amount;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
