package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;

public class Recovery extends Skill {

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.iron_guard;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ConsumePostureEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (CombatData.getCap(caster).getPosture() < CombatData.getCap(caster).getMaxPosture() * 0.25 * SkillUtils.getSkillEffectiveness(caster))
                ((ConsumePostureEvent) procPoint).setResetCooldown(false);
            CombatData.getCap(caster).setMightGrace(ResourceConfig.qiGrace);
        }
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return CombatData.getCap(caster).getPosture() < CombatData.getCap(caster).getMaxPosture() * 0.25 * SkillUtils.getSkillEffectiveness(caster);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
