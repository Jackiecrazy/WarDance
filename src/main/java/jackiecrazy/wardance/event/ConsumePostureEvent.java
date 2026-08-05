package jackiecrazy.wardance.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class ConsumePostureEvent extends LivingEvent {
    public static enum TYPE{
        NONE,
        BLOCK,
        PARRY
    }
    protected final float originalPostureConsumption;
    protected float postureConsumption;
    protected boolean canBreach;

    public ConsumePostureEvent(LivingEntity entity, float orig, float posture, boolean breach) {
        super(entity);
        originalPostureConsumption = orig;
        postureConsumption = posture;
        canBreach = breach;
    }

    public float getOriginalPostureConsumption() {
        return originalPostureConsumption;
    }

    public float getPostureConsumption() {
        return postureConsumption;
    }

    public void setPostureConsumption(float amount) {
        postureConsumption = amount;
    }

    public boolean canBreach() {
        return canBreach;
    }

    public abstract TYPE getType();

    public boolean success(){
        return false;
    }
}
