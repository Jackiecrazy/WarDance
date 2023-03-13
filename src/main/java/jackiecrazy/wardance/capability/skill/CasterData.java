package jackiecrazy.wardance.capability.skill;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CasterData implements ICapabilitySerializable<CompoundTag> {
    @CapabilityInject(ISkillCapability.class)
    public static Capability<ISkillCapability> CAP = null;
    private static ISkillCapability OHNO = new DummySkillCap();
    private final LazyOptional<ISkillCapability> instance;

    public CasterData(LivingEntity e) {
        instance = LazyOptional.of(() -> new SkillCapability(e));
    }

    public static ISkillCapability getCap(LivingEntity le) {
        if (le == null) return OHNO;
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return (CompoundTag) CAP.getStorage().writeNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CAP.getStorage().readNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null, nbt);
    }
}
