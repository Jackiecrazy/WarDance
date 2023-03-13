package jackiecrazy.wardance.skill.hex;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.LuckEvent;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.SetTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import jackiecrazy.wardance.skill.Skill.STATE;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Hex extends Skill {
    static final UUID HEX = UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1");
    private final SetTag<String> tag = makeTag("melee", "noDamage", "boundCast", ProcPoints.afflict_tick, ProcPoints.change_parry_result, ProcPoints.recharge_time, "normalAttack", "chant", "countdown");
    private final SetTag<String> thing = makeTag(SkillTags.offensive, SkillTags.magical);

    @SubscribeEvent
    public static void snakebite(LivingHealEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        LivingEntity entity = e.getEntityLiving();
        //snakebite nullifies healing
        Marks.getCap(entity).getActiveMark(WarSkills.GANGRENE.get()).ifPresent(a -> e.setCanceled(a.getArbitraryFloat() > 0));
    }

    @SubscribeEvent
    public static void echoes(LivingHurtEvent e) {
        LivingEntity target = e.getEntityLiving();
        Marks.getCap(target).getActiveMark(WarSkills.CURSE_OF_ECHOES.get()).ifPresent(a -> {
            if (a.getArbitraryFloat() < 0 && CombatUtils.isMeleeAttack(e.getSource())) {
                target.invulnerableTime = 0;
                target.hurt(new CombatDamageSource("echo", a.getCaster(target.level)).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(WarSkills.CURSE_OF_ECHOES.get()).bypassArmor().bypassMagic(), e.getAmount() * 0.4f);
                a.setArbitraryFloat(20);
            }
        });
    }

    @SubscribeEvent
    public static void blackmark(LivingDamageEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        LivingEntity target = e.getEntityLiving();
        //black mark stealing health/posture/spirit
        if (e.getAmount() > 0 && CombatUtils.isMeleeAttack(e.getSource()) && Marks.getCap(target).isMarked(WarSkills.BLACK_MARK.get())) {
            Entity source = e.getSource().getEntity();
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
        if (!CombatUtils.isPhysicalAttack(e.getSource()))
            Marks.getCap(e.getEntityLiving()).getActiveMark(WarSkills.GANGRENE.get()).ifPresent(g -> g.setArbitraryFloat(40));
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
    public SetTag<String> getTags(LivingEntity caster) {
        return thing;
    }

    @Nonnull
    @Override
    public SetTag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 4;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.hex;
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity e = SkillUtils.aimLiving(caster);
        if (to == STATE.ACTIVE && e != null && cast(caster, e, -999)) {
            mark(caster, e, duration(), prev.getArbitraryFloat());
            prev.setArbitraryFloat(0);
            markUsed(caster);
        }
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 15);
        return boundCast(prev, from, to);
    }

    protected int duration() {
        return 200;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (this == WarSkills.CURSE_OF_MISFORTUNE.get() && procPoint instanceof LuckEvent.Post && state != STATE.COOLING) {
            stats.setArbitraryFloat(Math.min(11, stats.getArbitraryFloat() + 1));
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (cooldownTick(stats)) {
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        final AttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null && this == WarSkills.CURSE_OF_MISFORTUNE.get()) {
            luck.removeModifier(HEX);
            AttributeModifier am = new AttributeModifier(HEX, "hex", -2 - sd.getArbitraryFloat(), AttributeModifier.Operation.ADDITION);
            sd.setArbitraryFloat(0);
            luck.addTransientModifier(am);
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final AttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) {
            luck.removeModifier(HEX);
        }
        super.onMarkEnd(caster, target, sd);
    }

    public static class Gangrene extends Hex {
        @Override
        public Color getColor() {
            return WarColors.VIOLET;
        }

        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            sd.setArbitraryFloat(sd.getArbitraryFloat() - 1);
            return super.markTick(caster, target, sd);
        }

        @Override
        protected int duration() {
            return 160;
        }
    }

    public static class BlackMark extends Hex {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public static class CurseOfEchoes extends Hex {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            sd.setArbitraryFloat(sd.getArbitraryFloat() - 1);
            return super.markTick(caster, target, sd);
        }
    }

    public static class Unravel extends Hex {

        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        protected void mark(LivingEntity caster, LivingEntity target, float duration, float arbitrary) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            final Collection<MobEffectInstance> potions = new ArrayList<>(target.getActiveEffects());
            target.curePotionEffects(milk);
            float size = 8, damage = 6;
            boolean proc = false;
            for (MobEffectInstance ei : potions) {
                proc = true;
                MobEffectInstance drop = new MobEffectInstance(ei.getEffect(), 0, -2);
                drop = EffectUtils.stackPot(caster, drop, EffectUtils.StackingMethod.MAXDURATION);
                if (drop.getAmplifier() >= 0) {
                    target.addEffect(drop);
                }
            }
            if (proc)
                FakeExplosion.explode(caster.level, caster, target.getX(), target.getY() + target.getBbHeight() * 1.1f, target.getZ(), size, DamageSource.explosion(caster).setMagic(), damage);
        }
    }


}