package jackiecrazy.wardance.skill.execution;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
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
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoint.on_hurt, "melee", "execution")));
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
        CombatData.getCap(caster).consumeMight(8);
        activate(caster, 1);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    protected void performEffect(LivingEntity caster, LivingEntity target, float amount) {

    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        CastStatus cs = super.castingCheck(caster);
        if (cs == CastStatus.ALLOWED && CombatData.getCap(caster).getMight() < 8) return CastStatus.OTHER;
        return cs;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (CombatUtils.getAttackingItemStack(e.getSource()) == null || CombatUtils.getAttackingItemStack(e.getSource()).isEmpty() || CombatUtils.isWeapon(caster, CombatUtils.getAttackingItemStack(e.getSource())))
                if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                    execute(e);
                    performEffect(caster, target, e.getAmount());
                    markUsed(caster);
                } else {
                    e.setCanceled(true);
                    CombatData.getCap(target).consumePosture(e.getAmount());
                }
        }
    }

    protected void execute(LivingHurtEvent e) {
        e.setAmount(e.getAmount() + (float) getLife(e.getEntityLiving()));
        e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
        CombatData.getCap(e.getEntityLiving()).decrementStaggerTime(CombatData.getCap(e.getEntityLiving()).getStaggerTime());
    }

    public static class EndlessMight extends Execution {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        public boolean onCast(LivingEntity caster) {
            CombatData.getCap(caster).consumeMight(5);
            activate(caster, 1);
            return true;
        }

        @Override
        public CastStatus castingCheck(LivingEntity caster) {
            CastStatus cs = super.castingCheck(caster);
            if (cs == CastStatus.ALLOWED && CombatData.getCap(caster).getMight() < 5) return CastStatus.OTHER;
            return cs;
        }
    }

    public static class Flare extends Execution {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        protected void execute(LivingHurtEvent e) {
            e.setAmount(e.getAmount() + (float) getLife(e.getEntityLiving()) * 2);
            e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
            CombatData.getCap(e.getEntityLiving()).decrementStaggerTime(CombatData.getCap(e.getEntityLiving()).getStaggerTime());
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount) {
            float regen = amount * 0.7f;
            target.addPotionEffect(new EffectInstance(Effects.REGENERATION, (int) (regen * 6), 3));
        }
    }

    public static class MastersLesson extends Execution {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoint.on_hurt, "melee", "execution", ProcPoint.on_parry, ProcPoint.change_might)));
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
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        protected void performEffect(LivingEntity caster, LivingEntity target, float amount) {
            final List<LivingEntity> list = caster.world.getEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(10), (a) -> TargetingUtils.isAlly(a, caster));
            for (LivingEntity pet : list) {
                pet.addPotionEffect(new EffectInstance(Effects.STRENGTH, (int) amount * 20 / list.size()));
                pet.addPotionEffect(new EffectInstance(Effects.SPEED, (int) amount * 20 / list.size()));
            }
        }
    }
}
