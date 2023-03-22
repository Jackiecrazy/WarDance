package jackiecrazy.wardance.capability.skill;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CasterData implements ICapabilitySerializable<CompoundTag> {
    public static Capability<ISkillCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static ISkillCapability OHNO = new DummySkillCap();
    protected final ISkillCapability instance;

    public CasterData() {
        this(new DummySkillCap());
    }

    public CasterData(ISkillCapability cap) {
        instance = cap;
    }

    public static ISkillCapability getCap(LivingEntity le) {
        if (le == null) return OHNO;
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(() -> instance));
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.write();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.read(nbt);
    }
}
