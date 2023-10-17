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
        float prev = stats.getArbitraryFloat();
        if (caster.isOnGround()) {
            stats.setArbitraryFloat((float) caster.getY());
        } else if (caster.getDeltaMovement().y() > 0) {
            stats.setArbitraryFloat((float) caster.getY());
        }
        return stats.getArbitraryFloat() != prev;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingEvent.LivingJumpEvent) {
            silentActivate(caster, 0F, false, (float) caster.getY());
        }
        if (procPoint instanceof LivingFallEvent) {
            stats.setArbitraryFloat((float) caster.getY());
        }
        if (procPoint instanceof EntityAwarenessEvent e && e.getAttacker() == caster && state == STATE.ACTIVE) {
            StealthUtils.Awareness awareness = e.getAwareness();
            double posDiff = stats.getArbitraryFloat() - caster.getY();
            int length = 0;
            if (posDiff > 1)
                ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), caster, e.getEntity().position(), 0, posDiff, getColor(), 0);
            while ((posDiff -= (7 / stats.getEffectiveness())) > 0) {
                switch (awareness) {
                    case UNAWARE -> length += 60;
                    case DISTRACTED -> awareness = StealthUtils.Awareness.UNAWARE;
                    case ALERT -> awareness = StealthUtils.Awareness.DISTRACTED;
                }
            }
            if (length >= 600) completeChallenge(caster);
            if (length > 0) e.getEntity().addEffect(new MobEffectInstance(FootworkEffects.PARALYSIS.get(), length));
            e.setAwareness(awareness);
            stats.setArbitraryFloat((float) caster.getY());
            caster.fallDistance = 0;
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.ACTIVE);
        }
        return passive(prev, from, to);
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }
}