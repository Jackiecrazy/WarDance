package jackiecrazy.wardance.skill.styles.five;

import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.ai.FearGoal;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.entity.ai.ExposeGoal;
import jackiecrazy.wardance.mixin.SifuDropsMixin;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;


public class Sifu extends ColorRestrictionStyle {
    private static final AttributeModifier kbr = new AttributeModifier(UUID.fromString("abc24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1, AttributeModifier.Operation.ADDITION);

    public Sifu() {
        super(10, true, SkillColors.purple);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            return activeTick(stats);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.isCondition()) {
            GoalCapabilityProvider.getCap(target).ifPresent((a) -> a.setFearSource(caster));
            if(target.distanceToSqr(caster)>256)
                target.remove(Entity.RemovalReason.KILLED);
        }
        return markTickDown(sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            if (CombatData.getCap(target).isVulnerable() || (isMarked(target) && getExistingMark(target).isCondition())) {
                lae.setCanceled(true);
                return;
            }
            if (!isMarked(target))
                mark(caster, target, 5f, 0);

        } else if (procPoint instanceof StunEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && sce.getEntity() != caster) {
            sce.setKnockdown(true);
            CombatUtils.knockBack(target, caster, 1.2f, true, true);
        } else if (procPoint instanceof LivingDeathEvent e && e.getEntity() != caster) {
            target.setHealth(1);
            e.setCanceled(true);
            ((SifuDropsMixin) target).callDropAllDeathLoot(e.getSource());
            if (target instanceof Mob mob) {
                for (WrappedGoal wg : new HashSet<>(mob.goalSelector.getAvailableGoals())) {
                    if (!(wg.getGoal() instanceof FearGoal) && !(wg.getGoal() instanceof ExposeGoal))
                        mob.goalSelector.removeGoal(wg.getGoal());
                }
            }
            mark(caster, target, 100f, 0, true);
            CombatData.getCap(target).expose(120);
        }
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) return existing;
        for (LivingEntity entity : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBoxForCulling().inflate(7), a -> !TargetingUtils.isAlly(a, caster))) {
            if (entity != target && entity != caster) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60));
                CombatUtils.knockBack(entity, caster, 0.5f, true, false);
            }
        }
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(kbr);
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(kbr);
    }
}
