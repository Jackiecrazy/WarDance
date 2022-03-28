package jackiecrazy.wardance.entity.ai;

import jackiecrazy.wardance.capability.goal.GoalCapabilityProvider;
import jackiecrazy.wardance.capability.goal.IGoalHelper;
import jackiecrazy.wardance.potion.WarEffects;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Optional;

public class FearGoal extends AvoidEntityGoal<LivingEntity> {
    public FearGoal(CreatureEntity entityIn) {
        super(entityIn, LivingEntity.class, 16, 1.3, 1.6);
    }

    public boolean canUse() {
        if (!mob.hasEffect(WarEffects.FEAR.get())) return false;
        this.toAvoid = mob.getKillCredit();
        final Optional<IGoalHelper> goal = GoalCapabilityProvider.getCap(mob).resolve();
        goal.ifPresent(iGoalHelper -> toAvoid = iGoalHelper.getFearSource());
        if (this.toAvoid == null) {
            return false;
        } else {
            Vector3d vector3d = RandomPositionGenerator.getPosAvoid(this.mob, 16, 7, this.toAvoid.position());
            if (vector3d == null) {
                return false;
            } else if (this.toAvoid.distanceToSqr(vector3d.x, vector3d.y, vector3d.z) < this.toAvoid.distanceToSqr(this.mob)) {
                return false;
            } else {
                this.path = this.pathNav.createPath(vector3d.x, vector3d.y, vector3d.z, 0);
                return this.path != null;
            }
        }
    }
}
