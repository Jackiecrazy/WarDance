package jackiecrazy.wardance.capability.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

public class CombatManipulator{

    @CapabilityInject(ICombatItemCapability.class)
    public static Capability<ICombatItemCapability> CAP = null;

    public static LazyOptional<ICombatItemCapability> getCap(ItemStack le) {
        return le.getCapability(CAP);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }
}
