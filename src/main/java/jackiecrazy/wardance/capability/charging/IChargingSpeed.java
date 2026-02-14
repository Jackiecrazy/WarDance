package jackiecrazy.wardance.capability.charging;

import net.minecraft.world.item.ItemStack;

public interface IChargingSpeed {
    void alterSpeed(ItemStack is, double speed);
    int tick(ItemStack stack);
}
