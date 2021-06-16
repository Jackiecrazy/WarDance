package jackiecrazy.wardance.event;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class EntityAwarenessEvent extends LivingEvent {
    private final CombatUtils.AWARENESS originalStatus;
    private LivingEntity attacker;
    private CombatUtils.AWARENESS status;

    public EntityAwarenessEvent(LivingEntity entity, LivingEntity attacker, CombatUtils.AWARENESS originally) {
        super(entity);
        this.attacker = attacker;
        originalStatus = status = originally;
    }

    public CombatUtils.AWARENESS getAwareness() {
        return status;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public void setAwareness(CombatUtils.AWARENESS newAwareness) {
        status = newAwareness;
    }

    public CombatUtils.AWARENESS getOriginalAwareness() {
        return originalStatus;
    }
}
