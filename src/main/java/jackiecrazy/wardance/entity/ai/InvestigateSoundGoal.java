package jackiecrazy.wardance.entity.ai;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.goal.GoalCapabilityProvider;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class InvestigateSoundGoal extends MoveToBlockGoal {
    int hesitation;

    public InvestigateSoundGoal(CreatureEntity c) {
        super(c, 0.6, 32);
    }

    @Override
    public void start() {
        super.start();
        hesitation = WarDance.rand.nextInt(60) + 20;
    }

    @Override
    public boolean canUse() {
        //only use if idle
        if (mob.getTarget() != null) return false;
        return findNearestBlock();
    }

    @Override
    protected boolean isValidTarget(IWorldReader w, BlockPos b) {
        double rangesq = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        rangesq *= rangesq;
        return (mob.blockPosition().distSqr(b) < rangesq);
    }

    @Override
    protected boolean findNearestBlock() {
        mob.getCapability(GoalCapabilityProvider.CAP).ifPresent(a -> {
            if (isValidTarget(mob.level, a.getSoundLocation()))
                this.blockPos = a.getSoundLocation();
            else this.blockPos = BlockPos.ZERO;
        });
        return this.blockPos != BlockPos.ZERO;
    }

    @Override
    public double acceptedDistance() {
        return 1 + (int) (mob.getX() * mob.getZ()) & 15;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getTarget() == null && mob.getLastHurtMob() == null && super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (--hesitation < 0)
            super.tick();
    }

    @Override
    public void stop() {
        super.stop();
        mob.getCapability(GoalCapabilityProvider.CAP).ifPresent(a -> a.setSoundLocation(BlockPos.ZERO));
    }
}
