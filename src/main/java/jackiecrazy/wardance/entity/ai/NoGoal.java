package jackiecrazy.wardance.entity.ai;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.potion.WarEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class NoGoal extends Goal {
    static final EnumSet<Flag> mutex = EnumSet.allOf(Flag.class);
    LivingEntity e;

    public NoGoal(LivingEntity bind) {
        e = bind;
    }

    @Override
    public boolean canUse() {
        return (CombatData.getCap(e).isValid() && CombatData.getCap(e).getStaggerTime() > 0) || e.hasEffect(WarEffects.PETRIFY.get()) || e.hasEffect(WarEffects.PARALYSIS.get()) || e.hasEffect(WarEffects.SLEEP.get());
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
