package jackiecrazy.wardance.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CasterData implements ICapabilitySerializable<CompoundNBT> {
    private static ISkillCapability OHNO=new DummySkillCap();

    @CapabilityInject(ISkillCapability.class)
    public static Capability<ISkillCapability> CAP = null;

    public static ISkillCapability getCap(LivingEntity le) {
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    private final LazyOptional<ISkillCapability> instance;

    public CasterData(LivingEntity e) {
        instance = LazyOptional.of(() -> new SkillCapability(e));
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
