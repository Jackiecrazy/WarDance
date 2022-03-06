package jackiecrazy.wardance.skill.crownchampion;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.event.GainMightEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
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
    private static final UUID MULT = UUID.fromString("abb2e130-36af-4fbb-bf66-0f4be905dc24");
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", ProcPoints.change_might)));

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hurt(LivingDamageEvent e) {
        Entity seme = e.getSource().getTrueSource();
        LivingEntity uke = e.getEntityLiving();
        if (seme instanceof LivingEntity) {
            final Skill venge = WarSkills.VENGEFUL_MIGHT.get();
            for (PlayerEntity p : uke.world.getPlayers())
                if (TargetingUtils.isAlly(p, uke) && p.getDistanceSq(uke) < 100 && CasterData.getCap(p).getEquippedSkills().contains(venge)) {
                    ((LivingEntity) seme).addPotionEffect(new EffectInstance(Effects.GLOWING, 100));
                    SkillData apply = Marks.getCap((LivingEntity) seme).getActiveMark(venge).orElse(new SkillData(venge, 0));
                    apply.setArbitraryFloat(apply.getArbitraryFloat() + e.getAmount());
                    Marks.getCap((LivingEntity) seme).mark(apply);
                }
            if (Marks.getCap(uke).isMarked(venge) && CasterData.getCap((LivingEntity) seme).getEquippedSkills().contains(venge)) {
                Marks.getCap(uke).getActiveMark(venge).ifPresent(a -> {
                    final float amnt = Math.min(e.getAmount(), a.getArbitraryFloat());
                    CombatData.getCap((LivingEntity) seme).addMight(amnt);
                    e.setAmount(e.getAmount() + amnt);
                    a.setArbitraryFloat(a.getArbitraryFloat() - e.getAmount());
                    if (a.getArbitraryFloat() < 0) a.setDuration(-10);
                });
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void oops(LivingDamageEvent e) {
        LivingEntity uke = e.getEntityLiving();
        if (CasterData.getCap(uke).getEquippedSkills().contains(WarSkills.PRIDEFUL_MIGHT.get())) {
            CombatData.getCap(uke).setMight(0);
        }
    }

    @SubscribeEvent
    public static void sneaky(LivingEvent.LivingVisibilityEvent e) {
        if (CasterData.getCap(e.getEntityLiving()).getEquippedSkills().contains(WarSkills.HIDDEN_MIGHT.get()))
            e.modifyVisibility(0.7 + CombatData.getCap(e.getEntityLiving()).getMight() * 0.03);
    }

    @SubscribeEvent
    public static void deady(LivingDeathEvent e) {
        if (!CombatUtils.isPhysicalAttack(e.getSource())) {
            for (PlayerEntity pe : e.getEntityLiving().world.getLoadedEntitiesWithinAABB(PlayerEntity.class, e.getEntity().getBoundingBox().grow(5))) {
                if (CasterData.getCap(pe).getEquippedSkills().contains(WarSkills.ELEMENTAL_MIGHT.get())) {
                    CombatData.getCap(pe).addMight(1);
                }
            }
        }
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.prowess;
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
    public void onEquip(LivingEntity caster) {
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MULT);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MULT);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        int might = (int) CombatData.getCap(caster).getMight() - 1;
        if (procPoint instanceof LivingAttackEvent && procPoint.getPhase() == EventPriority.LOWEST) {
            SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, MULT, 0.05f * might, AttributeModifier.Operation.MULTIPLY_BASE);
        }
        if (procPoint instanceof GainMightEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            ((GainMightEvent) procPoint).setQuantity(((GainMightEvent) procPoint).getQuantity() * (15 - might / 2f) / 10f);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        //pure passive, no state changes possible.
        prev.setState(STATE.INACTIVE);
        return false;
    }

    public static class HiddenMight extends CrownChampion {

        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", ProcPoints.attack_might)));

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof AttackMightEvent && procPoint.getPhase() == EventPriority.HIGHEST && StealthUtils.getAwareness(caster, target).equals(StealthUtils.Awareness.UNAWARE))
                ((AttackMightEvent) procPoint).setQuantity(((AttackMightEvent) procPoint).getQuantity() + 0.5f);
        }

        @Override
        public boolean equippedTick(LivingEntity caster, SkillData stats) {
            int might = (int) CombatData.getCap(caster).getMight() - 1;
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, MULT, 0.03f * might, AttributeModifier.Operation.MULTIPLY_BASE);
            return super.equippedTick(caster, stats);
        }

        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public static class VengefulMight extends CrownChampion {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {

        }

        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            target.addPotionEffect(new EffectInstance(Effects.GLOWING, 10));
            if (sd.getDuration() < 0) {
                removeMark(target);
                return true;
            }
            return false;
        }
    }

    public static class PridefulMight extends CrownChampion {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData pd, LivingEntity target) {
            if (procPoint instanceof GainMightEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
                ((GainMightEvent) procPoint).setQuantity(((GainMightEvent) procPoint).getQuantity() * 3);
                pd.setArbitraryFloat(pd.getArbitraryFloat() + ((GainMightEvent) procPoint).getQuantity());
                if (pd.getArbitraryFloat() > 3) {
                    pd.setArbitraryFloat(pd.getArbitraryFloat() % 3);
                    CombatData.getCap(caster).setShatterCooldown(0);
                }
            }
        }
    }

    public static class ElementalMight extends CrownChampion {
        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof LivingAttackEvent)
                ((LivingAttackEvent) procPoint).getEntityLiving().addPotionEffect(new EffectInstance(WarEffects.VULNERABLE.get(), (int) (CombatData.getCap(caster).getMight() * 20)));
        }

        @Override
        public Color getColor() {
            return WarColors.VIOLET;
        }
    }


}
