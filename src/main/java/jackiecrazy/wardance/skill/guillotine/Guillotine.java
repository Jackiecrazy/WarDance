package jackiecrazy.wardance.skill.guillotine;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.EffectUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Guillotine extends Skill {
    /*
    Redirects all might gain to another bar temporarily. Gaining 10 might in this manner will cause your next attack to deal 20% of the target's current health in damage. 10 second cooldown.
    */
    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return special;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return special;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.judgment;
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, int stack) {
        final float amount = stack == 3 ? target.getHealth() * 0.15f : target.getHealth() * 0.03f;
        if (this == WarSkills.AMPUTATION.get())
            CombatData.getCap(target).addWounding(amount);
        else
            target.attackEntityFrom(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).setDamageBypassesArmor().setDamageIsAbsolute(), amount);
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CombatData.getCap(caster).getComboRank() < 5)
            return CastStatus.OTHER;
        return super.castingCheck(caster);
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 3;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE && CombatData.getCap(caster).getComboRank() < 5) {
            markUsed(caster);
        }
        return cooldownTick(stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && cast(caster, -999)) {
            LivingEntity target = SkillUtils.aimLiving(caster);
            int stack = 1;
            float arb = Marks.getCap(target).getActiveMark(this).orElse(SkillData.DUMMY).getArbitraryFloat();
            if (arb >= 2) {//detonate
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                removeMark(target);
            } else prev.flagCondition(true);
            stack += arb;
            performEffect(caster, target, stack);
            mark(caster, target, 6, 1);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_RAVAGER_STEP, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        }
        if (to == STATE.COOLING) {
            if (prev.isCondition())
                setCooldown(caster, prev, 2);
            else setCooldown(caster, prev, 20);
            return true;
        }
        if (from != STATE.COOLING && to == STATE.HOLSTERED)
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        return boundCast(prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.setArbitraryFloat(sd.getArbitraryFloat() + existing.getArbitraryFloat());
            sd.setDuration(12);
        }
        return sd;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration(0.05f);
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingDeathEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingDeathEvent) procPoint).getEntityLiving() == target) {
            Marks.getCap(target).getActiveMark(this).ifPresent((a) -> CombatData.getCap(caster).addMight(a.getArbitraryFloat() * mightConsumption(caster) * 1.4f));
        }
    }

    public static class Brutalize extends Guillotine {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public float mightConsumption(LivingEntity caster) {
            return 5;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, int stack) {
            super.performEffect(caster, target, stack);
            if (stack == 3) {
                CombatData.getCap(target).consumePosture(caster, Float.MAX_VALUE, 0, true);
            }
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isHostile(a, caster));
            for (LivingEntity enemy : list) {
                enemy.addPotionEffect(new EffectInstance(WarEffects.ENFEEBLE.get(), 200));
                if (stack == 3 && target.getMaxHealth() > enemy.getMaxHealth())
                    EffectUtils.causeFear(enemy, caster, 200);
            }
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
            super.onProc(caster, procPoint, state, stats, target);
            if (procPoint instanceof StaggerEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST && ((StaggerEvent) procPoint).getAttacker() == caster) {
                ((StaggerEvent) procPoint).setCount(((StaggerEvent) procPoint).getCount() * 2);
                ((StaggerEvent) procPoint).setLength(((StaggerEvent) procPoint).getLength() * 2);
            }
        }
    }

    public static class LichtenbergScar extends Guillotine {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public float mightConsumption(LivingEntity caster) {
            return 1;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, int stack) {
            if (stack != 3) return;
            DamageSource cds = new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setSkillUsed(this).setProcSkillEffects(true).setProxy(target).setDamageBypassesArmor().setDamageIsAbsolute();
            target.attackEntityFrom(cds, target.getHealth() / 5);
            final float radius = 7;
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(radius), (a) -> TargetingUtils.isHostile(a, caster));
            //float damage = s.getArbitraryFloat() * (1 + CombatData.getCap(caster).getSpirit());
            for (LivingEntity baddie : list) {
                baddie.attackEntityFrom(cds, baddie.getHealth() / 10);
                LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(target.world);
                lightningboltentity.moveForced(baddie.getPosX(), baddie.getPosY(), baddie.getPosZ());
                lightningboltentity.setEffectOnly(true);
                target.world.addEntity(lightningboltentity);
                if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(baddie, lightningboltentity))
                    baddie.causeLightningStrike((ServerWorld) baddie.world, lightningboltentity);
            }
        }
    }

    public static class FeverDream extends Guillotine {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, int stack) {
            super.performEffect(caster, target, stack);
            if (stack != 3) return;
            hallucinate(caster, target, 3);
        }

        private void hallucinate(LivingEntity caster, LivingEntity target, int iterate) {
            if (iterate < 0) return;
            SkillUtils.createCloud(caster.world, caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(), 15, ParticleTypes.LARGE_SMOKE);
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(7), (a) -> TargetingUtils.isHostile(a, caster));
            Marks.getCap(target).removeMark(this);
            for (int i = 0; i < list.size(); i++) {
                LivingEntity enemy = list.get(i);
                if (GeneralUtils.getDistSqCompensated(target, enemy) < 49)
                    enemy.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 140));
                if (list.size() <= 1) return;
                int shift = (i * (list.size() - 1) + 1) % list.size();
                enemy.setRevengeTarget(list.get(shift));
                if (enemy instanceof MobEntity)
                    ((MobEntity) enemy).setAttackTarget(list.get(shift));
                if (Marks.getCap(enemy).isMarked(this)) {
                    hallucinate(caster, enemy, iterate - 1);
                    removeMark(enemy);
                }
            }
        }
    }

    public static class MastersLesson extends Guillotine {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.on_hurt, ProcPoints.normal_attack, ProcPoints.on_stagger, ProcPoints.on_cast, "melee", "execution")));

        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof StaggerEvent && ((StaggerEvent) procPoint).getEntityLiving() == target) {
                ((StaggerEvent) procPoint).setCount(1);
                ((StaggerEvent) procPoint).setLength(200);
            } else if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
                if (CombatData.getCap(target).getStaggerTime() > 0)
                    procPoint.setCanceled(true);
                else if (procPoint.getPhase() == EventPriority.HIGHEST && Marks.getCap(target).isMarked(this)) {
                    CombatData.getCap(target).consumePosture(((LivingAttackEvent) procPoint).getAmount());
                    procPoint.setCanceled(true);
                }
            }
        }
    }

    public static class CrowdPleaser extends Guillotine {
        @Override
        public Color getColor() {
            return Color.MAGENTA;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof SkillCastEvent && ((SkillCastEvent) procPoint).getSkill() != this && procPoint.getPhase() == EventPriority.HIGHEST) {
                stats.setArbitraryFloat(stats.getArbitraryFloat() + 1);
            } else super.onProc(caster, procPoint, state, stats, target);
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, int stack) {
            super.performEffect(caster, target, stack);
            int buff = 0;
            if (CasterData.getCap(caster).getSkillData(this).isPresent())
                buff = (int) CasterData.getCap(caster).getSkillData(this).get().getArbitraryFloat();
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isAlly(a, caster));
            for (LivingEntity pet : list) {
                if (stack == 1) {
                    pet.addPotionEffect(new EffectInstance(Effects.SPEED, buff * 40 + 60));
                    pet.addPotionEffect(new EffectInstance(Effects.LUCK, buff * 40 + 60));
                } else if (stack == 2) {
                    pet.addPotionEffect(new EffectInstance(Effects.STRENGTH, buff * 40 + 60));
                    pet.addPotionEffect(new EffectInstance(Effects.HASTE, buff * 40 + 60));
                } else {
                    pet.addPotionEffect(new EffectInstance(Effects.REGENERATION, buff * 40 + 60));
                    pet.addPotionEffect(new EffectInstance(Effects.RESISTANCE, buff * 40 + 60));
                }
            }
        }
    }
}
