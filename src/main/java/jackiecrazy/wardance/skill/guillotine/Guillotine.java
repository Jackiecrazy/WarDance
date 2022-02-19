package jackiecrazy.wardance.skill.guillotine;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.GainMightEvent;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
        return SkillCategories.guillotine;
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
        CombatData.getCap(target).addWounding(amount);
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CombatData.getCap(caster).getComboRank() < 5)
            return CastStatus.OTHER;
        final CastStatus supes = super.castingCheck(caster);
        return supes == CastStatus.ACTIVE | supes == CastStatus.HOLSTERED ? CastStatus.ALLOWED : supes;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE && CombatData.getCap(caster).getComboRank() < 5) markUsed(caster);
        return cooldownTick(stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && cast(caster, -999)) {
            LivingEntity target = SkillUtils.aimLiving(caster);
            Marks.getCap(target).getActiveMark(this).ifPresent((a) -> {
                if (a.getArbitraryFloat() >= 2) {//detonate
                    target.attackEntityFrom(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).setDamageBypassesArmor().setDamageIsAbsolute(), target.getHealth() * 0.15f);
                    caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                    prev.flagCondition(true);
                }
            });
            target.attackEntityFrom(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).setDamageBypassesArmor().setDamageIsAbsolute(), target.getHealth() * 0.03f);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_RAVAGER_STEP, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        }
        if (to == STATE.COOLING) {
            float arb = prev.getArbitraryFloat();
            if (prev.isCondition())
                setCooldown(caster, prev, 20);
            else setCooldown(caster, prev, 4);
            prev.setArbitraryFloat(arb);
            return true;
        }
        if (to == STATE.HOLSTERED)
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        return instantCast(prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.setArbitraryFloat(sd.getArbitraryFloat() + existing.getArbitraryFloat());
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingAttackEvent) procPoint).getEntityLiving() == target && CombatData.getCap(caster).getMight() > 9.8) {
            performEffect(caster, target, execute((LivingAttackEvent) procPoint), stats);
            CombatData.getCap(caster).setMight(stats.getArbitraryFloat());
            markUsed(caster);
        }
    }

    protected float execute(LivingAttackEvent e) {
        final float life = e.getEntityLiving().getHealth() / 5;
        if (this != WarSkills.AMPUTATION.get())
            e.getEntityLiving().setHealth(e.getEntityLiving().getHealth() - life);
        e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
        return life + e.getAmount();
    }

    public static class Brutalize extends Guillotine {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isHostile(a, caster));
            for (LivingEntity enemy : list) {
                enemy.addPotionEffect(new EffectInstance(WarEffects.ENFEEBLE.get(), 200));
                if (amount > enemy.getMaxHealth())
                    enemy.addPotionEffect(new EffectInstance(WarEffects.PARALYSIS.get(), 200));
                else if (target.getMaxHealth() > enemy.getMaxHealth())
                    EffectUtils.causeFear(enemy, caster, 200);
            }
        }
    }

    public static class LichtenbergScar extends Guillotine {
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            CombatDamageSource cds = new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setSkillUsed(this).setProcSkillEffects(true).setProxy(target);
            final float radius = 7;
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(radius), (a) -> TargetingUtils.isHostile(a, caster));
            //float damage = s.getArbitraryFloat() * (1 + CombatData.getCap(caster).getSpirit());
            float damage = (4 * CombatData.getCap(caster).getSpirit()) / list.size();
            CombatData.getCap(caster).setSpirit(0);
            for (LivingEntity baddie : list) {
                baddie.attackEntityFrom(cds, damage);
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
        public boolean equippedTick(LivingEntity caster, SkillData d) {
            if (d.getState() == STATE.ACTIVE && caster.ticksExisted % 10 == 0) {
                final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(7), (a) -> !TargetingUtils.isAlly(a, caster) && !Marks.getCap(a).isMarked(this));
                for (LivingEntity enemy : list) {
                    if (Marks.getCap(enemy).isMarked(this))
                        continue;
                    enemy.addPotionEffect(new EffectInstance(WarEffects.DISTRACTION.get(), 140));
                    mark(caster, enemy, 10);
                    break;
                }
            }
            return super.equippedTick(caster, d);
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            SkillUtils.createCloud(caster.world, caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(), 15, ParticleTypes.LARGE_SMOKE);
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(14), (a) -> TargetingUtils.isHostile(a, caster));
            Marks.getCap(target).removeMark(this);
            for (int i = 0; i < list.size(); i++) {
                LivingEntity enemy = list.get(i);
                if (GeneralUtils.getDistSqCompensated(caster, enemy) < 49)
                    enemy.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 140));
                if (list.size() <= 1) return;
                if (!Marks.getCap(enemy).isMarked(this) || enemy == target) continue;
                int shift = (i * (list.size() - 1) + 1) % list.size();
                enemy.setRevengeTarget(list.get(shift));
                if (enemy instanceof MobEntity)
                    ((MobEntity) enemy).setAttackTarget(list.get(shift));
                Marks.getCap(enemy).removeMark(this);
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
        public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
            if (from == STATE.ACTIVE && to == STATE.HOLSTERED) {
                CasterData.getCap(caster).removeActiveTag(SkillTags.special);
            }
            if (from == STATE.INACTIVE && to == STATE.HOLSTERED) {
                CasterData.getCap(caster).removeActiveTag(SkillTags.special);
                activate(caster, 1);
                CombatData.getCap(caster).setMight(0);
            }
            if (to == STATE.COOLING)
                setCooldown(caster, prev, 10);
            return instantCast(prev, from, to);
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (state == STATE.ACTIVE) {
                if (procPoint instanceof StaggerEvent && ((StaggerEvent) procPoint).getEntityLiving() == target) {
                    ((StaggerEvent) procPoint).setCount(1);
                    ((StaggerEvent) procPoint).setLength(200);
                } else if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
                    if (CombatData.getCap(target).getStaggerTime() > 0)
                        procPoint.setCanceled(true);
                    else if (procPoint.getPhase() == EventPriority.LOWEST) {
                        CombatData.getCap(target).consumePosture(((LivingAttackEvent) procPoint).getAmount());
                        procPoint.setCanceled(true);
                    }
                } else if (procPoint instanceof GainMightEvent) {
                    procPoint.setCanceled(true);
                }
            }
//            else if (procPoint instanceof LivingHurtEvent && ((LivingHurtEvent) procPoint).getEntityLiving() == target && state == STATE.ACTIVE) {
//
//            }
        }

        @Override
        public boolean equippedTick(LivingEntity caster, SkillData stats) {
            if (stats.getState() == STATE.ACTIVE && !CombatData.getCap(caster).consumeMight(0.05f)) markUsed(caster);
            return super.equippedTick(caster, stats);
        }
    }

    public static class CrowdPleaser extends Guillotine {
        @Override
        public Color getColor() {
            return Color.MAGENTA;
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
            if (procPoint instanceof SkillCastEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST) {
                stats.setArbitraryFloat(stats.getArbitraryFloat() + 1);
            } else super.onProc(caster, procPoint, state, stats, target);
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            int buff = 0;
            amount = 50f;
            if (CasterData.getCap(caster).getSkillData(this).isPresent())
                buff = (int) CasterData.getCap(caster).getSkillData(this).get().getArbitraryFloat();
//            Skill[] pastCasts = CasterData.getCap(caster).getPastCasts();
//            for (Skill s : pastCasts) {
//                boolean flag = true;
//                for (int find = 0; find < index; find++) {
//                    if (pastCasts[find] == s) {
//                        flag = false;
//                        break;
//                    }
//                }
//                if (flag) buff ++;
//                index++;
//            }
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isAlly(a, caster));
            for (LivingEntity pet : list) {
                pet.addPotionEffect(new EffectInstance(Effects.SPEED, (int) amount * 10, buff));
                if (buff >= 1)
                    pet.addPotionEffect(new EffectInstance(Effects.LUCK, (int) amount * 10, buff - 1));
                if (buff >= 2)
                    pet.addPotionEffect(new EffectInstance(Effects.STRENGTH, (int) amount * 10, buff - 2));
                if (buff >= 3)
                    pet.addPotionEffect(new EffectInstance(Effects.REGENERATION, (int) amount * 10, buff - 3));
                if (buff >= 4)
                    pet.addPotionEffect(new EffectInstance(Effects.RESISTANCE, (int) amount * 10, Math.min(buff - 4, 2)));
            }
        }
    }
}
