package jackiecrazy.wardance.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CombatStorage implements Capability.IStorage<ICombatCapability> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<ICombatCapability> capability, ICombatCapability iCombatCapability, Direction direction) {
        return iCombatCapability.write();
    }

    @Override
    public void readNBT(Capability<ICombatCapability> capability, ICombatCapability iCombatCapability, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            iCombatCapability.read((CompoundNBT) inbt);
        }
    }
}
