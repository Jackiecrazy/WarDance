package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
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
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", ProcPoints.recharge_sleep, ProcPoints.change_heals, ProcPoints.on_being_damaged, ProcPoints.attack_might)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.memento_mori;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MULT);
        caster.getAttribute(Attributes.ARMOR).removeModifier(MULT);
        caster.getAttribute(Attributes.LUCK).removeModifier(MULT);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        //on attack might event, check health percentage and increment
        if (procPoint instanceof AttackMightEvent && this == WarSkills.BLOODLUST.get()) {
            float healthPercentage = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            ((AttackMightEvent) procPoint).setQuantity(((AttackMightEvent) procPoint).getQuantity() * (3 - 2 * healthPercentage));
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData d) {
        if (getClass() != MementoMori.class) return false;
        float health = 1 - (caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster));
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, health, AttributeModifier.Operation.MULTIPLY_TOTAL);
        return false;
    }

    public static class RapidClotting extends MementoMori {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public boolean equippedTick(LivingEntity caster, SkillData d) {
            //TODO reserve max spirit
            float lostHealth = GeneralUtils.getMaxHealthBeforeWounding(caster) - caster.getHealth();
            lostHealth /= GeneralUtils.getMaxHealthBeforeWounding(caster);
            SkillUtils.modifyAttribute(caster, Attributes.ARMOR, MULT, lostHealth * 20, AttributeModifier.Operation.ADDITION);
            return super.equippedTick(caster, d);
        }
    }

    public static class StaticDischarge extends MementoMori {
        //If you have taken over 1 point of damage, create a shockwave that deals electric damage and majorly knocks back nearby entities
        //You can accumulate up to (max health/5) charge.

        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof LivingDamageEvent&&procPoint.getPhase()== EventPriority.HIGHEST && ((LivingDamageEvent) procPoint).getEntityLiving() == caster) {
                float stat = stats.getArbitraryFloat();
                stat += ((LivingDamageEvent) procPoint).getAmount();
                stat = Math.min(stat, GeneralUtils.getMaxHealthBeforeWounding(caster) / 5);
                stats.setArbitraryFloat(stat);
            }
            if (procPoint instanceof LivingAttackEvent&&procPoint.getPhase()== EventPriority.HIGHEST && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
                float stat = stats.getArbitraryFloat();
                if (stat > 1) {
                    //EXPLOOOOSION
                    for (LivingEntity e : caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(5))) {
                        if (TargetingUtils.isHostile(e, caster)) {
                            e.attackEntityFrom(new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(0).setAttackingHand(null).setSkillUsed(this).setMagicDamage(), stat);
                            CombatUtils.knockBack(e, caster, stat / 4, true, false);
                        }
                    }
                    stats.setArbitraryFloat(0);
                }
            }
        }
    }

    public static class Panic extends MementoMori {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            final float ohno = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            if (procPoint instanceof LivingDamageEvent&&procPoint.getPhase()== EventPriority.HIGHEST && ohno < 1) {
                float visibility = ohno * 10;
                SkillUtils.createCloud(caster.world, caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(), visibility, ParticleTypes.LARGE_SMOKE);
                for (LivingEntity e : caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(40), (a) -> TargetingUtils.isHostile(a, caster))) {
                    if (e.getDistanceSq(caster) > visibility * visibility) {
                        e.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 100));
                    }
                }
            }
        }
    }

}
