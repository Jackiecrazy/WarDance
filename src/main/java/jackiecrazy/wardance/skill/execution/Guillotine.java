package jackiecrazy.wardance.skill.execution;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.GainMightEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Guillotine extends Skill {
    /*
    Giantslayer, Giantkiller, Decimation
    Disables posture regeneration while active, ending if you are staggered. Costs 40% of lost posture as fatigue. All damage dealt converted to posture, attacking a staggered target takes one life of damage and ends the state.
Lichtenberg death: attacks will cause some amount of fragility (imagine negative absorption that detonates when taking damage). Attacking a staggered target will take 1+chained targets/(3+chained targets) lives and cause all chained targets to take one life of attacked target/number of chained targets damage (this is actually quite time consuming to implement, but I'm leaving the idea here)
Onslaught: casts heavy blow before every attack (this is a lot easier)
    Crowd Pleaser: allies of the caster gain extra damage, health and attack speed equal to one life/number of allies for 10 seconds
    Endless Might: only consumes 5 might
    Flare: deals two lives' worth of damage, but causes the target to rapidly regenerate 1.4 lives afterwards
    Master's Lesson: while active, might gain is converted into posture at a 1:1 ratio; overflow posture will generate free parries
     */
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.on_hurt, ProcPoints.on_stagger, ProcPoints.change_posture_regeneration, ProcPoints.on_cast, ProcPoints.recharge_cast, "melee", "execution")));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return special;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return special;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Guillotine.class ? null : WarSkills.GUILLOTINE.get();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isSkillActive(this)) {
            CasterData.getCap(caster).removeActiveSkill(this);
            return true;
        }
        activate(caster, 1);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        CombatData.getCap(caster).addBurnout(CombatData.getCap(caster).getMaxSpirit() / 5);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
        CombatData.getCap(target).addWounding(amount);
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CasterData.getCap(caster).isSkillActive(this))
            return CastStatus.ALLOWED;
        if (CombatData.getCap(caster).getSpirit() != CombatData.getCap(caster).getMaxSpirit()) return CastStatus.OTHER;
        return super.castingCheck(caster);
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (!CombatData.getCap(caster).consumeSpirit(0.05f))
            markUsed(caster);
        return super.activeTick(caster, d);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (e.getEntityLiving() == caster) return;
            if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                performEffect(caster, target, execute(e), stats);
                markUsed(caster);
            } else {
                e.setCanceled(true);
                absorbDamage(stats, e.getAmount());
                CombatData.getCap(target).consumePosture(caster, e.getAmount());
            }
        } else if (procPoint instanceof GainMightEvent) {
            stats.setArbitraryFloat(stats.getArbitraryFloat() + ((GainMightEvent) procPoint).getQuantity());
            //((GainMightEvent) procPoint).setQuantity(0);
        }
    }

    protected void absorbDamage(SkillData s, float a) {

    }

    protected float execute(LivingHurtEvent e) {
        final float life = e.getEntityLiving().getHealth() / 5;
        if (this != WarSkills.GUILLOTINE.get())
            e.getEntityLiving().setHealth(e.getEntityLiving().getHealth() - life);
        e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
        CombatData.getCap(e.getEntityLiving()).decrementStaggerTime(CombatData.getCap(e.getEntityLiving()).getStaggerTime());
        return life + e.getAmount();
    }

    public static class Brutalize extends Guillotine {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> !TargetingUtils.isAlly(a, caster));
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
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof GainMightEvent) {
                ((GainMightEvent) procPoint).setQuantity(0);
            } else super.onSuccessfulProc(caster, stats, target, procPoint);
        }

        protected void absorbDamage(SkillData s, float a) {
            s.setArbitraryFloat(s.getArbitraryFloat() + a);
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            CombatDamageSource cds = new CombatDamageSource("lightningBolt", caster).setProxy(target);
            final float radius = 2 + s.getArbitraryFloat() / 5;
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(radius), (a) -> !TargetingUtils.isAlly(a, caster));
            //float damage = s.getArbitraryFloat() * (1 + CombatData.getCap(caster).getSpirit());
            float damage = 5 + s.getArbitraryFloat() * CombatData.getCap(caster).getSpirit() / list.size();
            CombatData.getCap(caster).setSpirit(0);
            for (LivingEntity baddie : list) {
                if (baddie == target) continue;
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
            return Color.GRAY;
        }

        @Override
        public boolean activeTick(LivingEntity caster, SkillData d) {
            if (caster.ticksExisted % 10 == 0) {
                final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(7), (a) -> !TargetingUtils.isAlly(a, caster) && !Marks.getCap(a).isMarked(this));
                for (LivingEntity enemy : list) {
                    if (Marks.getCap(enemy).isMarked(this))
                        continue;
                    enemy.addPotionEffect(new EffectInstance(WarEffects.DISTRACTION.get(), 140));
                    mark(caster, enemy, 10);
                    break;
                }
            }
            return super.activeTick(caster, d);
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            SkillUtils.createCloud(caster.world, caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(), 15, ParticleTypes.LARGE_SMOKE);
            final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(14), (a) -> !TargetingUtils.isAlly(a, caster));
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
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.on_hurt, ProcPoints.normal_attack, ProcPoints.on_stagger, ProcPoints.change_might, ProcPoints.on_cast, "melee", "execution")));

        @Override
        public Tag<String> getProcPoints(LivingEntity caster) {
            return tag;
        }

        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof StaggerEvent && !procPoint.isCanceled()) {
                ((StaggerEvent) procPoint).setCount(1);
                ((StaggerEvent) procPoint).setLength(200);
            } else if (procPoint instanceof LivingAttackEvent && CombatData.getCap(target).getStaggerTime() > 0) {
                procPoint.setCanceled(true);
            } else super.onSuccessfulProc(caster, stats, target, procPoint);
        }
    }

    public static class CrowdPleaser extends Guillotine {
        @Override
        public Color getColor() {
            return Color.MAGENTA;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof SkillCastEvent) {
                stats.setArbitraryFloat(stats.getArbitraryFloat() + 1);
            } else super.onSuccessfulProc(caster, stats, target, procPoint);
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount, SkillData s) {
            int buff = 0;
            if (CasterData.getCap(caster).getActiveSkill(this).isPresent())
                buff = (int) CasterData.getCap(caster).getActiveSkill(this).get().getArbitraryFloat();
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
