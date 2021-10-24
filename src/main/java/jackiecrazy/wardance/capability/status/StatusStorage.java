package jackiecrazy.wardance.capability.status;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class StatusStorage implements Capability.IStorage<IMark> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IMark> capability, IMark IMark, Direction direction) {
        return IMark.write();
    }

    @Override
    public void readNBT(Capability<IMark> capability, IMark IMark, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            IMark.read((CompoundNBT) inbt);
        }
    }
}
