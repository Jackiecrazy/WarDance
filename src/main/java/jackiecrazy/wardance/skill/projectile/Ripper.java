package jackiecrazy.wardance.skill.projectile;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Ripper extends CoupDeGrace {
    //pounce, teleport slash, countershot, weapon throw, ripper, shell shock, bomb arrow and datura tip
    //locust stones, pocket sand, bone toss, boulder dash, scavenger?

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof LivingHurtEvent lhe && lhe.getEntity() == target && state != STATE.ACTIVE) {
            if (DamageUtils.isMeleeAttack(lhe.getSource()) && isMarked(target)) {
                //rip out 2 arrows
                SkillData sd = getExistingMark(target);
                int rip = (int) Math.min(sd.getDuration(), 2);
                sd.decrementDuration(2);
                lhe.setAmount(lhe.getAmount() + rip * 3 * SkillUtils.getSkillEffectiveness(caster));
            }
            if (lhe.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                mark(caster, target, lhe.getAmount() / 10);
            }
        }
        if (procPoint instanceof ProjectileImpactEvent pie && procPoint.getPhase() == EventPriority.LOWEST && pie.getProjectile().getOwner() == caster) {
            mark(caster, target, 1);
        }
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return !willKillOnCast(null, target, mark);
    }

    @Override
    protected float getDamage(LivingEntity caster, LivingEntity target, @javax.annotation.Nullable SkillData stats) {
        return (target.getMaxHealth() - target.getHealth()) * 0.05f * getExistingMark(target).getDuration() * (stats == null ? 1 : stats.getEffectiveness());
    }
}
