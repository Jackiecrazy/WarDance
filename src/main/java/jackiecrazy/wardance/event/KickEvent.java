package jackiecrazy.wardance.event;

import jackiecrazy.footwork.api.CombatDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class KickEvent extends LivingEvent {
    private Entity target;
    private CombatDamageSource ds;
    private float damage;
    private float postureDamage;

    public KickEvent(LivingEntity entity, CombatDamageSource ds, Entity target, float amnt, float pos) {
        super(entity);
        this.target = target;
        this.ds = ds;
        damage = amnt;
        postureDamage=pos;
    }

    public float getPostureDamage() {
        return postureDamage;
    }

    public KickEvent setPostureDamage(float amount) {
        this.postureDamage = amount;
        return this;
    }

    public float getDamage() {
        return damage;
    }

    public KickEvent setDamage(float amount) {
        this.damage = amount;
        return this;
    }

    public CombatDamageSource getDamageSource() {
        return ds;
    }

    public Entity getTarget() {
        return target;
    }
}
