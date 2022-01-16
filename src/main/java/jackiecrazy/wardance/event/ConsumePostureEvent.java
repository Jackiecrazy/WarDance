package jackiecrazy.wardance.event;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
@Cancelable
/**
 * default for... default, allow to bypass config hard cap
 */
public class ConsumePostureEvent extends LivingEvent {
    private final float original;
    private final LivingEntity attacker;
    private float amount;
    private final float above;

    public ConsumePostureEvent(LivingEntity entity, LivingEntity attacker, float amnt, float above) {
        super(entity);
        amount = original = amnt;
        this.above = above;
        this.attacker = attacker;
    }

    public float getAbove(){
        return above;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getOriginal() {
        return original;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }
}