package jackiecrazy.wardance.skill.projectile;

import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Ripper extends Skill {
    //pounce, teleport slash, countershot, wind shot, weapon throw, ripper, shell shock, bomb arrow and datura tip
    //locust stones, pocket sand, bone toss, boulder dash, scavenger?

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof LivingDamageEvent lhe && lhe.getEntity() == target && lhe.getPhase() == EventPriority.LOWEST && state != STATE.ACTIVE) {
            if (DamageUtils.isMeleeAttack(lhe.getSource()) && isMarked(target)) {
                //rip out 2 arrows
                SkillData sd = getExistingMark(target);
                if (sd.getDuration() >= 1) {
                    int rip = (int) Math.min(sd.getDuration(), 2);
                    sd.decrementDuration(2);
                    lhe.setAmount(lhe.getAmount() + rip * 2);
                }
            }
        }
        if (procPoint instanceof LivingHurtEvent lhe && lhe.getEntity() == target && lhe.getPhase() == EventPriority.LOWEST && state != STATE.ACTIVE) {
            if (lhe.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                int extra = (int) (lhe.getAmount() / (10 / SkillUtils.getSkillEffectiveness(caster)));
                if (extra > 0)
                    mark(caster, target, extra);
            }
        }
        if (procPoint instanceof ProjectileImpactEvent pie && procPoint.getPhase() == EventPriority.LOWEST && target!=null && pie.getProjectile().getOwner() == caster) {
            mark(caster, target, 1);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (prev.getState() != STATE.INACTIVE)
            prev.setState(STATE.INACTIVE);
        return false;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }
}
