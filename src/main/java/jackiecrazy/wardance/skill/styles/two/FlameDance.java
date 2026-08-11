package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.api.FootworkDamageTypeTags;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class FlameDance extends WarCry {
    private static final UUID attackSpeed = UUID.fromString("338a5b6f-46c2-44b6-921f-f15c5e59cd48");
    private static final AttributeModifier knockback = new AttributeModifier(attackSpeed, "flame dance debuff", -0.4, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final int MAX_FLAME_STACKS = 100;

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, Attributes.ATTACK_KNOCKBACK, knockback);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, Attributes.ATTACK_KNOCKBACK, knockback);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (caster == target) return;
        if (procPoint instanceof LivingHurtEvent lae && notRecursive(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            if (hasMark(target) && getExistingMark(target).getArbitraryFloat() > 50 && !CombatData.getCap(target).alreadyProc("flameDance_mark")) {
                target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
                target.hurt(new CombatDamageSource(caster, null, null).setDamageDealer(null).setAttackingHand(null).setIsFire().flagBreach(false).setIndirect(true).setArmorReductionPercentage(1).setKnockbackPercentage(0).setSkillUsed(this).setProcSkillEffects(true), getExistingMark(target).getArbitraryFloat() * SkillUtils.getSkillEffectiveness(caster) / MAX_FLAME_STACKS);
                CombatData.getCap(target).tickProc("flameDance_mark");
            }
            if (!CombatData.getCap(caster).alreadyProc("flameDance") && hasMark(target) && getExistingMark(target).getDuration() > 1.5) {
                //rapid attack, heat wave
                heatWave(caster, 5);
                CombatData.getCap(caster).tickProc("flameDance");
            }
            mark(caster, target, 1.70f, 0);
        }
        if (procPoint instanceof SkillCastEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && !CombatData.getCap(caster).alreadyProc("flameDance") && sce.getEntity() == caster) {
            heatWave(caster, 8);
            CombatData.getCap(caster).tickProc("flameDance");
        }
        if (procPoint instanceof PlayInteractionEvent.Interaction p) {
            switch (p.getOriginalState()) {
                case THROW, DRAW_ATTACK:
                    heatWave(caster, 5);
                    CombatData.getCap(caster).tickProc("flameDance");
            }
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    private boolean notRecursive(DamageSource lae) {
        return !lae.is(DamageTypeTags.IS_FIRE) && !lae.is(DamageTypeTags.BYPASSES_RESISTANCE) && !lae.is(FootworkDamageTypeTags.SKILL);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (caster.tickCount % 5 == 0) {
            //aoe mark
            heatWave(caster, 3);
        }
        return super.equippedTick(caster, stats);
    }

    private void heatWave(LivingEntity caster, float amount) {
        final double reach = caster.getAttributeValue(ForgeMod.ENTITY_REACH.get());
        for (LivingEntity e : caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(reach * 2))) {
            if (GeneralUtils.getDistSqCompensated(caster, e) < reach * reach && TargetingUtils.isHostile(e, caster)) {
                mark(caster, e, 1.5f, amount);
            }
        }
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        boolean ret = markTickDown(sd);
        if (sd.getDuration() <= 0 && sd.getArbitraryFloat() >= 1) {
            sd.addArbitraryFloat(-1f);
            sd.setDuration(0.05f);
        }
        return ret;
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        float orig = sd.getArbitraryFloat();
        if (existing != null)
            sd.addArbitraryFloat(existing.getArbitraryFloat());
        SkillUtils.modifyAttribute(target, Attributes.ARMOR, attackSpeed, -sd.getArbitraryFloat() / 100d, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (sd.getArbitraryFloat() > MAX_FLAME_STACKS/2) {
            target.removeEffect(MobEffects.FIRE_RESISTANCE);
        }//else ((IFlameDance)target).warDance$stripFireResist(false);
        if (orig > 3 || (sd.getArbitraryFloat() > MAX_FLAME_STACKS && target.getRemainingFireTicks() < 10)) {
            target.setSecondsOnFire(2 + (int) (sd.getArbitraryFloat() / 25));
        }
        if (sd.getArbitraryFloat() > MAX_FLAME_STACKS && orig > 0 && !CombatData.getCap(target).alreadyProc("flameDance_mark")) {
            target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
            target.hurt(new CombatDamageSource(caster, null, null).setDamageDealer(null).setAttackingHand(null).setDamageTyping(FootworkDamageArchetype.TRUE).setIndirect(true).flagBreach(false).setArmorReductionPercentage(1).setKnockbackPercentage(0).setSkillUsed(this).setProcSkillEffects(true), orig / 10);
            sd.setArbitraryFloat(MAX_FLAME_STACKS);
            target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
            CombatData.getCap(target).tickProc("flameDance_mark");
        }
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        SkillUtils.removeAttribute(target, Attributes.ARMOR, attackSpeed);
        super.onMarkEnd(caster, target, sd);
    }
}
