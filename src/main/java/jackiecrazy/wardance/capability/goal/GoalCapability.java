package jackiecrazy.wardance.capability.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class GoalCapability implements IGoalHelper {
    private BlockPos p = BlockPos.ZERO.below(100);
    private LivingEntity target;

    public GoalCapability() {}

    @Override
    public LivingEntity getForcedTarget() {
        return target;
    }

    @Override
    public void setForcedTarget(LivingEntity e) {
        target = e;
    }

    @Override
    public BlockPos getSoundLocation() {
        return p;
    }

    @Override
    public void setSoundLocation(BlockPos pos) {
        p=pos;
    }

    public static class Storage implements Capability.IStorage<IGoalHelper> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IGoalHelper> capability, IGoalHelper instance, Direction side) {
            CompoundNBT ret = new CompoundNBT();
            return ret;
        }

        @Override
        public void readNBT(Capability<IGoalHelper> capability, IGoalHelper instance, Direction side, INBT nbt) {
        }
    }
}
