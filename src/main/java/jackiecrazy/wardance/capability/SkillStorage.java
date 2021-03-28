package jackiecrazy.wardance.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;


import javax.annotation.Nullable;

public class SkillStorage implements Capability.IStorage<ISkillCapability> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<ISkillCapability> capability, ISkillCapability ISkillCapability, Direction direction) {
        return ISkillCapability.write(new CompoundNBT());
    }

    @Override
    public void readNBT(Capability<ISkillCapability> capability, ISkillCapability ISkillCapability, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            ISkillCapability.read((CompoundNBT) inbt);
        }
    }
}
