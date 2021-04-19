package jackiecrazy.wardance.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public class ParryEvent extends LivingEvent {
    private final LivingEntity attacker;
    private final Hand attackingHand, defendingHand;
    private final ItemStack attackingStack;
    private final ItemStack defendingStack;
    private final float originalPostureConsumption;
    private float postureConsumption;

    public ParryEvent(LivingEntity entity, LivingEntity seme, Hand hand, ItemStack a, Hand dhand, ItemStack d, float posture) {
        super(entity);
        attacker = seme;
        attackingHand = hand;
        attackingStack = a;
        defendingHand = dhand;
        defendingStack = d;
        originalPostureConsumption = postureConsumption = posture;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public Hand getAttackingHand() {
        return attackingHand;
    }

    public Hand getDefendingHand() {
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
}