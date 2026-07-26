package jackiecrazy.wardance.event;

import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class PlayInteractionEvent extends LivingEvent {
    protected final InteractionHand hand;
    protected final ItemStack stack;
    protected WeaponStats.AttackType state;
    protected WeaponInteractions.InteractionGroup interact=null;

    public PlayInteractionEvent(LivingEntity entity,
                                InteractionHand hand,
                                ItemStack stack,
                                WeaponStats.AttackType state) {
        super(entity);
        this.hand = hand;
        this.stack = stack;
        this.state = state;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public WeaponStats.AttackType getOriginalState() {
        return state;
    }

    @Cancelable
    public static class Pre extends PlayInteractionEvent {
        protected WeaponStats.AttackType state;

        public Pre(LivingEntity entity,
                   InteractionHand hand,
                   ItemStack stack,
                   WeaponStats.AttackType state) {
            super(entity, hand, stack, state);
            this.state=state;
        }

        public WeaponStats.AttackType getMoveState() {
            return state;
        }

        public Pre setMoveState(WeaponStats.AttackType state) {
            this.state = state;
            return this;
        }

        public PlayInteractionEvent setInteraction(WeaponInteractions.InteractionGroup interact) {
            this.interact = interact;
            return this;
        }

        public WeaponInteractions.InteractionGroup getInteraction() {
            return interact;
        }
    }

    public static class Post extends PlayInteractionEvent {
        protected WeaponInteractions.InteractionGroup orig;

        public Post(LivingEntity entity,
                    InteractionHand hand,
                    ItemStack stack,
                    WeaponStats.AttackType state,
                    WeaponInteractions.InteractionGroup interact) {
            super(entity, hand, stack, state);
            this.orig = this.interact = interact;
        }

        public WeaponInteractions.InteractionGroup getOriginalInteraction() {
            return orig;
        }

        public PlayInteractionEvent setInteraction(WeaponInteractions.InteractionGroup interact) {
            this.interact = interact;
            return this;
        }

        public WeaponInteractions.InteractionGroup getInteraction() {
            return interact;
        }
    }
}
