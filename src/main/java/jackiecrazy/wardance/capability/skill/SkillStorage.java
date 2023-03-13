package jackiecrazy.wardance.capability.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;


import javax.annotation.Nullable;

public class SkillStorage implements Capability.IStorage<ISkillCapability> {
    @Nullable
    @Override
    public Tag writeNBT(Capability<ISkillCapability> capability, ISkillCapability ISkillCapability, Direction direction) {
        return ISkillCapability.write();
    }

    @Override
    public void readNBT(Capability<ISkillCapability> capability, ISkillCapability ISkillCapability, Direction direction, Tag inbt) {
        if(inbt instanceof CompoundTag) {
            ISkillCapability.read((CompoundTag) inbt);
        }
    }
}
