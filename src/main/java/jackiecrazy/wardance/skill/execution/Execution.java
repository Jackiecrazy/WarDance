package jackiecrazy.wardance.skill.execution;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Execution extends Skill {
    /*
    Consumes 8 might. All damage dealt converted to posture, attacking a staggered target takes one life of damage and ends the state.
Lichtenberg death: attacks will cause some amount of fragility (imagine negative absorption that detonates when taking damage). Attacking a staggered target will take 1+chained targets/(3+chained targets) lives and cause all chained targets to take one life of attacked target/number of chained targets damage (this is actually quite time consuming to implement, but I'm leaving the idea here)
Onslaught: casts heavy blow before every attack (this is a lot easier)
    Crowd Pleaser: allies of the caster gain extra damage, health and attack speed equal to one life/number of allies for 10 seconds
    Endless Might: only consumes 5 might
    Flare: deals two lives' worth of damage, but causes the target to rapidly regenerate 1.4 lives afterwards
    Master's Lesson: while active, might gain is converted into posture at a 1:1 ratio; overflow posture will generate free parries
     */
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", SkillTags.on_hurt, SkillTags.on_cast, "melee", "execution")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Collections.singletonList("execution")));

    private static double getLife(LivingEntity e) {
        if (e instanceof PlayerEntity) return 3;
        return e.getMaxHealth() / Math.max(1, Math.floor(Math.log(e.getMaxHealth())) - 1);
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Execution.class ? null : WarSkills.EXECUTION.get();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isSkillActive(this)) {
            CasterData.getCap(caster).removeActiveSkill(this);
            return true;
        }
        activate(caster, 1);
        CombatData.getCap(caster).consumeMight(8);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    protected void performEffect(LivingEntity caster, LivingEntity target, float amount) {

    }

    protected int getMightNeeded() {
        return 8;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CasterData.getCap(caster).isSkillActive(this)) return CastStatus.ALLOWED;
        CastStatus cs = super.castingCheck(caster);
        if (cs == CastStatus.ALLOWED && !CasterData.getCap(caster).isSkillActive(this) && CombatData.getCap(caster).getMight() < getMightNeeded())
            return CastStatus.OTHER;
        return cs;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                performEffect(caster, target, execute(e));
                markUsed(caster);
            } else {
                e.setCanceled(true);
                CombatData.getCap(target).consumePosture(caster, e.getAmount());
            }
        }
    }

    protected float execute(LivingHurtEvent e) {
        WarDance.LOGGER.debug(e.getEntityLiving().getHealth());
        final float life = (float) getLife(e.getEntityLiving());
        e.getEntityLiving().setHealth(e.getEntityLiving().getHealth() - life);
        e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
        CombatData.getCap(e.getEntityLiving()).decrementStaggerTime(CombatData.getCap(e.getEntityLiving()).getStaggerTime());
        WarDance.LOGGER.debug(e.getEntityLiving().getHealth());
        return e.getAmount();
    }

    public static class Onslaught extends Execution {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public boolean activeTick(LivingEntity caster, SkillData d) {
            if (!CasterData.getCap(caster).isSkillActive(WarSkills.HEAVY_BLOW.get())) {
                CasterData.getCap(caster).getEquippedVariation(WarSkills.HEAVY_BLOW.get()).onCast(caster);
                return true;
            }
            return super.activeTick(caster, d);
        }
    }

    public static class EndlessMight extends Execution {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        public boolean onCast(LivingEntity caster) {
            if (CasterData.getCap(caster).isSkillActive(this)) {
                CasterData.getCap(caster).removeActiveSkill(this);
                return true;
            }
            activate(caster, 100);
            CombatData.getCap(caster).consumeMight(5);
            return true;
        }

        @Override
        protected int getMightNeeded() {
            return 5;
        }
    }

    public static class Flare extends Execution {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        protected float execute(LivingHurtEvent e) {
            WarDance.LOGGER.debug(e.getEntityLiving().getHealth());
            final float lives = (float) getLife(e.getEntityLiving()) * 2;
            e.getEntityLiving().setHealth(e.getEntityLiving().getHealth() - lives);
            //e.setAmount(e.getAmount() + (float) getLife(e.getEntityLiving()) * 2);
            e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
            CombatData.getCap(e.getEntityLiving()).decrementStaggerTime(CombatData.getCap(e.getEntityLiving()).getStaggerTime());
            WarDance.LOGGER.debug(e.getEntityLiving().getHealth());
            return e.getAmount();

        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount) {
            float regen = amount * 0.7f;
            target.addPotionEffect(new EffectInstance(Effects.REGENERATION, (int) (regen * 6), 3));
        }
    }

    public static class MastersLesson extends Execution {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", SkillTags.on_hurt, "melee", "execution", SkillTags.on_parry, SkillTags.change_might)));
        private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Collections.singletonList("execution")));

        @Override
        public Tag<String> getTags(LivingEntity caster) {
            return tag;
        }

        @Override
        public Tag<String> getIncompatibleTags(LivingEntity caster) {
            return no;
        }

        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            super.onSuccessfulProc(caster, stats, target, procPoint);
            if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).canParry() && ((ParryEvent) procPoint).getPostureConsumption() > 0 && stats.getArbitraryFloat() > 1) {
                stats.setArbitraryFloat(stats.getArbitraryFloat() - 1);
                ((ParryEvent) procPoint).setPostureConsumption(0);
            }
            if (procPoint instanceof AttackMightEvent) {
                stats.setArbitraryFloat(stats.getArbitraryFloat() + ((AttackMightEvent) procPoint).getQuantity());
                ((AttackMightEvent) procPoint).setQuantity(0);
            }
        }
    }

    public static class CrowdPleaser extends Execution {
        //TODO buff
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof SkillCastEvent) {
                stats.setArbitraryFloat(stats.getArbitraryFloat() + 1);
            }
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount) {
            int buff = 0;
            if (CasterData.getCap(caster).getActiveSkill(this).isPresent())
                buff = Math.min((int) CasterData.getCap(caster).getActiveSkill(this).get().getArbitraryFloat() / 2, 3);
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
            final List<LivingEntity> list = caster.world.getEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isAlly(a, caster));
            for (LivingEntity pet : list) {
                pet.addPotionEffect(new EffectInstance(Effects.SPEED, (int) amount * 10, buff));
                pet.addPotionEffect(new EffectInstance(Effects.STRENGTH, (int) amount * 10, buff));
                if (buff >= 1)
                    pet.addPotionEffect(new EffectInstance(Effects.LUCK, (int) amount * 10, buff - 1));
                if (buff >= 2)
                    pet.addPotionEffect(new EffectInstance(Effects.REGENERATION, (int) amount * 10, buff - 2));
                if (buff >= 3)
                    pet.addPotionEffect(new EffectInstance(Effects.RESISTANCE, (int) amount * 10, Math.min(buff - 3, 2)));
            }
        }
    }
}
