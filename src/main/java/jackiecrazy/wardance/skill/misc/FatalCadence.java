package jackiecrazy.wardance.skill.misc;

import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;

public class FatalCadence extends Skill {

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof SkillCastEvent sre && procPoint.getPhase() == EventPriority.HIGHEST) {
            stats.setMaxDuration(4);
            stats.decrementDuration(1);
            caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.NOTE_BLOCK_XYLOPHONE.get(), SoundSource.PLAYERS, 0.5f + 0.25f * (4 - stats.getDuration()), 0.5f + 0.25f * (4 - stats.getDuration()));
            if (stats.getDuration() <= 0) {
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
