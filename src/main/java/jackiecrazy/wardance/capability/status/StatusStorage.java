package jackiecrazy.wardance.capability.status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class StatusStorage implements Capability.IStorage<IMark> {
    @Nullable
    @Override
    public Tag writeNBT(Capability<IMark> capability, IMark IMark, Direction direction) {
        return IMark.write();
    }

    @Override
    public void readNBT(Capability<IMark> capability, IMark IMark, Direction direction, Tag inbt) {
        if(inbt instanceof CompoundTag) {
            IMark.read((CompoundTag) inbt);
        }
    }
}
