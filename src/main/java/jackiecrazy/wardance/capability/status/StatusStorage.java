package jackiecrazy.wardance.capability.status;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class StatusStorage implements Capability.IStorage<IStatus> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IStatus> capability, IStatus IStatus, Direction direction) {
        return IStatus.write();
    }

    @Override
    public void readNBT(Capability<IStatus> capability, IStatus IStatus, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            IStatus.read((CompoundNBT) inbt);
        }
    }
}
