package jackiecrazy.wardance.capability.aerial;

import jackiecrazy.wardance.capability.aerial.IAerialMode;
import jackiecrazy.footwork.capability.timeslow.TimeCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AerialModeData implements ICapabilitySerializable<Tag> {
    public AerialModeData(Entity bound) {
        this.instance = new AerialCapability(bound);
    }

    public AerialModeData() {
    }

    private static IAerialMode OHNO = new AerialCapability();

    public static Capability<IAerialMode> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static IAerialMode getCap(Entity le) {
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }


    private IAerialMode instance = new AerialCapability();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(()->instance));
    }

    @Override
    public Tag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {

    }
}
