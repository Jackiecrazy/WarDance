package jackiecrazy.wardance.capability.kits;

import net.minecraft.item.ItemStack;

public interface IKitItemStack {
    void setKitItem(ItemStack is);
    ItemStack getKitItem();
}
