package jackiecrazy.wardance.skill.descend;

import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.ParticleUtils;
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
    public HashSet<String> getTags() {
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
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingEvent.LivingJumpEvent) {
            silentActivate(caster, 1F, false, (float) caster.getY());
        }
        if (procPoint instanceof LivingFallEvent) {
            markUsed(caster, true);
        }
        if (procPoint instanceof EntityAwarenessEvent e && e.getAttacker() == caster) {
            StealthUtils.Awareness awareness = e.getAwareness();
            double posDiff = stats.getDuration() - caster.getY();
            ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, e.getEntity().position(), 0, posDiff, getColor(), 0);
            int length = 0;
            while (posDiff > 0) {
                switch (awareness) {
                    case ALERT -> awareness = StealthUtils.Awareness.DISTRACTED;
                    case DISTRACTED -> awareness = StealthUtils.Awareness.UNAWARE;
                    case UNAWARE -> length += 60;
                }
                posDiff -= 7 / stats.getEffectiveness();
            }
            if (length > 0) e.getEntity().addEffect(new MobEffectInstance(FootworkEffects.PARALYSIS.get(), length));
            e.setAwareness(awareness);
            markUsed(caster);
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