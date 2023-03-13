package jackiecrazy.wardance.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired whenever an entity parries a melee attack.
 * This event has a result. ALLOW will force a parry, while DENY will cancel a parry.
 * This event can be canceled. If the event is canceled, the attack event will also be canceled.
 */
@Cancelable
@Event.HasResult
public class ParryEvent extends LivingEvent {
    private final boolean originally;
    private final LivingEntity attacker;
    private final InteractionHand attackingHand, defendingHand;
    private final ItemStack attackingStack;
    private final ItemStack defendingStack;
    private final float originalPostureConsumption, attackDamage;
    private float postureConsumption;

    public ParryEvent(LivingEntity entity, LivingEntity seme, boolean canParry, InteractionHand hand, ItemStack a, InteractionHand dhand, ItemStack d, float posture, float orig, float damage) {
        super(entity);
        originally = canParry;
        attacker = seme;
        attackingHand = hand;
        attackingStack = a;
        defendingHand = dhand;
        defendingStack = d;
        originalPostureConsumption = orig;
        postureConsumption = posture;
        attackDamage = damage;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public InteractionHand getAttackingHand() {
        return attackingHand;
    }

    public InteractionHand getDefendingHand() {
        return defendingHand;
    }

    public ItemStack getAttackingStack() {
        return attackingStack;
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

    public float getAttackDamage() {return attackDamage;}

    public boolean canParry() {
        return getResult() == Event.Result.ALLOW || (originally && getResult() == Event.Result.DEFAULT);
    }
}
