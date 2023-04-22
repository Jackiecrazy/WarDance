package jackiecrazy.wardance.skill.flurry;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.GainMightEvent;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class Flurry extends Skill {
    private static final ResourceLocation rl = new ResourceLocation("wardance:textures/skill/flurry.png");

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
    public HashSet<String> getTags(LivingEntity caster) {
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
        final ICombatCapability cap = CombatData.getCap(caster);
        if (!cap.consumeMight(0.05f)) markUsed(caster);
        caster.attackStrengthTicker++;
        cap.setOffhandCooldown(cap.getOffhandCooldown() + 1);
        //main hand flurry
        if (stats.getState() == STATE.ACTIVE && CombatUtils.getCooledAttackStrength(caster, InteractionHand.MAIN_HAND, 0.5f) == 1f && !caster.isAutoSpinAttack()) {
            cap.setForcedSweep(360);
            CombatUtils.sweep(caster, null, InteractionHand.MAIN_HAND, 3);
        }
        //offhand flurry
        if (stats.getState() == STATE.ACTIVE && CombatUtils.getCooledAttackStrength(caster, InteractionHand.OFF_HAND, 0.5f) == 1f && !caster.isAutoSpinAttack()) {
            //spin to win!
            cap.setForcedSweep(360);
            CombatUtils.sweep(caster, null, InteractionHand.OFF_HAND, 3);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof GainMightEvent gme && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.LOWEST) {
            gme.setQuantity(0);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 1)) {
            CasterData.getCap(caster).removeActiveTag(SkillTags.state);
            prev.setMaxDuration(0);
            return true;
        }
        if(from==STATE.ACTIVE&&to==STATE.COOLING){
            prev.setState(STATE.INACTIVE);
        }
        return instantCast(prev, from, to);
    }
}
