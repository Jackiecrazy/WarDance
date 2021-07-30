package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.status.StatusEffects;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.EffectUtils;
import jackiecrazy.wardance.utils.SkillUtils;
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
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Hex extends Skill {
    private static final AttributeModifier HEX = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1"), "hex", -2, AttributeModifier.Operation.ADDITION);
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("melee", "noDamage", "boundCast", SkillTags.afflict_tick, SkillTags.change_parry_result, SkillTags.recharge_time, "normalAttack", "countdown")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @SubscribeEvent
    public static void snakebite(LivingHealEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity entity = e.getEntityLiving();
        //snakebite nullifies healing
        if (StatusEffects.getCap(entity).isStatusActive(WarSkills.SNAKEBITE.get()))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public static void blackmark(LivingDamageEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity target = e.getEntityLiving();
        //black mark stealing health/posture/spirit
        if (e.getAmount() > 0 && CombatUtils.isMeleeAttack(e.getSource()) && StatusEffects.getCap(target).isStatusActive(WarSkills.BLACK_MARK.get())) {
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

    @SubscribeEvent
    public static void petrify(CriticalHitEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (!(e.getTarget() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) e.getTarget();
        //crit explosion on petrified target
        StatusEffects.getCap(target).getActiveStatus(WarSkills.PETRIFY.get()).ifPresent((sd) -> {
            if (sd.isCondition() && (e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical()))) {
                FakeExplosion.explode(e.getEntityLiving().world, e.getPlayer(), target.getPosX(), target.getPosY(), target.getPosZ(), target.getWidth()* target.getHeight()*3, new CombatDamageSource("explosion.player",e.getPlayer()).setArmorReductionPercentage(2).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(false).setProcNormalEffects(false).setProcAttackEffects(false).setExplosion(), target.getTotalArmorValue());
                StatusEffects.getCap(target).removeStatus(WarSkills.PETRIFY.get());
            }
        });

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
    public CastStatus castingCheck(LivingEntity caster) {
        CastStatus s = super.castingCheck(caster);
        if (s == CastStatus.ALLOWED && CombatData.getCap(caster).getSpirit() < 4) return CastStatus.OTHER;
        return s;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 60);
        CombatData.getCap(caster).consumeSpirit(4);
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
        if (procPoint instanceof ParryEvent && (!((ParryEvent) procPoint).canParry() || getTags(caster).contains(SkillTags.unblockable))) {
            procPoint.setCanceled(true);
            afflict(caster, target, 200);
            markUsed(caster);
        }
    }

    @Override
    public SkillData onStatusAdd(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        final ModifiableAttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) {
            luck.removeModifier(HEX);
            luck.applyNonPersistentModifier(HEX);
        }
        return super.onStatusAdd(caster, target, sd, existing);
    }

    @Override
    public void onStatusEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) {
            luck.removeModifier(HEX);
        }
        super.onStatusEnd(caster, target, sd);
    }

    public static class ItchyCurse extends Hex {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof ParryEvent && (!((ParryEvent) procPoint).canParry() || getTags(caster).contains(SkillTags.unblockable))) {
                procPoint.setCanceled(true);
                afflict(caster, target, 60);
                markUsed(caster);
            }
        }

        @Override
        public boolean statusTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            boolean stationary = target.lastTickPosY == target.getPosY() && target.lastTickPosZ == target.getPosZ() && target.lastTickPosX == target.getPosX();
            if (stationary) {
                sd.decrementDuration();
            }
            if (target.ticksExisted % 20 == 0)
                sd.setArbitraryFloat(sd.getArbitraryFloat() + 1);
            SkillUtils.modifyAttribute(target, Attributes.ARMOR, HEX.getID(), -sd.getArbitraryFloat() * 4, AttributeModifier.Operation.ADDITION);
            if (target.getTotalArmorValue() == 0 && sd.getArbitraryFloat() > 7) {
                CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 60);
                CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 60);
                sd.setDuration(60);
                SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX.getID(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            if (sd.getDuration() <= 0) {
                StatusEffects.getCap(target).removeStatus(this);
                return true;
            }
            return false;
        }

        @Override
        public void onStatusEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
            final ModifiableAttributeInstance armor = target.getAttribute(Attributes.ARMOR);
            if (armor != null) {
                armor.removeModifier(HEX.getID());
            }
            final ModifiableAttributeInstance speed = target.getAttribute(Attributes.ARMOR);
            if (speed != null) {
                speed.removeModifier(HEX.getID());
            }
            super.onStatusEnd(caster, target, sd);
        }
    }

    public static class Petrify extends Hex {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public boolean statusTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            final float weegee = sd.getArbitraryFloat();
            if (target.ticksExisted % 20 == 0) {
                sd.setArbitraryFloat(weegee + 1f);
            }
            SkillUtils.modifyAttribute(target, Attributes.ARMOR, HEX.getID(), weegee, AttributeModifier.Operation.ADDITION);
            SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, HEX.getID(), -weegee / 10, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (sd.getDuration() == 1 && !sd.isCondition()) {
                sd.setDuration(100);
                sd.flagCondition(true);
                EffectUtils.attemptAddPot(target, new EffectInstance(WarEffects.PETRIFY.get(), 100), false);
            }
            if (sd.getDuration() <= 0) {
                StatusEffects.getCap(target).removeStatus(this);
                return true;
            }
            sd.decrementDuration();
            return false;
        }

        @Override
        public void onStatusEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
            final ModifiableAttributeInstance armor = target.getAttribute(Attributes.ARMOR);
            if (armor != null) {
                armor.removeModifier(HEX.getID());
            }
            final ModifiableAttributeInstance speed = target.getAttribute(Attributes.ARMOR);
            if (speed != null) {
                speed.removeModifier(HEX.getID());
            }
            super.onStatusEnd(caster, target, sd);
        }
    }

    public static class Snakebite extends Hex {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public SkillData onStatusAdd(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            EffectInstance poison = new EffectInstance(Effects.POISON, 200, 1);
            if (target.isPotionActive(Effects.POISON)) {
                poison = new EffectInstance(Effects.POISON, Math.max(target.getActivePotionEffect(Effects.POISON).getDuration(), 200), Math.max(target.getActivePotionEffect(Effects.POISON).getAmplifier(), 1));
            }
            poison.setCurativeItems(Collections.emptyList());
            target.removePotionEffect(Effects.POISON);
            target.addPotionEffect(poison);
            sd.setArbitraryFloat(target.getAir());
            return super.onStatusAdd(caster, target, sd, existing);
        }

        @Override
        public boolean statusTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            //heal block, lose oxygen, gain wounding, removed with poison
            if (!target.isPotionActive(Effects.POISON)) {
                StatusEffects.getCap(target).removeStatus(this);
                return true;
            }
            sd.setArbitraryFloat(sd.getArbitraryFloat() - 1);
            target.setAir((int) sd.getArbitraryFloat());
            //manual drowning, bah
            if (sd.getArbitraryFloat() <= -20) {
                sd.setArbitraryFloat(0);
                target.attackEntityFrom(DamageSource.IN_WALL, 1);
            }
            if (target.getHealth() < target.getMaxHealth()) {
                float lostHealth = target.getMaxHealth() - target.getHealth();
                CombatData.getCap(target).addWounding(Math.max(0.05f, lostHealth * 0.0025f));
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
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("melee", "noDamage", "boundCast", SkillTags.recharge_time, SkillTags.unblockable, "normalAttack", "countdown")));

        @Override
        public Tag<String> getTags(LivingEntity caster) {
            return tag;
        }

        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        protected void afflict(LivingEntity caster, LivingEntity target, float duration) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            final Collection<EffectInstance> potions = new ArrayList<>(target.getActivePotionEffects());
            target.curePotionEffects(milk);
            float size = 0, damage = 0;
            for (EffectInstance ei : potions) {
                EffectInstance drop = new EffectInstance(ei.getPotion(), 0, -2);
                drop = EffectUtils.stackPot(caster, drop, EffectUtils.StackingMethod.MAXDURATION);
                if (drop.getAmplifier() >= 0) {
                    if (target.addPotionEffect(drop)) {//potion was wiped by milk
                        size += drop.getDuration();
                        damage++;
                    } else {//mob is immune (!?!?) or potion was not wiped
                        size += drop.getDuration();
                        damage += 0.5f;
                    }
                } else {
                    size += drop.getDuration();
                    damage++;
                }
            }
            size = Math.min(size / 100, 10);
            FakeExplosion.explode(caster.world, caster, target.getPosX(), target.getPosY() + target.getHeight() / 2, target.getPosZ(), size, DamageSource.causeExplosionDamage(target), damage * 4);
        }
    }
}