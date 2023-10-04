package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.ExposeAttackEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class CoupDeGrace extends Skill {
    private final HashSet<String> tag = new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.normal_attack, ProcPoints.on_hurt, ProcPoints.recharge_cast, ProcPoints.change_parry_result, "execution"));

    protected float getDamage(LivingEntity caster, LivingEntity target, @Nullable SkillData stats) {
        return (target.getMaxHealth() - target.getHealth()) * 0.2f * (stats == null ? 1 : stats.getEffectiveness());
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.coup_de_grace;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        final CastStatus supes = super.castingCheck(caster);
        return supes == CastStatus.ACTIVE | supes == CastStatus.HOLSTERED ? CastStatus.ALLOWED : supes;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 2;
    }

    @Override
    public HashSet<String> getTags() {
        return special;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return special;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE) {
            if (procPoint instanceof LivingHurtEvent e && e.getEntity() == target) {
                if (CombatData.getCap(e.getEntity()).isExposed() && !CombatData.getCap(e.getEntity()).isStaggeringStrike()) {
                    if (willKillOnCast(caster, target, stats))
                        target.setHealth(1);
                    if(e.getSource() instanceof CombatDamageSource cds)
                        cds.setDamageTyping(CombatDamageSource.TYPE.TRUE);
                    e.getSource().bypassMagic().bypassArmor();
                    deathCheck(caster, target, e.getAmount());
                    markUsed(caster);
                } else if (!CombatData.getCap(e.getEntity()).isExposed() && willKillOnCast(caster, target, stats)) {
                    e.setCanceled(true);
                    CombatData.getCap(target).consumePosture(caster, e.getAmount());
                }
            }
            if (procPoint instanceof ExposeAttackEvent e && e.getEntity() == target) {
                e.setAmount(getDamage(caster, target, stats));
            }
        } else if (procPoint instanceof SkillCastEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
            stats.decrementDuration();
        } else if (procPoint instanceof StunEvent e && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE) {
            if (e.getEntity() == target) {
                if (willKillOnCast(caster, target, stats)) {
                    CombatData.getCap(target).expose(CombatConfig.exposeDuration);
                }
            }
        }
        if (state == STATE.ACTIVE && stats.getDuration() < 0 && this == WarSkills.DECAPITATE.get() && procPoint.getPhase() == EventPriority.LOWEST) {
            if (procPoint instanceof LivingDropsEvent && ((LivingDropsEvent) procPoint).getEntity() == target) {
                ItemStack drop = GeneralUtils.dropSkull(target);
                if (drop == null) return;
                for (ItemEntity i : ((LivingDropsEvent) procPoint).getDrops()) {
                    if (i.getItem().getItem() == drop.getItem() && (!(target instanceof Player) || i.getItem().getOrCreateTag().getString("SkullOwner").equalsIgnoreCase(drop.getTag().getString("SkullOwner"))))
                        return;
                }
                ItemEntity forceSkull = new ItemEntity(target.level, target.getX(), target.getY(), target.getZ(), drop);
                forceSkull.setDefaultPickUpDelay();
                ((LivingDropsEvent) procPoint).getDrops().add(forceSkull);
            }

            if (procPoint instanceof LootingLevelEvent && ((LootingLevelEvent) procPoint).getEntity() == target && GeneralUtils.dropSkull(target) == null) {
                ((LootingLevelEvent) procPoint).setLootingLevel(((LootingLevelEvent) procPoint).getLootingLevel() + 3);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.ACTIVE && to == STATE.HOLSTERED) {
            //return statement necessary to prevent instant cast re-activating the effect
            prev.setState(STATE.INACTIVE);
            return true;
        }
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 1)) {
            CasterData.getCap(caster).removeActiveTag(SkillTags.special);
            prev.setMaxDuration(0);
            return true;
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 2);
            return true;
        }
        return boundCast(prev, from, to);
    }

    protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
    }

    public boolean willKillOnCast(LivingEntity caster, LivingEntity target, SkillData stats) {
        return target.getHealth() < getDamage(caster, target, stats);
    }

    public static class Rupture extends CoupDeGrace {

        @Override
        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            CombatData.getCap(target).consumeSpirit(CombatData.getCap(target).getSpirit() / 2);
            FakeExplosion.explode(caster.level, caster, target.getX(), target.getY(), target.getZ(), (float) Math.sqrt(CombatData.getCap(target).getMaxPosture()), new CombatDamageSource("explosion.player", caster).setProxy(target).setSkillUsed(this).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setExplosion().setMagic(), 4 * CombatData.getCap(target).getSpirit());
        }
    }

    public static class DanseMacabre extends CoupDeGrace {

        @Override
        protected float getDamage(LivingEntity caster, LivingEntity target, SkillData sd) {
            return GeneralUtils.getMaxHealthBeforeWounding(target) * SkillUtils.getSkillEffectiveness(caster) * (1 - (target.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(target))) * (0.2f + 0.2f * (CombatData.getCap(caster).getRank() / 10));
        }
    }

    public static class ReapersLaugh extends CoupDeGrace {
        private final HashSet<String> tag = (new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.change_awareness, ProcPoints.on_hurt, ProcPoints.recharge_cast, ProcPoints.change_parry_result, "execution")));
        private final HashSet<String> tague = (new HashSet<>(Arrays.asList(SkillTags.special, SkillTags.offensive)));

        @Override
        public float mightConsumption(LivingEntity caster) {
            return 0;
        }

        @Override
        public HashSet<String> getTags() {
            return special;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof SkillCastEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
                stats.decrementDuration();
            }
        }

        @Override
        public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
            if (from == STATE.INACTIVE && to == STATE.HOLSTERED) {
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.RAVAGER_CELEBRATE, SoundSource.PLAYERS, 0.8f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                prev.setState(STATE.HOLSTERED);
            }
            if (to == STATE.ACTIVE && cast(caster)) {
                //DIE!
                ParticleUtils.playSweepParticle(FootworkParticles.CIRCLE.get(), caster, caster.position().add(caster.getLookAngle().multiply(1, 0, 1)), 0, caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get()), Color.RED, 0.5f);
                for (Entity e : caster.level.getEntities(caster, caster.getBoundingBox().inflate(caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get())), (a -> !TargetingUtils.isAlly(a, caster)))) {
                    if (!(e instanceof LivingEntity) || !caster.hasLineOfSight(e)) continue;
                    final CombatDamageSource die = new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this);
                    if (willKillOnCast(caster, (LivingEntity) e, prev)) {
                        die.setCrit(true).setCritDamage(1).setKnockbackPercentage(0).setDamageTyping(CombatDamageSource.TYPE.TRUE).bypassArmor().bypassMagic();
                    }
                    e.hurt(die, GeneralUtils.getMaxHealthBeforeWounding((LivingEntity) e) * prev.getEffectiveness() / 10 + (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    if (((LivingEntity) e).isDeadOrDying()) prev.flagCondition(true);
                }
            }
            if (to == STATE.COOLING) {
                if (prev.isCondition())
                    prev.setState(STATE.INACTIVE);
                else setCooldown(caster, prev, 5);
                prev.flagCondition(false);
            }
            boundCast(prev, from, to);
            return from != prev.getState();
        }

        @Override
        public boolean willKillOnCast(LivingEntity caster, LivingEntity target, SkillData stats) {
            return target.getHealth() < (GeneralUtils.getMaxHealthBeforeWounding(target) * stats.getEffectiveness() / 10 + caster.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }

        @Override
        protected boolean showArchetypeDescription() {
            return false;
        }
    }

    public static class Reinvigorate extends CoupDeGrace {

        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            final ICombatCapability cap = CombatData.getCap(caster);
        }
    }

    public static class Frenzy extends CoupDeGrace {

        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            ISkillCapability isc = CasterData.getCap(caster);
            for (SkillData d : isc.getAllSkillData().values()) {
                if (d.getState() == STATE.COOLING && d.getSkill().getTags().contains(SkillTags.physical)) {
                    isc.changeSkillState(d.getSkill(), STATE.INACTIVE);
                }
            }
        }
    }

}
