package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class HeavyBlow extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.disable_shield, ProcPoints.melee, ProcPoints.on_hurt, "boundCast", ProcPoints.normal_attack, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.on_being_parried)));
    private final Tag<String> tags = makeTag(SkillTags.physical, SkillTags.forced_crit, SkillTags.passive, SkillTags.offensive, SkillTags.disable_shield);

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return Tag.getEmptyTag();
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.heavy_blow;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (((CriticalHitEvent) procPoint).isVanillaCritical() && state == STATE.INACTIVE) {
                onCrit((CriticalHitEvent) procPoint, stats, caster, target);
                markUsed(caster);
            } else if (state == STATE.COOLING) {
                stats.decrementDuration();
            }
        }
    }

    protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {
        proc.setDamageModifier(proc.getDamageModifier() * 1.3f);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, 3);
        }
        return passive(prev, from, to);
    }


    public static class Leverage extends HeavyBlow {

        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {
            proc.setDamageModifier(1 + (float) Math.max(2, Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target)) / 4f));
            Vector3d extra = caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(-1);
            if (extra.lengthSquared() > 1) extra = extra.normalize();
            caster.setMotion(caster.getMotion().add(extra));
            caster.velocityChanged = true;
        }
    }
}
