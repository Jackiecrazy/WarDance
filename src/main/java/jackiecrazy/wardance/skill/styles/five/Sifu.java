package jackiecrazy.wardance.skill.styles.five;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.ai.FearGoal;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.entity.ai.ExposeGoal;
import jackiecrazy.wardance.mixin.SifuDropsMixin;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;


public class Sifu extends ColorRestrictionStyle {
    public static final TagKey<EntityType<?>> EVIL = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(WarDance.MODID, "great_evil"));
    private static final AttributeModifier kbr = new AttributeModifier(UUID.fromString("abc24c38-73e3-4551-9df4-e06e117699c1"), "sifu target", 1, AttributeModifier.Operation.ADDITION);

    public Sifu() {
        super(10, true, SkillColors.purple);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.COOLING) {
            CombatData.getCap(caster).setForcedSweep(0);
            return cooldownTick(stats);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.isCondition() && target instanceof Mob && !CombatData.getCap(target).isVulnerable()) {
            if (sd.getArbitraryFloat() == 0) {
                ((SifuDropsMixin) target).callDropAllDeathLoot(target.getLastDamageSource());
                sd.addArbitraryFloat(1);
            }
            if (caster != null) {
                GoalCapabilityProvider.getCap(target).ifPresent((a) -> a.setFearSource(caster));
                target.move(MoverType.SELF, target.position().subtract(caster.position()).normalize().scale(0.01));
                if (target.distanceToSqr(caster) > 256 || !GeneralUtils.isFacingEntity(caster, target, 180))
                    target.remove(Entity.RemovalReason.KILLED);
            } else
                target.remove(Entity.RemovalReason.KILLED);
            return false;
        }
        return markTickDown(sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        //applies to you
        if (procPoint instanceof LivingHurtEvent e && DamageUtils.isPhysicalAttack(e.getSource()) && e.getEntity() == caster && e.getPhase() == EventPriority.HIGHEST) {
            //ouch
            if (caster.getAttributeValue(FootworkAttributes.STEALTH.get()) < 0)
                e.setAmount(e.getAmount() * (float) (1 - (caster.getAttributeValue(FootworkAttributes.STEALTH.get()) * 0.05)));
        }
        if (procPoint.getPhase() == EventPriority.LOWEST) {
            //frugality
            if (procPoint instanceof LootingLevelEvent e) {
                e.setLootingLevel(0);
            }
            //exp
            if (procPoint instanceof LivingExperienceDropEvent e) {
                e.setDroppedExperience(e.getDroppedExperience() * 3);
            }
        }
        //great evils
        if (target == null || isGreatEvil(target)) return;
        //applies to eligible enemies
        if (procPoint instanceof LivingAttackEvent lae && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            if (!CombatData.getCap(target).isVulnerable()) {
                //cannot attack the weak
                lae.setCanceled(true);
            }
            if (!isMarked(target) && cast(caster)) {
                mark(caster, target, 4f, 0);
            }
        } else if (procPoint instanceof LivingHurtEvent lae && procPoint.getPhase() == EventPriority.HIGHEST && isMarked(target) && lae.getEntity() == target) {
            lae.setAmount(lae.getAmount() / Math.max(getExistingMark(target).getDuration(), 1));
        } else if (procPoint instanceof StunEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && sce.getEntity() != caster) {
            //upgrade to knockdown
            sce.setKnockdown(true);
            CombatUtils.knockBack(target, caster, 1f, true, true);
        } else if (procPoint instanceof LivingDeathEvent e && e.getPhase() == EventPriority.LOWEST && e.getEntity() != caster) {
            //run away!
            target.setHealth(1);
            e.setCanceled(true);
            if (target instanceof Mob mob) {
                for (WrappedGoal wg : new HashSet<>(mob.goalSelector.getAvailableGoals())) {
                    if (!(wg.getGoal() instanceof FearGoal) && !(wg.getGoal() instanceof ExposeGoal))
                        mob.goalSelector.removeGoal(wg.getGoal());
                }
                for (WrappedGoal wg : new HashSet<>(mob.targetSelector.getAvailableGoals())) {
                    if (!(wg.getGoal() instanceof FearGoal) && !(wg.getGoal() instanceof ExposeGoal))
                        mob.targetSelector.removeGoal(wg.getGoal());
                }
                if (target instanceof PathfinderMob p)
                    mob.goalSelector.addGoal(0, new FearGoal(p));
            }
            mark(caster, target, 100, 0, true);
            EffectUtils.causeFear(target, caster, 2000);
            CombatData.getCap(target).knockdown(120);
        }
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (sd.isCondition()) return sd;
        if (existing != null) return existing;
        for (LivingEntity entity : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBoxForCulling().inflate(7), a -> !TargetingUtils.isAlly(a, caster))) {
            if (entity != target && entity != caster) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                CombatUtils.knockBack(entity, caster, 0.5f, true, false);
            }
        }
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(kbr);
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(kbr);
        if (sd.isCondition() && target instanceof Mob) {
            target.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.ACTIVE && to == STATE.COOLING)
            setCooldown(caster, prev, 5);
        if (from == STATE.COOLING && to == STATE.INACTIVE)
            CombatData.getCap(caster).setForcedSweep(-1);
        return super.onStateChange(caster, prev, from, to);
    }

    private boolean isGreatEvil(LivingEntity target) {
        return target.getMaxHealth() > 100 || target instanceof Player;//target.getType().is(EVIL);
    }
}
