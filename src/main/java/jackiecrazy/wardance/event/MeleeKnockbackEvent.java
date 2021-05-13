package jackiecrazy.wardance.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public class MeleeKnockbackEvent extends LivingEvent {
    protected final float originalStrength;
    protected final double originalRatioX, originalRatioZ;
    protected LivingEntity attacker;
    protected DamageSource ds;
    protected float strength;
    protected double ratioX, ratioZ;

    public MeleeKnockbackEvent(LivingEntity attacker, DamageSource source, LivingEntity target, float strength, double ratioX, double ratioZ) {
        super(target);
        this.strength = this.originalStrength = strength;
        this.ratioX = this.originalRatioX = ratioX;
        this.ratioZ = this.originalRatioZ = ratioZ;
        ds = source;
        this.attacker = attacker;
    }

    public float getStrength() {return this.strength;}

    public void setStrength(float strength) {this.strength = strength;}

    public double getRatioX() {return this.ratioX;}

    public void setRatioX(double ratioX) {this.ratioX = ratioX;}

    public double getRatioZ() {return this.ratioZ;}

    public void setRatioZ(double ratioZ) {this.ratioZ = ratioZ;}

    public float getOriginalStrength() {return this.originalStrength;}

    public double getOriginalRatioX() {return this.originalRatioX;}

    public double getOriginalRatioZ() {return this.originalRatioZ;}

    public LivingEntity getAttacker() {
        return attacker;
    }

    public DamageSource getDamageSource() {
        return ds;
    }
}
