package jackiecrazy.wardance.event;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public class ProjectileParryEvent extends LivingEvent {
    private final Entity projectile;
    private final Hand defendingHand;
    private final ItemStack defendingStack;
    private final float originalPostureConsumption;
    private final Vector3d originalReturnVec;
    private float postureConsumption;
    private boolean trigger;
    /**
     * null to delete.
     */
    private Vector3d returnVec;

    public ProjectileParryEvent(LivingEntity entity, Entity seme, Hand dhand, ItemStack d, float mult) {
        super(entity);
        projectile = seme;
        defendingHand = dhand;
        defendingStack = d;
        CombatUtils.initializePPE(this, mult);
        originalPostureConsumption = postureConsumption;
        originalReturnVec = returnVec;
    }

    public boolean doesTrigger() {
        return trigger;
    }

    public void setTrigger(boolean trigger) {
        this.trigger = trigger;
    }

    public Entity getProjectile() {
        return projectile;
    }

    public Hand getDefendingHand() {
        return defendingHand;
    }

    public ItemStack getDefendingStack() {
        return defendingStack;
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

    public Vector3d getOriginalReturnVec() {return originalReturnVec;}

    public Vector3d getReturnVec() {return returnVec;}

    public void setReturnVec(Vector3d vec) {returnVec = vec;}
}
