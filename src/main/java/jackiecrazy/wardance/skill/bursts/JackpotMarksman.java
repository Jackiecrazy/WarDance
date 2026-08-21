package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.event.GainAdrenalineEvent;
import jackiecrazy.footwork.move.ActionSets;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.utils.ActionJsonAdapters;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.skill.JackpotCoin;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;

public class JackpotMarksman extends Skill {
    /*
    Collect coins from healing/buffing others, or as tax when striking others, or over time by draining adrenaline.
    When the time is up, toss all your coins in the air and throw a weapon or your final coin at them,
    causing that projectile to repeatedly ricochet before flying at the biggest enemy it can find.
    chip off flying gold nuggets to strike lesser targets on each bounce
     */

    @Override
    public HashSet<String> getTags() {
        return burst;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return burst;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (!StylishData.getCap(caster).drainAdrenaline(0.005f)) {
                markUsed(caster);
                kaping(caster, caster.level().getEntity((int) stats.getMaxDuration()), stats.getArbitraryFloat());
                //stats.setArbitraryFloat(0);
                return true;
            }
            if(stats.getDuration()<6&& caster.tickCount%15==0&&stats.getArbitraryFloat()>=1){
                JackpotCoin tgt = createCoins(caster, stats);
                stats.setMaxDuration(tgt.getId());
            }
            stats.addArbitraryFloat(0.02f* stats.getEffectiveness());
            return activeTick(stats);
        }
        return false;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (!StylishData.getCap(caster).maxAdrenaline()) return CastStatus.ADRENALINE;
        return super.castingCheck(caster, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.ACTIVE) {
            if (procPoint instanceof MobEffectEvent.Added le && le.getEffectSource() == caster && procPoint.getPhase() == EventPriority.HIGHEST) {
                if (le.getEffectInstance().getEffect().getCategory() == MobEffectCategory.BENEFICIAL)
                    stats.addArbitraryFloat(le.getEffectInstance().getAmplifier() + 1);
            }
            if (procPoint.getPhase() == EventPriority.LOWEST && procPoint instanceof LivingDamageEvent e && e.getEntity() == target) {
                stats.addArbitraryFloat(e.getAmount() / 10);
            }
            if (procPoint instanceof GainAdrenalineEvent gme) {
                gme.setQuantity(0);
            }
        }
    }

    private JackpotCoin createCoins(LivingEntity caster, SkillData stats) {
        caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.25f, 1.4f + WarDance.rand.nextFloat() * 0.4f);
        Vec3 approxCenter = caster.getEyePosition().add(caster.getLookAngle().scale(3));
        JackpotCoin ret = null;
        final float perToss = Math.min(4, stats.getArbitraryFloat());
        stats.addArbitraryFloat(-perToss);
        for (float x = perToss; x > 0; x--) {
            JackpotCoin jc = new JackpotCoin(WarEntities.COIN.get(), caster.level());
            jc.moveTo(approxCenter.add((WarDance.rand.nextDouble() - 0.5) * 16, (WarDance.rand.nextDouble()) * 4, (WarDance.rand.nextDouble() - 0.5) * 16));
            jc.addDeltaMovement(new Vec3(0, 0.3, 0));
            caster.level().addFreshEntity(jc);
            ret = jc;
        }
        return ret;
    }

    private void kaping(LivingEntity caster, Entity target, double damage) {
        ThrownWeaponEntity fwe = new ThrownWeaponEntity(WarEntities.THROWN_WEAPON.get(), caster.level());
        fwe.setHeldItem(caster.getItemInHand(InteractionHand.MAIN_HAND).copyWithCount(1));
        fwe.setOwner(caster);
        fwe.moveTo(caster.getEyePosition());
        fwe.yeet(target.position().add(target.getDeltaMovement()), 2);
        fwe.setMotionTarget(target);
        fwe.setFake(true).setFlourish(false).setEffect(FlyingWeaponEffect.WEAPON);
        fwe.setPierce(9999).setLodgeEntity(false).setInteractionRange(1);

        //fwe.yeet(pos, strength);
        fwe.setInteractionRange(1f);
        fwe.setEmbedActions(List.of(ActionJsonAdapters.gson.fromJson(ActionSets.moves.get(new ResourceLocation("wardance:expire_10s")), Action[].class)));
        fwe.setHitInfo(new HitInfo(0, 1, 1, true, true, damage));
        caster.level().addFreshEntity(fwe);
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 12)) {
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            StylishData.getCap(caster).resetAdrenaline();
            return true;
        }
        return instantCast(prev, from, to);
    }


}
