package jackiecrazy.wardance.event;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public abstract class ProjectileDefendEvent extends LivingEvent {
    private final Entity projectile;
    private final InteractionHand defendingHand;
    private final ItemStack defendingStack;
    private final float originalPostureConsumption;
    private final Vec3 originalReturnVec;
    private float postureConsumption, rallyPerc;
    private boolean trigger;
    /**
     * null to delete.
     */
    private Vec3 returnVec;

    public ProjectileDefendEvent(LivingEntity entity, Entity seme, InteractionHand dhand, ItemStack d, float mult) {
        super(entity);
        projectile = seme;
        defendingHand = dhand;
        defendingStack = d;
        CombatUtils.initializePPE(this);
        rallyPerc=mult;
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

    public InteractionHand getDefendingHand() {
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

    public Vec3 getOriginalReturnVec() {return originalReturnVec;}

    public Vec3 getReturnVec() {return returnVec;}

    public void setReturnVec(Vec3 vec) {returnVec = vec;}

    public float getRallyPercentage() {
        return rallyPerc;
    }

    public void setRallyPercentage(float rallyPerc) {
        this.rallyPerc = rallyPerc;
    }

    public static class Block extends ProjectileDefendEvent {

        public Block(LivingEntity entity, Entity seme, InteractionHand dhand, ItemStack d, float mult) {
            super(entity, seme, dhand, d, mult);
        }
    }

    public static class Parry extends ProjectileDefendEvent {

        public Parry(LivingEntity entity, Entity seme, InteractionHand dhand, ItemStack d, float mult) {
            super(entity, seme, dhand, d, mult);
            setPostureConsumption(0);
            setReturnVec(entity.getLookAngle().scale(seme.getDeltaMovement().length()));
            setTrigger(false);
        }
    }
}
