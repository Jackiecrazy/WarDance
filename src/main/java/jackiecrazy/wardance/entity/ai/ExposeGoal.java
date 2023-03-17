package jackiecrazy.wardance.entity.ai;

import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ExposeGoal extends Goal {
    static final EnumSet<Flag> mutex = EnumSet.allOf(Flag.class);
    LivingEntity e;

    public ExposeGoal(LivingEntity bind) {
        e = bind;
    }

    @Override
    public boolean canUse() {
        return CombatData.getCap(e).isExposed();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public EnumSet<Flag> getFlags() {
        return mutex;
    }
}
