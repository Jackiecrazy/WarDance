package jackiecrazy.wardance.capability.kits;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

public class KitCapabilityProvider {

    @CapabilityInject(IKitItemStack.class)
    public static Capability<IKitItemStack> CAP = null;

    public static LazyOptional<IKitItemStack> getCap(ItemStack le) {
        return le.getCapability(CAP);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }
}
