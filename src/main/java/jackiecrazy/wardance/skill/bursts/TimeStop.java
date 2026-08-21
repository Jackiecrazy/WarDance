package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.event.GainAdrenalineEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class TimeStop extends Skill {
    @SubscribeEvent
    public static void nonstop(LivingDamageEvent e) {
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.TIME_STOP.get()).ifPresent((a) -> e.getEntity().hurtTime = e.getEntity().invulnerableTime = e.getEntity().hurtDuration = 0);
    }

    /*
    stop time for 9 seconds.
    During this time, all throws reset cooldown.
    Every second new mobs are frozen
     */

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (!StylishData.getCap(caster).maxAdrenaline()) return CastStatus.ADRENALINE;
        return super.castingCheck(caster, sd);
    }

    @Override
    public HashSet<String> getTags() {
        return burst;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return burst;
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        return markTickDown(sd);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (!StylishData.getCap(caster).drainAdrenaline(1 / (180f * stats.getEffectiveness()))) {
                markUsed(caster);
                CombatData.getCap(caster).setSpirit(0);
                return true;
            }
            CombatData.getCap(caster).setSpirit(100);
            if (caster.tickCount % 4 == 0) {
                float predictedTimeLeft = StylishData.getCap(caster).getAdrenaline() * 180;
                freeze(caster, (int) predictedTimeLeft);
            }
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {

        if (state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (procPoint instanceof LivingEntityUseItemEvent.Tick e) {
                e.setDuration(e.getDuration() - 100);
            } else if (procPoint instanceof PlayInteractionEvent.Post e)
                e.setCooldown(e.getOriginalState() == WeaponStats.AttackType.THROW ? 1 : 0.5);
            if (procPoint instanceof GainAdrenalineEvent gme) {
                gme.setQuantity(0);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 9)) {
            CasterData.getCap(caster).removeActiveTag(SkillTags.state);
            //TimeSlowData.getCap(caster).alterSpeed(180, 0);
            freeze(caster, 180);
            CombatData.getCap(caster).setIframe(180);
            return true;
        }
        if (from == STATE.ACTIVE && to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
        }
        return instantCast(prev, from, to);
    }

    private void freeze(LivingEntity caster, int dur) {
        for (Entity t : caster.level().getEntities(caster, caster.getBoundingBox().inflate(32))) {
            boolean freeze = true;
            if (t instanceof FlyingItemEntity f && f.getOwner() == caster && (!(f instanceof ThrownWeaponEntity) || t.distanceToSqr(caster) < 5))
                freeze = false;
            if (t instanceof Projectile f && f.getOwner() == caster && t.distanceToSqr(caster) < 5)
                freeze = false;
            if (freeze) {
                TimeSlowData.getCap(t).alterSpeed(dur, 0);
                if (t instanceof LivingEntity le)
                    mark(caster, le, 2);
                t.hurtMarked = true;
            }
        }
    }
}
