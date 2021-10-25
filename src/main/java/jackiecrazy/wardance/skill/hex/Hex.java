package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.EffectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Hex extends Skill {
    static final AttributeModifier HEX = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1"), "hex", -2, AttributeModifier.Operation.ADDITION);
    private final Tag<String> tag = makeTag("melee", "noDamage", "boundCast", ProcPoints.afflict_tick, ProcPoints.change_parry_result, ProcPoints.recharge_time, "normalAttack", "chant", "countdown");
    private final Tag<String> thing = makeTag(SkillTags.offensive, SkillTags.magical);

    @SubscribeEvent
    public static void snakebite(LivingHealEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity entity = e.getEntityLiving();
        //snakebite nullifies healing
        if (Marks.getCap(entity).isMarked(WarSkills.SNAKEBITE.get()))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public static void blackmark(LivingDamageEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity target = e.getEntityLiving();
        //black mark stealing health/posture/spirit
        if (e.getAmount() > 0 && CombatUtils.isMeleeAttack(e.getSource()) && Marks.getCap(target).isMarked(WarSkills.BLACK_MARK.get())) {
            Entity source = e.getSource().getTrueSource();
            if (source instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) source;
                final ICombatCapability cap = CombatData.getCap(le);
                float posture = cap.getPosture();
                float spirit = cap.getSpirit();
                if (posture < spirit) {
                    CombatData.getCap(target).consumePosture(2);
                    cap.addPosture(1);
                } else {
                    CombatData.getCap(target).consumeSpirit(3);
                    cap.addSpirit(1.5f);
                }
                le.heal(e.getAmount() / 3);
            }

        }
    }

//    @SubscribeEvent
//    public static void petrify(CriticalHitEvent e) {
//        if (!e.getEntityLiving().isServerWorld()) return;
//        if (!(e.getTarget() instanceof LivingEntity)) return;
//        LivingEntity target = (LivingEntity) e.getTarget();
//        //crit explosion on petrified target
//        StatusEffects.getCap(target).getActiveStatus(WarSkills.PETRIFY.get()).ifPresent((sd) -> {
//            if (sd.isCondition() && (e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical()))) {
//                FakeExplosion.explode(e.getEntityLiving().world, e.getPlayer(), target.getPosX(), target.getPosY(), target.getPosZ(), target.getWidth() * target.getHeight() * 3, new CombatDamageSource("explosion.player", e.getPlayer()).setArmorReductionPercentage(2).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(false).setProcNormalEffects(false).setProcAttackEffects(false).setExplosion(), target.getTotalArmorValue());
//                StatusEffects.getCap(target).removeStatus(WarSkills.PETRIFY.get());
//            }
//        });
//
//    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return thing;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 4;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 60);
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        return true;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Hex.class ? null : WarSkills.HEX.get();
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 300);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && (!((ParryEvent) procPoint).canParry() || getTags(caster).contains(ProcPoints.unblockable))) {
            procPoint.setCanceled(true);
            mark(caster, target, 200);
            markUsed(caster);
        }
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        final ModifiableAttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null && getParentSkill() == null) {
            luck.removeModifier(HEX);
            luck.applyNonPersistentModifier(HEX);
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) {
            luck.removeModifier(HEX);
        }
        super.onMarkEnd(caster, target, sd);
    }

    public static class Snakebite extends Hex {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            EffectInstance poison = new EffectInstance(Effects.POISON, 200, 1);
            if (target.isPotionActive(Effects.POISON)) {
                poison = new EffectInstance(Effects.POISON, Math.max(target.getActivePotionEffect(Effects.POISON).getDuration(), 200), Math.max(target.getActivePotionEffect(Effects.POISON).getAmplifier(), 1));
            }
            poison.setCurativeItems(Collections.emptyList());
            target.removePotionEffect(Effects.POISON);
            target.addPotionEffect(poison);
            return super.onMarked(caster, target, sd, existing);
        }

        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            //heal block, removed with poison
            if (!target.isPotionActive(Effects.POISON)) {
                removeMark(target);
                return true;
            }
            return false;
        }
    }

    public static class BlackMark extends Hex {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public static class Unravel extends Hex {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("melee", "noDamage", "boundCast", ProcPoints.change_parry_result, ProcPoints.recharge_time, ProcPoints.unblockable, "normalAttack", "countdown")));

        @Override
        public Tag<String> getProcPoints(LivingEntity caster) {
            return tag;
        }

        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        protected void mark(LivingEntity caster, LivingEntity target, float duration) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            final Collection<EffectInstance> potions = new ArrayList<>(target.getActivePotionEffects());
            target.curePotionEffects(milk);
            float size = 0, damage = 0;
            int effectCount = 1;
            for (EffectInstance ei : potions) {
                effectCount++;
                EffectInstance drop = new EffectInstance(ei.getPotion(), 0, -2);
                drop = EffectUtils.stackPot(caster, drop, EffectUtils.StackingMethod.MAXDURATION);
                if (drop.getAmplifier() >= 0) {
                    if (target.addPotionEffect(drop)) {//potion was wiped by milk
                        size += ei.getDuration();
                        damage += ei.getAmplifier() + 1;
                    } else {//mob is immune (!?!?) or potion was not wiped
                        size += ei.getDuration();
                        damage += (ei.getAmplifier() + 1) / 2f;
                    }
                } else {
                    size += drop.getDuration();
                    damage++;
                }
            }
            size = Math.min(size / (100 * effectCount), 10);
            FakeExplosion.explode(caster.world, caster, target.getPosX(), target.getPosY() + target.getHeight() * 1.1f, target.getPosZ(), size, DamageSource.causeExplosionDamage(target), damage * 4);
        }
    }
}