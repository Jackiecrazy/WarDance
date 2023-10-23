package jackiecrazy.wardance.skill.styles.five;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.ai.FearGoal;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
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
import net.minecraft.core.registries.Registries;
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
    public static final TagKey<EntityType<?>> EVIL = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "great_evil"));
    private static final AttributeModifier kbr = new AttributeModifier(UUID.fromString("abc24c38-73e3-4551-9df4-e06e117699c1"), "sifu target", 1, AttributeModifier.Operation.ADDITION);

    public Sifu() {
        super(10, true, SkillColors.purple, SkillColors.gold);
    }

    //redundancy, just in case
    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (Marks.getCap(e.getEntity()).isMarked(WarSkills.SIFU.get())) {
            e.setCanceled(true);
            fakeDie(e.getEntity(), e.getEntity().getKillCredit());
        }
    }

    private static void fakeDie(LivingEntity target, @Nullable LivingEntity caster) {
        SkillUtils.removeAttribute(target, Attributes.KNOCKBACK_RESISTANCE, kbr);
        target.setHealth(1);
        target.removeAllEffects();
        target.clearFire();
        if (target instanceof Mob mob) {
            for (WrappedGoal wg : new HashSet<>(mob.goalSelector.getAvailableGoals())) {
                if (!(wg.getGoal() instanceof FearGoal) && !(wg.getGoal() instanceof ExposeGoal))
                    mob.goalSelector.removeGoal(wg.getGoal());
            }
            for (WrappedGoal wg : new HashSet<>(mob.targetSelector.getAvailableGoals())) {
                if (!(wg.getGoal() instanceof FearGoal) && !(wg.getGoal() instanceof ExposeGoal))
                    mob.targetSelector.removeGoal(wg.getGoal());
            }
            mob.getBrain().removeAllBehaviors();
            if (target instanceof PathfinderMob p)
                mob.goalSelector.addGoal(0, new FearGoal(p));
        }
        final SkillData sd = new SkillData(WarSkills.SIFU.get(), 100).flagCondition(true);
        if (caster != null) {
            sd.setCaster(caster);
            EffectUtils.causeFear(target, caster, 2000);
        }
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 10));
        Marks.getCap(target).mark(sd);
        CombatData.getCap(target).knockdown(120);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return cooldownTick(stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.isCondition() && target instanceof Mob && !CombatData.getCap(target).isVulnerable()) {
            if (caster != null) {
                if (!target.hasEffect(FootworkEffects.FEAR.get()))
                    EffectUtils.causeFear(target, caster, 100);
                GoalCapabilityProvider.getCap(target).ifPresent((a) -> a.setFearSource(caster));
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
                e.setAmount(e.getAmount() * (float) (1 - (caster.getAttributeValue(FootworkAttributes.STEALTH.get()) * 0.05 * SkillUtils.getSkillEffectiveness(caster))));
        }
        if (procPoint.getPhase() == EventPriority.LOWEST) {
            //frugality
            if (procPoint instanceof LootingLevelEvent e) {
                e.setLootingLevel(0);
            }
            //exp
            if (procPoint instanceof LivingExperienceDropEvent e) {
                e.setDroppedExperience((int) (e.getDroppedExperience() * 3 * SkillUtils.getSkillEffectiveness(caster)));
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
            //less damage
            for (int ignore = 0; ignore < CasterData.getCap(caster).getEquippedColors().size(); ignore++) {
                lae.setAmount(lae.getAmount() * 0.85f * SkillUtils.getSkillEffectiveness(caster));
            }
            //%max health dealt immediately on expose
            if (CombatData.getCap(target).isExposed()) {
                lae.setAmount(lae.getAmount() + target.getMaxHealth() * 0.07f * SkillUtils.getSkillEffectiveness(caster));
                if (lae.getSource() instanceof CombatDamageSource cds)
                    cds.bypassArmor().bypassEnchantments().bypassMagic();
            }
        } else if (procPoint instanceof StunEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && sce.getEntity() != caster) {
            SkillUtils.removeAttribute(target, Attributes.KNOCKBACK_RESISTANCE, kbr);
            CombatUtils.knockBack(target, caster, 1f, false, true);
            //upgrade to knockdown
            sce.setKnockdown(true);
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
        for (LivingEntity entity : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBoxForCulling().inflate(7), a -> !TargetingUtils.isAlly(a, caster))) {
            if (entity != target && entity != caster) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (60 * SkillUtils.getSkillEffectiveness(caster)), 1));
                CombatUtils.knockBack(entity, caster, 0.6f * SkillUtils.getSkillEffectiveness(caster), true, false);
            }
        }
        SkillUtils.addAttribute(target, Attributes.KNOCKBACK_RESISTANCE, kbr);
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        SkillUtils.removeAttribute(target, Attributes.KNOCKBACK_RESISTANCE, kbr);
        if (sd.isCondition() && target instanceof Mob) {
            final SifuDropsMixin sifu = (SifuDropsMixin) target;
            if (caster instanceof Player p)
                target.setLastHurtByPlayer(p);
            sifu.callDropAllDeathLoot(new CombatDamageSource(caster));
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
