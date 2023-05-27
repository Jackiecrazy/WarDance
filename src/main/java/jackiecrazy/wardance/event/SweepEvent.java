package jackiecrazy.wardance.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

public class SweepEvent extends LivingEvent {
    private final int oangle;
    private int a;
    private final InteractionHand hand;
    private final ItemStack stack;

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public SweepEvent(LivingEntity entity, InteractionHand hand, ItemStack stack, int angle) {
        super(entity);
        oangle = a = angle;
        this.hand = hand;
        this.stack = stack;
    }

    public int getOriginalAngle() {
        return oangle;
    }

    public int getAngle() {
        return a;
    }

    public SweepEvent setAngle(int a) {
        this.a = a;
        return this;
    }
}
