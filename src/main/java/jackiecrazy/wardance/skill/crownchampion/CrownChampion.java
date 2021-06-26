package jackiecrazy.wardance.skill.crownchampion;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
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
public class CrownChampion extends Skill {
    /*
vengeful might: 10% damage taken converted to might; highlight enemies that successfully damage you, and deal more damage to marked targets
hidden might: +1 might on successful unaware stab; -30% detection distance at first, lose 3% per level of might and gain 3% speed
prideful might: triple might gain, but clear everything on taking damage; shatter instantly cools down for every 3 might gained
elemental might: +1 burn/snowball/poison/drown damage to targets you have attacked; +1 might for every mob that dies to environmental damage around you
     */
    private static final UUID MULT = UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b1");
    private static final AttributeModifier[] list = {
            new AttributeModifier(MULT, "might multiplier", 0.05, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.10, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.20, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.25, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.30, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.35, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.40, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.45, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeModifier(MULT, "might multiplier", 0.50, AttributeModifier.Operation.MULTIPLY_BASE)
    };
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
        return this.getClass() == CrownChampion.class ? null : WarSkills.CROWN_CHAMPION.get();
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
        caster.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MULT);
        if (might >= 0)
            caster.getAttribute(Attributes.ATTACK_DAMAGE).applyNonPersistentModifier(list[might]);
    }

    public static class HiddenMight extends CrownChampion {
        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            if (CombatUtils.getAwareness(caster, target).equals(CombatUtils.AWARENESS.UNAWARE))
                    CombatData.getCap(caster).addMight(1);
        }

        @Override
        public boolean equippedTick(LivingEntity caster, STATE state) {
            int might = (int) CombatData.getCap(caster).getMight() - 1;
            caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MULT);
            if (might >= 0)
                caster.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(list[might]);
            return super.equippedTick(caster, state);
        }

        @Override
        public Color getColor() {
            return Color.GRAY;
        }
    }

    public static class VengefulMight extends CrownChampion {
        @Override
        public Color getColor() {
            return Color.RED;
        }
    }

    public static class PridefulMight extends CrownChampion {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData pd, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, pd, target, procPoint);
            if(procPoint instanceof AttackMightEvent){
                ((AttackMightEvent) procPoint).setQuantity(((AttackMightEvent) procPoint).getQuantity()*3);
                pd.setArbitraryFloat(pd.getArbitraryFloat()+((AttackMightEvent) procPoint).getQuantity());
                if (pd.getArbitraryFloat() > 3) {
                    pd.setArbitraryFloat(pd.getArbitraryFloat() % 3);
                    CombatData.getCap(caster).setShatterCooldown(0);
                }
            }
        }
    }

    public static class ElementalMight extends CrownChampion {
        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            target.addPotionEffect(new EffectInstance(WarEffects.VULNERABLE.get(), (int) (CombatData.getCap(caster).getMight() * 20)));
        }

        @Override
        public Color getColor() {
            return Color.BLUE;
        }
    }


}
