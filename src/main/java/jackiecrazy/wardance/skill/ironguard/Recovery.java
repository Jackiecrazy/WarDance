package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashSet;

public class Recovery extends Skill {
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.iron_guard;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ConsumePostureEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (CombatData.getCap(caster).getPosture() < CombatData.getCap(caster).getMaxPosture() / 2)
                ((ConsumePostureEvent) procPoint).setResetCooldown(false);
            CombatData.getCap(caster).setMightGrace(ResourceConfig.qiGrace);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
