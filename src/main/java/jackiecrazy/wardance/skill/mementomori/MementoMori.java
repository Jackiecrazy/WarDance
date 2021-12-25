package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
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

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == MementoMori.class ? null : WarSkills.MEMENTO_MORI.get();
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return empty;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 0);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        if (!activate(caster, 0)) {
            caster.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MULT);
            caster.getAttribute(Attributes.ARMOR).removeModifier(MULT);
            caster.getAttribute(Attributes.LUCK).removeModifier(MULT);
        }
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        //on attack might event, check health percentage and increment
        if (procPoint instanceof AttackMightEvent && getParentSkill() == null) {
            float healthPercentage = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            ((AttackMightEvent) procPoint).setQuantity(((AttackMightEvent) procPoint).getQuantity() * (3 - 2 * healthPercentage));
        }
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (getClass() != MementoMori.class) return false;
        float health = 1 - (caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster));
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, health, AttributeModifier.Operation.MULTIPLY_TOTAL);
        return super.activeTick(caster, d);
    }

    public static class RapidClotting extends MementoMori {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public boolean activeTick(LivingEntity caster, SkillData d) {
            //TODO reserve max spirit
            float lostHealth = GeneralUtils.getMaxHealthBeforeWounding(caster) - caster.getHealth();
            lostHealth /= GeneralUtils.getMaxHealthBeforeWounding(caster);
            SkillUtils.modifyAttribute(caster, Attributes.ARMOR, MULT, lostHealth * 20, AttributeModifier.Operation.ADDITION);
            return super.activeTick(caster, d);
        }
    }

    public static class HealShock extends MementoMori {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            final float lostHealthPerc = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            if (procPoint instanceof LivingHealEvent && caster.getHealth() != caster.getMaxHealth() && ((LivingHealEvent) procPoint).getAmount() > 0) {
                int hitSize = 1 + (int) (lostHealthPerc * 10);
                final float amnt = ((LivingHealEvent) procPoint).getAmount();
                for (LivingEntity e : caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(7))) {
                    if (TargetingUtils.isHostile(e, caster) && e.attackEntityFrom(new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setKnockbackPercentage(0).setAttackingHand(null).setSkillUsed(this).setMagicDamage(), amnt)) {
                        hitSize--;
                    }
                    if (hitSize < 0) break;
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
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            final float ohno = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            if (procPoint instanceof LivingDamageEvent && ohno < 1) {
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
