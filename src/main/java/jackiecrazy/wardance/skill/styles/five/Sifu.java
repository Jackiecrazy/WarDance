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
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.entity.ai.ExposeGoal;
import jackiecrazy.wardance.event.SweepEvent;
import jackiecrazy.wardance.mixin.SifuDropsMixin;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Sifu extends ColorRestrictionStyle {
    public static final TagKey<EntityType<?>> EVIL = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(WarDance.MODID, "great_evil"));
    private static final AttributeModifier kbr = new AttributeModifier(UUID.fromString("abc24c38-73e3-4551-9df4-e06e117699c1"), "sifu target", 1, AttributeModifier.Operation.ADDITION);

    public Sifu() {
        super(10, true, SkillColors.purple);
    }

    //redundancy, just in case
    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (Marks.getCap(e.getEntity()).isMarked(WarSkills.SIFU.get())) {
            e.setCanceled(true);
            fakeDie(e.getEntity(), e.getEntity().getCombatTracker().getKiller());
        }
    }

    private static void fakeDie(LivingEntity target, @Nullable LivingEntity caster) {
        target.setHealth(1);
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
        final SkillData sd = new SkillData(WarSkills.SIFU.get(), 100).flagCondition(true);
        if (caster != null) {
            sd.setCaster(caster);
            EffectUtils.causeFear(target, caster, 2000);
        }
        Marks.getCap(target).mark(sd);
        CombatData.getCap(target).knockdown(120);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.isCondition() && target instanceof Mob && !CombatData.getCap(target).isVulnerable()) {
            if (caster != null) {
                GoalCapabilityProvider.getCap(target).ifPresent((a) -> a.setFearSource(caster));
                target.move(MoverType.SELF, target.position().subtract(caster.position()).normalize().scale(0.01));
                if (target.distanceToSqr(caster) > 256 || !GeneralUtils.isFacingEntity(caster, target, 180))
                    removeMark(target);
            } else
                removeMark(target);
            return false;
        }
        return markTickDown(sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        //no sweeping
        if (procPoint instanceof SweepEvent e && procPoint.getPhase() == EventPriority.HIGHEST && stats.getState() == STATE.COOLING) {
            e.setCanceled(true);
        }
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
            if (CombatData.getCap(target).isVulnerable()) {
                //cannot attack the weak
                lae.setCanceled(true);
            }
            if (!isMarked(target) && cast(caster)) {
                mark(caster, target, 4f, 0);
            }
        } else if (procPoint instanceof LivingHurtEvent lae && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            if (isMarked(target))
                lae.setAmount(lae.getAmount() * Math.max(5 - getExistingMark(target).getDuration(), 1) * 0.2f);
            else
                lae.setAmount(lae.getAmount() / 5);
        } else if (procPoint instanceof StunEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && sce.getEntity() != caster) {
            //upgrade to knockdown
            sce.setKnockdown(true);
            CombatUtils.knockBack(target, caster, 1f, true, true);
        } else if (procPoint instanceof LivingDeathEvent e && e.getPhase() == EventPriority.LOWEST && e.getEntity() != caster) {
            //run away!
            e.setCanceled(true);
            fakeDie(target, caster);
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
        SkillUtils.addAttribute(target, Attributes.KNOCKBACK_RESISTANCE, kbr);
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        SkillUtils.removeAttribute(target, Attributes.KNOCKBACK_RESISTANCE, kbr);
        if (sd.isCondition() && target instanceof Mob) {
            ((SifuDropsMixin) target).callDropAllDeathLoot(target.getLastDamageSource());
            target.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.ACTIVE && to == STATE.COOLING)
            setCooldown(caster, prev, 5);
        return super.onStateChange(caster, prev, from, to);
    }

    private boolean isGreatEvil(LivingEntity target) {
        return target.getMaxHealth() > 100 || target instanceof Player;//target.getType().is(EVIL);
    }
}
