package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.event.SkillCastEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class FatalCadence extends Skill {

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof SkillCastEvent sre && procPoint.getPhase() == EventPriority.HIGHEST) {
            stats.setMaxDuration(4);
            stats.decrementDuration(1);
            if (stats.getDuration() <=0) {
                stats.setDuration(4);
                sre.setEffectiveness(sre.getEffectiveness() + 0.4);
            }
            stats.markDirty();
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.ACTIVE);
        return true;
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }
}
