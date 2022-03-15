package jackiecrazy.wardance.entity.ai;

import jackiecrazy.wardance.capability.goal.GoalCapabilityProvider;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class InvestigateSoundGoal extends MoveToBlockGoal {
    public InvestigateSoundGoal(CreatureEntity c) {
        super(c, 0.6, 32);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public boolean canUse() {
        //only use if idle
        if (mob.getTarget() != null || mob.getLastHurtMob() != null) return false;
        return super.canUse();
    }

    @Override
    protected boolean isValidTarget(IWorldReader p_179488_1_, BlockPos p_179488_2_) {
        return true;
    }

    @Override
    protected boolean findNearestBlock() {
        BlockPos blockpos = this.mob.blockPosition();
        mob.getCapability(GoalCapabilityProvider.CAP).ifPresent(a -> {
            double rangesq = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
            rangesq *= rangesq;
            if (blockpos.distSqr(a.getSoundLocation()) < rangesq)
                this.blockPos = a.getSoundLocation();
            else this.blockPos = BlockPos.ZERO;
        });
        return this.blockPos == BlockPos.ZERO;
    }

    @Override
    public double acceptedDistance() {
        return (int) mob.getX() & (int) mob.getZ() & 7;
    }

    @Override
    public boolean canContinueToUse() {
        return !isReachedTarget() && mob.getTarget() == null && mob.getLastHurtMob() == null && super.canContinueToUse();
    }

    @Override
    public void tick() {
        super.tick();
    }

}
