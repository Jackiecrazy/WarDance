package jackiecrazy.wardance.skill.riposte;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Riposte extends Skill {
    /*
Riposte (passive): Parrying a foe marks them for 40 ticks. Hitting that target with any level of cooled down attack refreshes your hand you attacked with dealing the damage as if it was full and cools down defensive skills by +1. Can trigger and mark a foe every 60 ticks. (mostly for counter playing reeling).
Upgrades
Counter Kick: Riposte becomes a free kick, knocking the enemy back and dealing posture damage equivalent to the kick power at range 3.
Savage Counter: Ripostes become a free stagger heavy attack requiring one might, otherwise it just acts as normal.
Piercing Counter: Riposte attacks cannot be parried.
Career Ender: Riposte's strike acts as if the enemy is unaware if it is not parried and ignores the parry if the damage that will be dealt is more than their current health left...
Moment of Courage: If you parry an attack that should stagger you regardless of cooldown block the stagger and remain on nearly empty posture proccing riposte. If you manage to riposte in time gain back 50% posture and add your weapons damage to the posture damage of the attack. cooldown is tripled.
     */
    private static final UUID MULT = UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b1");
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", SkillTags.on_hurt, SkillTags.change_might)));
    private final Tag<String> no = Tag.getEmptyTag();

    @SubscribeEvent
    public static void hurt(LivingHurtEvent e) {
        Entity seme = e.getSource().getTrueSource();
        LivingEntity uke = e.getEntityLiving();
        if (CasterData.getCap(uke).isSkillActive(WarSkills.VENGEFUL_MIGHT.get())) {
            CombatData.getCap(uke).addMight(e.getAmount() / 10);
            if (seme instanceof LivingEntity)
                ((LivingEntity) seme).addPotionEffect(new EffectInstance(Effects.GLOWING, 100));
        }
        if (CasterData.getCap(uke).isSkillActive(WarSkills.PRIDEFUL_MIGHT.get())) {
            CombatData.getCap(uke).setMight(0);
        }
    }

    @SubscribeEvent
    public static void sneaky(LivingEvent.LivingVisibilityEvent e) {
        if (CasterData.getCap(e.getEntityLiving()).isSkillActive(WarSkills.HIDDEN_MIGHT.get()))
            e.modifyVisibility(0.7 + CombatData.getCap(e.getEntityLiving()).getMight() * 0.03);
    }

    @SubscribeEvent
    public static void deady(LivingDeathEvent e) {
        if (!CombatUtils.isPhysicalAttack(e.getSource())) {
            for (PlayerEntity pe : e.getEntityLiving().world.getEntitiesWithinAABB(PlayerEntity.class, e.getEntity().getBoundingBox().grow(5))) {
                if (CasterData.getCap(pe).isSkillActive(WarSkills.ELEMENTAL_MIGHT.get())) {
                    CombatData.getCap(pe).addMight(1);
                }
            }
        }
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Riposte.class ? null : WarSkills.CROWN_CHAMPION.get();
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
        int might = (int) CombatData.getCap(caster).getMight() - 1;
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, 0.05f*might, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public static class HiddenMight extends Riposte {
        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            if (CombatUtils.getAwareness(caster, target).equals(CombatUtils.Awareness.UNAWARE))
                CombatData.getCap(caster).addMight(0.5f);
        }

        @Override
        public boolean activeTick(LivingEntity caster, SkillData d) {
            int might = (int) CombatData.getCap(caster).getMight() - 1;
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, MULT, 0.03f*might, AttributeModifier.Operation.MULTIPLY_BASE);
            return super.activeTick(caster, d);
        }

        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public static class VengefulMight extends Riposte {
        @Override
        public Color getColor() {
            return Color.RED;
        }
    }

    public static class PridefulMight extends Riposte {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData pd, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, pd, target, procPoint);
            if (procPoint instanceof AttackMightEvent) {
                ((AttackMightEvent) procPoint).setQuantity(((AttackMightEvent) procPoint).getQuantity() * 3);
                pd.setArbitraryFloat(pd.getArbitraryFloat() + ((AttackMightEvent) procPoint).getQuantity());
                if (pd.getArbitraryFloat() > 3) {
                    pd.setArbitraryFloat(pd.getArbitraryFloat() % 3);
                    CombatData.getCap(caster).setShatterCooldown(0);
                }
            }
        }
    }

    public static class ElementalMight extends Riposte {
        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            target.addPotionEffect(new EffectInstance(WarEffects.VULNERABLE.get(), (int) (CombatData.getCap(caster).getMight() * 20)));
        }

        @Override
        public Color getColor() {
            return Color.CYAN;
        }
    }


}
