package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, health * SkillUtils.getSkillEffectiveness(caster), AttributeModifier.Operation.MULTIPLY_TOTAL);
        return false;
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

    public static class RapidClotting extends MementoMori {

        @Override
        public boolean equippedTick(LivingEntity caster, SkillData d) {
            float lostHealth = GeneralUtils.getMaxHealthBeforeWounding(caster) - caster.getHealth();
            lostHealth /= GeneralUtils.getMaxHealthBeforeWounding(caster);
            SkillUtils.modifyAttribute(caster, Attributes.ARMOR, MULT, lostHealth * SkillUtils.getSkillEffectiveness(caster) * 20, AttributeModifier.Operation.ADDITION);
            return super.equippedTick(caster, d);
        }
    }

    public static class StaticDischarge extends MementoMori {
        //If you have taken over 1 point of damage, create a shockwave that deals electric damage and majorly knocks back nearby entities
        //You can accumulate up to (max health/5) charge.

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof final LivingHurtEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() == caster) {
                float stat = stats.getArbitraryFloat();
                stat += lhe.getAmount();
                stat = Math.min(stat, GeneralUtils.getMaxHealthBeforeWounding(caster));
                stats.setArbitraryFloat(stat);
            }
            if (procPoint instanceof LivingAttackEvent && procPoint.getPhase() == EventPriority.HIGHEST && DamageUtils.isMeleeAttack(((LivingAttackEvent) procPoint).getSource()) && ((LivingAttackEvent) procPoint).getEntity() == target) {
                float stat = stats.getArbitraryFloat();
                if (stat > 1) {
                    //EXPLOOOOSION
                    for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(5))) {
                        if (TargetingUtils.isHostile(e, caster)) {
                            e.hurt(new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(0).setAttackingHand(null).setSkillUsed(this).setMagic(), stat * SkillUtils.getSkillEffectiveness(caster));
                            CombatUtils.knockBack(e, caster, stat / 4, true, false);
                        }
                    }
                    stats.setArbitraryFloat(0);
                }
            }
        }
    }

    public static class HealShock extends MementoMori {
        //When there is a hostile within 6 blocks, healing is redirected to shock and knock them back.

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof LivingHealEvent ee && procPoint.getPhase() == EventPriority.HIGHEST) {
                for (LivingEntity e : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(6))) {
                    if (TargetingUtils.isHostile(e, caster)) {
                        ee.setCanceled(true);
                        e.hurt(new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(0).setAttackingHand(null).setSkillUsed(this).setMagic(), ee.getAmount() * SkillUtils.getSkillEffectiveness(caster));
                        CombatUtils.knockBack(e, caster, ee.getAmount() / 4, true, false);
                    }
                }
            }
        }
    }

}
