package jackiecrazy.wardance.capability.charging;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChargingCapability implements IChargingSpeed {
    Item bind;
    private double speed = 1;
    private double partial = 0;

    public ChargingCapability() {
    }

    @Override
    public void alterSpeed(ItemStack stack, double speed) {
        this.bind = stack.getItem();
        this.speed = speed;
        partial = 0;
    }

    @Override
    public int tick(ItemStack stack) {
        if (stack.getItem() != bind) return 1;
        partial += speed;
        int ret = 0;
        while (partial >= 1) {
            ret++;
            partial--;
        }
        return ret;
    }
}
