package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.*;
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
import java.util.Arrays;
import java.util.HashSet;

public class CoupDeGrace extends Skill {
    private final HashSet<String> tag = new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.normal_attack, ProcPoints.on_hurt, ProcPoints.recharge_cast, ProcPoints.change_parry_result, "execution"));

    protected float getDamage(LivingEntity caster, LivingEntity target) {
        return (GeneralUtils.getMaxHealthBeforeWounding(target) - target.getHealth()) * 0.2f;
    }

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return special;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return special;
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
        return 1;
    }

    protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingHurtEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (e.getEntity() != caster) {
                if (CombatData.getCap(e.getEntity()).isExposed() && !CombatData.getCap(e.getEntity()).isStaggeringStrike()) {
                    if (willKillOnCast(caster, target))
                        target.setHealth(1);
                    e.setAmount(e.getAmount() + getDamage(caster, target));
                    e.getSource().bypassArmor().bypassMagic();
                    deathCheck(caster, target, e.getAmount());
                    markUsed(caster);
                } else if (target.getHealth() < getDamage(caster, target)) {
                    e.setCanceled(true);
                    CombatData.getCap(target).consumePosture(caster, e.getAmount());
                }
            }
        } else if (procPoint instanceof SkillCastEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
            stats.decrementDuration();
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
            CasterData.getCap(caster).removeActiveTag(SkillTags.special);
        }
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 1)) {
            CasterData.getCap(caster).removeActiveTag(SkillTags.special);
            prev.setMaxDuration(0);
        }
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 2);
        return instantCast(prev, from, to);
    }

    public boolean willKillOnCast(LivingEntity caster, LivingEntity target) {
        return target.getHealth() < getDamage(caster, target);
    }

    public static class Rupture extends CoupDeGrace {

        @Override
        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            CombatData.getCap(target).consumeSpirit(CombatData.getCap(target).getSpirit() / 2);
            FakeExplosion.explode(caster.level, caster, target.getX(), target.getY(), target.getZ(), (float) Math.sqrt(CombatData.getCap(target).getMaxPosture()), new CombatDamageSource("explosion.player", caster).setProxy(target).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setExplosion().setMagic(), 4 * CombatData.getCap(target).getSpirit());
        }
    }

    public static class DanseMacabre extends CoupDeGrace {

        @Override
        protected float getDamage(LivingEntity caster, LivingEntity target) {
            return GeneralUtils.getMaxHealthBeforeWounding(target) * (1 - (target.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(target))) * (0.2f + 0.2f * (CombatData.getCap(caster).getRank() / 10));
        }
    }

    public static class ReapersLaugh extends CoupDeGrace {
        private final HashSet<String> tag = (new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.change_awareness, ProcPoints.on_hurt, ProcPoints.recharge_cast, ProcPoints.change_parry_result, "execution")));
        private final HashSet<String> tague = (new HashSet<>(Arrays.asList(SkillTags.special, SkillTags.offensive)));

        @Override
        public HashSet<String> getTags(LivingEntity caster) {
            return special;
        }

        @Override
        public float mightConsumption(LivingEntity caster) {
            return 0;
        }

        @Override
        public boolean willKillOnCast(LivingEntity caster, LivingEntity target) {
            return target.getHealth() < (GeneralUtils.getMaxHealthBeforeWounding(target) * 0.10f + caster.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }

        @Override
        public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
            if (from == STATE.INACTIVE && to == STATE.HOLSTERED) {
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.RAVAGER_CELEBRATE, SoundSource.PLAYERS, 0.8f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                prev.setState(STATE.HOLSTERED);
            }
            if (to == STATE.ACTIVE && cast(caster)) {
                //DIE!
                for (Entity e : caster.level.getEntities(caster, caster.getBoundingBox().inflate(caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get())), (a -> !TargetingUtils.isAlly(a, caster)))) {
                    if (!(e instanceof LivingEntity) || !caster.hasLineOfSight(e)) continue;
                    final CombatDamageSource die = new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setSkillUsed(this).setKnockbackPercentage(0);
                    if (willKillOnCast(caster, (LivingEntity) e)) {
                        prev.flagCondition(true);
                        die.setCrit(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).bypassArmor().bypassMagic();
                    }
                    e.hurt(die, GeneralUtils.getMaxHealthBeforeWounding((LivingEntity) e) * 0.1f + (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE));
                }
            }
            if (to == STATE.COOLING)
                if (prev.isCondition())
                    prev.setState(STATE.INACTIVE);
                else setCooldown(caster, prev, 5);
            boundCast(prev, from, to);
            return from != prev.getState();
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof SkillCastEvent && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
                stats.decrementDuration();
            }
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
                if (d.getState() == STATE.COOLING && d.getSkill().getTags(caster).contains(SkillTags.physical)) {
                    isc.changeSkillState(d.getSkill(), STATE.INACTIVE);
                }
            }
        }
    }

}
