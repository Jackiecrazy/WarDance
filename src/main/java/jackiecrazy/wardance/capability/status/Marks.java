package jackiecrazy.wardance.capability.status;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Marks implements ICapabilitySerializable<CompoundNBT> {
    private static IMark OHNO=new DummyMarkCap();

    @CapabilityInject(IMark.class)
    public static Capability<IMark> CAP = null;

    public static IMark getCap(LivingEntity le) {
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    private final LazyOptional<IMark> instance;

    public Marks(LivingEntity e) {
        instance = LazyOptional.of(() -> new Mark(e));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, instance);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) CAP.getStorage().writeNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CAP.getStorage().readNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null, nbt);
    }
}
