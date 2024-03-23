package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class MementoMori extends Skill {
    private static final UUID MULT = UUID.fromString("c2b95d0d-b52a-45c7-b2d6-4ea669aa848e");
    private static final UUID LUCK = UUID.fromString("c2b95d0d-b52a-45c7-b2d6-4ea660aa848e");
    /*
    memento mori: your might generation speed and attack damage scales proportionally with lost health (including wounding)
rapid clotting: charm 3: gain 1 armor every point of health lost
panic: when taking damage under 50%, consume 3 spirit to create a smoke cloud that blinds the enemy, with radius increasing as health decreases
death denial: upon receiving fatal damage, become immune to all damage and all healing for 5 seconds, recharge on sleep
saving throw: your luck scales very strongly with lost health
pound of flesh: active skill. Consumes all your spirit, and until your spirit regenerates or (spirit) seconds have elapsed take 5% max health damage per attack to deal 10% max posture damage if parried, or 5% max health damage if connected
     */
    private final HashSet<String> tag = makeTag("passive", ProcPoints.recharge_sleep, ProcPoints.change_heals, ProcPoints.on_being_damaged, ProcPoints.attack_might);
    private final HashSet<String> no = none;

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.memento_mori;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData d) {
        if (getClass() != MementoMori.class) return false;
        float health = 1 - (caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster));
        final float amount = health * SkillUtils.getSkillEffectiveness(caster);
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        float afore = d.getArbitraryFloat();
        d.setArbitraryFloat(amount);
        return afore != d.getArbitraryFloat();
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MULT);
        caster.getAttribute(Attributes.ARMOR).removeModifier(MULT);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return caster.getHealth() != caster.getMaxHealth();
    }

    public static class RapidClotting extends MementoMori {

        @Override
        public boolean equippedTick(LivingEntity caster, SkillData d) {
            float lostHealth = GeneralUtils.getMaxHealthBeforeWounding(caster) - caster.getHealth();
            lostHealth /= GeneralUtils.getMaxHealthBeforeWounding(caster);
            final float armor = lostHealth * SkillUtils.getSkillEffectiveness(caster) * 20;
            SkillUtils.modifyAttribute(caster, Attributes.ARMOR, MULT, armor, AttributeModifier.Operation.ADDITION);
            float afore = d.getArbitraryFloat();
            d.setArbitraryFloat(armor);
            return afore != d.getArbitraryFloat();
        }
    }

    public static class StaticDischarge extends MementoMori {
        //If you have taken over 1 point of damage, create a shockwave that deals electric damage and majorly knocks back nearby entities
        //You can accumulate up to (max health/5) charge.

        @Override
        public HashSet<String> getTags() {
            return none;
        }

        @Override
        public boolean equippedTick(LivingEntity caster, SkillData d) {
            if (d.getState() == STATE.ACTIVE) {
                if (CombatData.getCap(caster).getSpirit() == CombatData.getCap(caster).getMaxSpirit())
                    markUsed(caster);
                return activeTick(d);
            }
            return super.equippedTick(caster, d);
        }

        @Override
        public boolean displaysInactive(LivingEntity caster, SkillData stats) {
            return true;
        }

        @Override
        public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
            if(sd.getArbitraryFloat()<1)return CastStatus.OTHER;
            return super.castingCheck(caster, sd);
        }

        @Override
        public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
            if (to == STATE.COOLING) {
                prev.setState(STATE.INACTIVE);
                prev.setDuration(0);
            }
            if (to == STATE.HOLSTERED) {
                float stat = prev.getArbitraryFloat();
                if (stat > 1 && cast(caster)) {
                    caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                    //EXPLOOOOSION
                    if (stat >= caster.getMaxHealth())
                        completeChallenge(caster);
                    for (LivingEntity e : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(5*prev.getEffectiveness()))) {
                        if (TargetingUtils.isHostile(e, caster)) {
                            e.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(stat/4).setAttackingHand(null).setSkillUsed(this).flag(DamageTypeTags.IS_LIGHTNING), stat *prev.getEffectiveness());
                        }
                    }
                    prev.setArbitraryFloat(0);
                }
            }
            return instantCast(prev, from, to);
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof final LivingHealEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() == caster) {
                lhe.setAmount(Math.min(lhe.getAmount(), caster.getMaxHealth() - caster.getHealth()));
                float stat = stats.getArbitraryFloat();
                stat += lhe.getAmount();
                stat = Math.min(stat, GeneralUtils.getMaxHealthBeforeWounding(caster));
                stats.setArbitraryFloat(stat);
                stats.markDirty();
            }
        }
    }

    public static class HealShock extends MementoMori {
        //When there is a hostile within 6 blocks, healing is redirected to shock and knock them back.

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof LivingHealEvent ee && procPoint.getPhase() == EventPriority.LOWEST) {
                ee.setAmount(Math.min(ee.getAmount(), caster.getMaxHealth() - caster.getHealth()));
                for (LivingEntity e : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(6))) {
                    if (TargetingUtils.isHostile(e, caster) && !target.hasEffect(FootworkEffects.VULNERABLE.get())) {
                        ee.setCanceled(true);
                        e.addEffect(EffectUtils.stackPot(e, new MobEffectInstance(FootworkEffects.VULNERABLE.get(), (int) (5 * ee.getAmount() * SkillUtils.getSkillEffectiveness(caster)), 0), EffectUtils.StackingMethod.MAXDURATION));
                        //e.hurt(new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(0).setAttackingHand(null).setSkillUsed(this).setMagic(), ee.getAmount() * SkillUtils.getSkillEffectiveness(caster));
                        return;
                    }
                }
            }
        }

        @Override
        public boolean displaysInactive(LivingEntity caster, SkillData stats) {
            return stats.isCondition();
        }
    }

}
