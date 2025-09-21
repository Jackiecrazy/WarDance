package jackiecrazy.wardance.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class MeleePostureEvent extends LivingEvent {
    protected final LivingEntity attacker;
    protected final InteractionHand attackingHand;
    protected final ItemStack attackingStack;
    protected final DamageSource ds;
    protected final float originalPostureConsumption;
    protected final float attackDamage;
    protected float postureConsumption;
    protected boolean canBreach;

    public MeleePostureEvent(LivingEntity entity, LivingEntity seme, InteractionHand hand, ItemStack a, DamageSource ds, float orig, float posture, float damage, boolean breach) {
        super(entity);
        attacker = seme;
        attackingHand = hand;
        attackingStack = a;
        this.ds = ds;
        originalPostureConsumption = orig;
        attackDamage = damage;
        postureConsumption = posture;
        canBreach = breach;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public InteractionHand getAttackingHand() {
        return attackingHand;
    }

    public ItemStack getAttackingStack() {
        return attackingStack;
    }

    public float getOriginalPostureConsumption() {
        return originalPostureConsumption;
    }

    public float getPostureConsumption() {
        return postureConsumption;
    }

    public void setPostureConsumption(float amount) {
        postureConsumption = amount;
        //fixme damage is becoming posture somewhere in the line
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public DamageSource getDamageSource() {
        return ds;
    }

    public boolean canBreach() {
        return canBreach;
    }

    /**
     * This event is fired whenever an entity parries a melee attack.
     * This event has a result. ALLOW will force a parry, while DENY will cancel a parry.
     * There are three subevents:
     * Pre is run before parry and block checks
     */
    public abstract static class Defense extends MeleePostureEvent {
        protected final InteractionHand defendingHand;
        final boolean originally;
        private final ItemStack defendingStack;

        public Defense(LivingEntity entity, LivingEntity seme, boolean canParry, InteractionHand hand, ItemStack a, InteractionHand dhand, ItemStack d, float posture, float orig, DamageSource ds, float damage, boolean canBreach) {
            super(entity, seme, hand, a, ds, damage, orig, posture, canBreach);
            originally = canParry;
            defendingHand = dhand;
            defendingStack = d;
        }

        public InteractionHand getDefendingHand() {
            return defendingHand;
        }

        public ItemStack getDefendingStack() {
            return defendingStack;
        }

        public abstract boolean success();
    }


    /**
     * this is where you should modify posture damage dealt on a melee hit
     */
    public static class Pre extends MeleePostureEvent {

        public Pre(LivingEntity entity, LivingEntity seme, InteractionHand hand, ItemStack a, float posture, float orig, DamageSource ds, float damage, boolean canBreach) {
            super(entity, seme, hand, a, ds, posture, orig, damage, canBreach);
        }
    }

    /**
     * by convention you should only use this for what happens on a guard
     */
    @HasResult
    public static class Guard extends Defense {

        public Guard(LivingEntity entity, LivingEntity seme, boolean canParry, InteractionHand hand, ItemStack a, InteractionHand dhand, ItemStack d, float posture, float orig, DamageSource ds, float damage, boolean canBreach) {
            super(entity, seme, canParry, hand, a, dhand, d, posture, orig, ds, damage, canBreach);
        }


        public boolean success() {
            return getResult() == Result.ALLOW || (originally && getResult() == Result.DEFAULT);
        }
    }

    /**
     * by convention you should only use this for what happens on a block
     */
    @HasResult
    public static class Block extends Defense {

        public Block(LivingEntity entity, LivingEntity seme, boolean canParry, InteractionHand hand, ItemStack a, InteractionHand dhand, ItemStack d, float posture, float orig, DamageSource ds, float damage, boolean canBreach) {
            super(entity, seme, canParry, hand, a, dhand, d, posture, orig, ds, damage, canBreach);
        }


        public boolean success() {
            return getResult() == Result.ALLOW || (originally && getResult() == Result.DEFAULT);
        }
    }

    /**
     * by convention you should only use this for what happens on a parry
     */
    @HasResult
    public static class Parry extends Defense {

        public Parry(LivingEntity entity, LivingEntity seme, boolean canParry, InteractionHand hand, ItemStack a, InteractionHand dhand, ItemStack d, float posture, float orig, DamageSource ds, float damage, boolean canBreach) {
            super(entity, seme, canParry, hand, a, dhand == null ? InteractionHand.MAIN_HAND : dhand, d == null ? ItemStack.EMPTY : d, posture, orig, ds, damage, canBreach);
        }

        public boolean success() {
            return getResult() == Result.ALLOW || (originally && getResult() == Result.DEFAULT);
        }
    }

    /**
     * by convention you should only use this for what happens on a parry
     */
    @HasResult
    public static class Environment extends Parry {

        public Environment(LivingEntity entity, boolean canParry, float posture, DamageSource ds, float damage, boolean canBreach) {
            super(entity, null, canParry, InteractionHand.MAIN_HAND, ItemStack.EMPTY, InteractionHand.MAIN_HAND, ItemStack.EMPTY, posture, posture, ds, damage, canBreach);
        }
    }
}
