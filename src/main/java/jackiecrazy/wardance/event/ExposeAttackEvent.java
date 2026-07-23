package jackiecrazy.wardance.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ExposeAttackEvent extends LivingEvent {
    private LivingEntity attacker;
    private DamageSource ds;
    private float amount = 0;

    public ExposeAttackEvent(Entity attacker, DamageSource ds, LivingEntity entity, float amnt) {
        super(entity);
        if (attacker instanceof LivingEntity le)
            this.attacker = le;
        this.ds = ds;
        amount = amnt;
    }

    public float getAmount() {
        return amount;
    }

    public ExposeAttackEvent setAmount(float amount) {
        this.amount = amount;
        return this;
    }

    public DamageSource getDamageSource() {
        return ds;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }
}
