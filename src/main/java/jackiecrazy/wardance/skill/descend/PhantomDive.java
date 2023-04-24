package jackiecrazy.wardance.skill.descend;

import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class PhantomDive extends Skill {
    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (caster.getDeltaMovement().y() > 0)
                stats.setArbitraryFloat((float) caster.getY());
        }
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingEvent.LivingJumpEvent) {
            activate(caster, 1F, (float) caster.getY());
        }
        if (procPoint instanceof LivingFallEvent) {
            markUsed(caster);
        }
        if (procPoint instanceof EntityAwarenessEvent e && e.getAttacker() == caster) {
            StealthUtils.Awareness awareness = e.getAwareness();
            double posDiff = stats.getDuration() - caster.getY();
            int length = 0;
            while (posDiff > 0) {
                switch (awareness) {
                    case ALERT -> awareness = StealthUtils.Awareness.DISTRACTED;
                    case DISTRACTED -> awareness = StealthUtils.Awareness.UNAWARE;
                    case UNAWARE -> length += 60;
                }
                posDiff -= 7;
            }
            if (length > 0) e.getEntity().addEffect(new MobEffectInstance(FootworkEffects.PARALYSIS.get(), length));
            ((EntityAwarenessEvent) procPoint).setAwareness(awareness);
            caster.fallDistance = 0;
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (prev.getState() == STATE.ACTIVE && to == STATE.COOLING)
            prev.setState(STATE.INACTIVE);
        return passive(prev, from, to);
    }
}