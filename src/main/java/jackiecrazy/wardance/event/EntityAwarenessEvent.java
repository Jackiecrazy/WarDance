package jackiecrazy.wardance.event;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class EntityAwarenessEvent extends LivingEvent {
    private final CombatUtils.Awareness originalStatus;
    private LivingEntity attacker;
    private CombatUtils.Awareness status;

    public EntityAwarenessEvent(LivingEntity entity, LivingEntity attacker, CombatUtils.Awareness originally) {
        super(entity);
        this.attacker = attacker;
        originalStatus = status = originally;
    }

    public CombatUtils.Awareness getAwareness() {
        return status;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public void setAwareness(CombatUtils.Awareness newAwareness) {
        status = newAwareness;
    }

    public CombatUtils.Awareness getOriginalAwareness() {
        return originalStatus;
    }
}
