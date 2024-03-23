package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class HeavyBlow extends Skill {
    private final HashSet<String> tags = makeTag(SkillTags.passive, SkillTags.disable_shield);

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.heavy_blow;
    }

    @Override
    public HashSet<String> getTags() {
        return tags;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent crit && caster != target && procPoint.getPhase() == EventPriority.LOWEST) {
            if (isCrit(crit) && state == STATE.INACTIVE && cast(caster, target, -999)) {
                onCrit(crit, stats, caster, target);
            } else if (state == STATE.COOLING) {
                stats.decrementDuration();
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 3);
        }
        return passive(prev, from, to);
    }

    protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {
        if (this == WarSkills.VITAL_STRIKE.get())
            proc.setDamageModifier(proc.getDamageModifier() * 1.5f * stats.getEffectiveness() * stats.getEffectiveness());
    }

    public static class Leverage extends HeavyBlow {

        @Override
        protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {
            proc.setDamageModifier(1 + (float) Math.min(2, Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) / 4f) * SkillUtils.getSkillEffectiveness(caster));
            if(proc.getDamageModifier()>=2)completeChallenge(caster);
//            Vector3d extra = caster.position().vectorTo(target.position()).scale(-1);
//            if (extra.lengthSqr() > 1) extra = extra.normalize();
//            caster.setDeltaMovement(caster.getDeltaMovement().add(extra));
//            caster.hurtMarked = true;
        }
    }
}
