package jackiecrazy.wardance.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;

public abstract class MeleePostureEvent extends ConsumePostureEvent {
    protected final LivingEntity attacker;
    protected final InteractionHand attackingHand;
    protected final ItemStack attackingStack;
    protected final DamageSource ds;
    protected final float attackDamage;

    public MeleePostureEvent(LivingEntity entity,
                             LivingEntity seme,
                             InteractionHand hand,
                             ItemStack a,
                             DamageSource ds,
                             float orig,
                             float posture,
                             float damage,
                             boolean breach) {
        super(entity, orig, posture, breach);
        attacker = seme;
        attackingHand = hand;
        attackingStack = a;
        this.ds = ds;
        attackDamage = damage;
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

    public float getAttackDamage() {
        return attackDamage;
    }

    public DamageSource getDamageSource() {
        return ds;
    }

    /**
     * This event is fired whenever an entity parries a melee attack.
     * This event has a result. ALLOW will force a parry, while DENY will cancel a parry.
     * There are three subevents:
     * Pre is run before parry and block checks
     * Block and parry run for their specific checks
     * Listening to this event might cause your code to run three times per block, so be careful.
     * Block and parry are cancelable. Canceling them causes the subsequent block/parry action to not run.
     */
    public abstract static class Defense extends MeleePostureEvent {
        protected final InteractionHand defendingHand;
        final boolean originally;
        private final ItemStack defendingStack;
        protected float rallyPercentage;

        public Defense(LivingEntity entity,
                       LivingEntity seme,
                       boolean canParry,
                       InteractionHand hand,
                       ItemStack a,
                       InteractionHand dhand,
                       ItemStack d,
                       float posture,
                       float orig,
                       DamageSource ds,
                       float damage,
                       float rallyPerc,
                       boolean canBreach) {
            super(entity, seme, hand, a, ds, orig, posture, damage, canBreach);
            originally = canParry;
            defendingHand = dhand;
            defendingStack = d;
            rallyPercentage=rallyPerc;
        }

        public InteractionHand getDefendingHand() {
            return defendingHand;
        }

        public ItemStack getDefendingStack() {
            return defendingStack;
        }

        public abstract boolean success();

        public float getRallyPercentage() {
            return rallyPercentage;
        }

        public void setRallyPercentage(float rallyPercentage) {
            this.rallyPercentage = rallyPercentage;
        }

    }


    /**
     * this is where you should modify posture damage dealt on a melee hit
     */
    public static class Pre extends MeleePostureEvent {

        public Pre(LivingEntity entity,
                   LivingEntity seme,
                   InteractionHand hand,
                   ItemStack a,
                   float posture,
                   float orig,
                   DamageSource ds,
                   float damage,
                   boolean canBreach) {
            super(entity, seme, hand, a, ds, posture, orig, damage, canBreach);
        }
        @Override
        public TYPE getType() {
            return TYPE.NONE;
        }
    }

    /**
     * by convention you should only use this for what happens on a block
     */
    @HasResult
    @Cancelable
    public static class Block extends Defense {

        public Block(LivingEntity entity,
                     LivingEntity seme,
                     boolean canParry,
                     InteractionHand hand,
                     ItemStack a,
                     InteractionHand dhand,
                     ItemStack d,
                     float posture,
                     float orig,
                     DamageSource ds,
                     float damage,
                     float rallyPerc,
                     boolean canBreach) {
            super(entity, seme, canParry, hand, a, dhand, d, posture, orig, ds, damage, rallyPerc, canBreach);
        }


        public boolean success() {
            return getResult() == Result.ALLOW || (originally && getResult() == Result.DEFAULT);
        }

        @Override
        public TYPE getType() {
            return TYPE.BLOCK;
        }
    }

    /**
     * by convention you should only use this for what happens on a parry
     */
    @HasResult
    @Cancelable
    public static class Parry extends Defense {

        public Parry(LivingEntity entity,
                     LivingEntity seme,
                     boolean canParry,
                     InteractionHand hand,
                     ItemStack a,
                     InteractionHand dhand,
                     ItemStack d,
                     float posture,
                     float orig,
                     DamageSource ds,
                     float damage,
                     float rallyPerc,
                     boolean canBreach) {
            super(entity, seme, canParry, hand, a, dhand == null ? InteractionHand.MAIN_HAND : dhand, d == null ? ItemStack.EMPTY : d, posture, orig, ds, damage, rallyPerc, canBreach);
        }

        public boolean success() {
            return getResult() == Result.ALLOW || (originally && getResult() == Result.DEFAULT);
        }
        @Override
        public TYPE getType() {
            return TYPE.PARRY;
        }
    }

    /**
     * by convention you should only use this for what happens on a parry
     */
    @HasResult
    @Cancelable
    public static class Environment extends Parry {

        public Environment(LivingEntity entity,
                           boolean canParry,
                           float posture,
                           DamageSource ds,
                           float damage,
                           boolean canBreach) {
            super(entity, null, canParry, InteractionHand.MAIN_HAND, ItemStack.EMPTY, InteractionHand.MAIN_HAND, ItemStack.EMPTY, posture, posture, ds, damage, 1, canBreach);
        }
    }
}
