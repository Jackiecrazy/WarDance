package jackiecrazy.wardance.capability.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public interface IGoalHelper {
    void setForcedTarget(LivingEntity e);
    LivingEntity getForcedTarget();
    void setFearSource(LivingEntity e);
    LivingEntity getFearSource();
    void setSoundLocation(BlockPos pos);
    BlockPos getSoundLocation();
}
