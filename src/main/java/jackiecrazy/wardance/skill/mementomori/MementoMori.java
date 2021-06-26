package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class MementoMori extends Skill {
    private static final UUID MULT = UUID.fromString("67ff7ef6-a398-4c65-9bb1-42edaa80e7b1");
    /*
    memento mori: your might generation speed and attack damage scales proportionally with lost health (including wounding)
rapid clotting: charm 3: gain 1 armor every point of health lost
panic: when taking damage under 50%, consume 3 spirit to create a smoke cloud that blinds the enemy, with radius increasing as health decreases
death denial: upon receiving fatal damage, become immune to all damage and all healing for 5 seconds, recharge on sleep
saving throw: your luck scales very strongly with lost health
pound of flesh: active skill. Consumes all your spirit, and until your spirit regenerates or (spirit) seconds have elapsed take 5% max health damage per attack to deal 10% max posture damage if parried, or 5% max health damage if connected
     */
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", SkillTags.recharge_sleep, SkillTags.on_being_damaged, SkillTags.change_might)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == MementoMori.class ? null : WarSkills.MEMENTO_MORI.get();
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 0);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        activate(caster, 0);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        //on attack might event, check health percentage and increment
        if (procPoint instanceof AttackMightEvent) {
            float healthPercentage = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            ((AttackMightEvent) procPoint).setQuantity(((AttackMightEvent) procPoint).getQuantity() * (2 - healthPercentage));
        }
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        float health = 1 - (caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster));
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, health / 2, AttributeModifier.Operation.MULTIPLY_TOTAL, d);
        return super.activeTick(caster, d);
    }

    public static class RapidClotting extends MementoMori {
        @Override
        public Color getColor() {
            return Color.PINK;
        }

        @Override
        public boolean activeTick(LivingEntity caster, SkillData d) {
            //TODO reserve max spirit
            float lostHealth = GeneralUtils.getMaxHealthBeforeWounding(caster) - caster.getHealth();
            SkillUtils.modifyAttribute(caster, Attributes.ARMOR, MULT, lostHealth, AttributeModifier.Operation.ADDITION, d);
            return super.activeTick(caster, d);
        }
    }

    public static class SavingThrow extends MementoMori {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public boolean activeTick(LivingEntity caster, SkillData d) {
            float lostHealth = 1 - (caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster));
            SkillUtils.modifyAttribute(caster, Attributes.LUCK, MULT, lostHealth * 10, AttributeModifier.Operation.ADDITION, d);
            return super.activeTick(caster, d);
        }
    }

    public static class Panic extends MementoMori {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            final float ohno = caster.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(caster);
            if (procPoint instanceof LivingDamageEvent && ohno < 0.5) {
                float visibility = ohno * 10;
                SkillUtils.createCloud(caster.world, caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(), 13 - ohno, ParticleTypes.LARGE_SMOKE);
                for (LivingEntity e : caster.world.getEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(11))) {
                    if (e.getDistanceSq(caster) > visibility * visibility) {
                        e.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 100));
                    }
                }
            }
        }
    }

}
