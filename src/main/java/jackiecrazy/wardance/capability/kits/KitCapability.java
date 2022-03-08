package jackiecrazy.wardance.capability.kits;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class KitCapability implements IKitItemStack {
    private ItemStack stack=ItemStack.EMPTY;

    @Override
    public void setKitItem(ItemStack is) {
        stack=is.copy();
    }

    @Override
    public ItemStack getKitItem() {
        return stack;
    }

    public static class Storage implements Capability.IStorage<IKitItemStack> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IKitItemStack> capability, IKitItemStack instance, Direction side) {
            CompoundNBT ret= new CompoundNBT();
            ret.put("stack", instance.getKitItem().save(new CompoundNBT()));
            return ret;
        }

        @Override
        public void readNBT(Capability<IKitItemStack> capability, IKitItemStack instance, Direction side, INBT nbt) {
            instance.setKitItem(ItemStack.of(((CompoundNBT)nbt).getCompound("stack")));
        }
    }
}
