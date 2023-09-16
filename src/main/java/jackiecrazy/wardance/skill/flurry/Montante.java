package jackiecrazy.wardance.skill.flurry;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.GainMightEvent;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class Montante extends Skill {
    private static final AttributeModifier bad = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117600c1"), "flurry", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final ResourceLocation rl = new ResourceLocation("wardance:textures/skill/montante.png");

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CombatData.getCap(caster).getMight() < 1) return CastStatus.OTHER;
        return super.castingCheck(caster);
    }

    @Override
    public ResourceLocation icon() {
        return rl;
    }

    /*
    Whirlwind: until your might is emptied, continuously attack with alternating hands as soon as they recharge. Requires at least 5 might to start.
Blade storm: after every attack, choose one direction to lunge forward in
Sinawali: all attacks will sweep for range 3
Montante: incoming melee attacks are deflected at high posture damage to the attacker, your attack speed is doubled but you only attack with the main hand
Helicopter kick: disables both hands, instead dealing 7 posture damage per attack with large knockback and deflecting projectiles
Flow: cooldown of all attack skills are halved, and any cooled attack skill is automatically cast
     */
    @Override
    public HashSet<String> getTags() {
        return state;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return state;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() != STATE.ACTIVE) return false;
        if (!CombatData.getCap(caster).consumeMight(0.05f / stats.getEffectiveness())) markUsed(caster);
        if (stats.getState() == STATE.ACTIVE && caster.tickCount % 10 == 0 && !caster.isAutoSpinAttack()) {
            //spin to win!
            double reach = caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get());
            CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, 1f, false);
            CombatUtils.sweep(caster, null, InteractionHand.MAIN_HAND, WeaponStats.SWEEPTYPE.CIRCLE, reach, reach, 0);
            CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, 0, true);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ParryEvent lae && lae.getEntity() == caster && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.LOWEST) {
            lae.setPostureConsumption(0);
            if (target != null)
                CombatData.getCap(lae.getAttacker()).consumePosture(caster, CombatUtils.getPostureAtk(caster, target, InteractionHand.MAIN_HAND, null, (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE), caster.getMainHandItem()));
            lae.setResult(Event.Result.ALLOW);
        }
        if (procPoint instanceof ProjectileParryEvent lae && lae.getEntity() == caster && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.LOWEST) {
            lae.setPostureConsumption(0);
            lae.setResult(Event.Result.ALLOW);
        }
        if (procPoint instanceof GainMightEvent gme && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.LOWEST) {
            gme.setQuantity(0);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 1)) {
            CasterData.getCap(caster).removeActiveTag(SkillTags.state);
            SkillUtils.addAttribute(caster, Attributes.ATTACK_DAMAGE, bad);
            prev.setMaxDuration(0);
            return true;
        }
        if (from == STATE.ACTIVE && to == STATE.COOLING) {
            SkillUtils.removeAttribute(caster, Attributes.ATTACK_DAMAGE, bad);
            prev.setState(STATE.INACTIVE);
        }
        return instantCast(prev, from, to);
    }
}
