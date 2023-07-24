package jackiecrazy.wardance.event;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

public class SweepEvent extends LivingEvent {
    private final double oangle;
    private double a;
    private final InteractionHand hand;
    private final ItemStack stack;
    private CombatUtils.SWEEPTYPE t;

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public SweepEvent(LivingEntity entity, InteractionHand hand, ItemStack stack, CombatUtils.SWEEPTYPE type, double width) {
        super(entity);
        oangle = a = width;
        this.hand = hand;
        this.stack = stack;
        t=type;
    }

    public double getOriginalWidth() {
        return oangle;
    }

    public double getWidth() {
        return a;
    }

    public void setWidth(int a) {
        this.a = a;
    }

    public CombatUtils.SWEEPTYPE getType() {
        return t;
    }

    public void setType(CombatUtils.SWEEPTYPE t) {
        this.t = t;
    }
}
