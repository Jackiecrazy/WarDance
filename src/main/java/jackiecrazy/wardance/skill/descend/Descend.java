package jackiecrazy.wardance.skill.descend;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Descend extends Skill {
    /*
    Descend
Can only be cast on the ground. Until you next touch the ground, your attack will add (height difference*2) posture damage to the target.
Upgrades
Hawk dive: can be used in midair, you will automatically drop out of elytra flight when you activate this.
Lights out: If this hit staggers the foe, blind them and greatly lengthen the stagger time, but set stagger count to 1.
Shockwave: Upon landing without proc, distribute fall distance as posture damage to entities in a 4 block radius
Crush: Converts own posture into extra posture damage applied increased with heavier armor classes. Knocks down whoever's posture empties first.
Assassinate: Stab rank increased by 1 for this attack, instantly stagger a distracted or unaware enemy.
     */

    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", SkillTags.melee, SkillTags.on_stagger, SkillTags.change_awareness, "boundCast", SkillTags.normal_attack, SkillTags.recharge_normal, SkillTags.change_parry_result)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @SubscribeEvent
    public static void noFall(LivingFallEvent e) {
        CasterData.getCap(e.getEntityLiving()).getActiveSkill(CasterData.getCap(e.getEntityLiving()).getEquippedVariation(WarSkills.DESCEND.get())).ifPresent((d) -> {
            if (CasterData.getCap(e.getEntityLiving()).isSkillActive(WarSkills.SHOCKWAVE.get())) {
                WarSkills.SHOCKWAVE.get().onSuccessfulProc(e.getEntityLiving(), CasterData.getCap(e.getEntityLiving()).getActiveSkill(WarSkills.SHOCKWAVE.get()).get(), null, null);
            }
            e.setCanceled(d.isCondition());
        });
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
        return this.getClass() == Descend.class ? null : WarSkills.DESCEND.get();
    }


    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 4;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (!canCast(caster)) return CastStatus.OTHER;
        return super.castingCheck(caster);
    }

    protected boolean canCast(LivingEntity caster) {
        return caster.isOnGround();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, (float) caster.getPosY());
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        return true;
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        //lasts until player touch ground again
        if (d.isCondition() && d.getDuration() > caster.getPosY() && caster.isOnGround()) {
            onSuccessfulProc(caster, d, null, null);
            markUsed(caster);
            return true;
        } else if (!caster.isOnGround())
            d.flagCondition(true);
        return super.activeTick(caster, d);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 6);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        double posDiff = stats.getDuration() - caster.getPosY();
        if (posDiff > 0 && procPoint instanceof ParryEvent) {
            spooketh(caster, target, (float) posDiff);
            caster.fallDistance = 0;
            stats.flagCondition(true);
            markUsed(caster);
        }

    }

    @Override
    public boolean onCooldownProc(LivingEntity caster, SkillCooldownData stats, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            int recharge = 1;
            if (CombatUtils.getAwareness(caster, ((LivingAttackEvent) procPoint).getEntityLiving()) != CombatUtils.Awareness.ALERT)
                recharge += 1;
            if (CombatUtils.getAwareness(caster, ((LivingAttackEvent) procPoint).getEntityLiving()) == CombatUtils.Awareness.UNAWARE)
                recharge += 2;
            stats.decrementDuration(recharge);
            return true;
        }
        return false;
    }

    protected void spooketh(LivingEntity caster, LivingEntity target, float posDiff) {
        CombatData.getCap(target).consumePosture(caster, posDiff * 2, 0, true);
        if (getParentSkill() == null) target.attackEntityFrom(DamageSource.FALLING_BLOCK, posDiff);
    }

    public static class LightsOut extends Descend {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof StaggerEvent && !procPoint.isCanceled()) {
                ((StaggerEvent) procPoint).setLength(200);
                ((StaggerEvent) procPoint).setCount(1);
                target.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 200));
            }
            super.onSuccessfulProc(caster, stats, target, procPoint);
        }
    }

    public static class Shockwave extends Descend {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }


        @Override
        public boolean onCast(LivingEntity caster) {
            return super.onCast(caster);
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint == null) {
                double posDiff = stats.getDuration() - caster.getPosY();
                for (LivingEntity e : caster.world.getEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(5))) {
                    if (e != caster) {
                        CombatData.getCap(e).consumePosture((float) posDiff);
                        e.addPotionEffect(new EffectInstance(WarEffects.EXHAUSTION.get(), 40 + ResourceConfig.postureCD, 1));
                    }
                }
            }
        }

    }

    public static class TitansFall extends Descend {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        protected void spooketh(LivingEntity caster, LivingEntity target, float posDiff) {
            posDiff += CombatData.getCap(caster).getPosture();
            posDiff *= (1 + caster.getTotalArmorValue() / 20f);
            CombatData.getCap(caster).setPosture(0.1f);
            CombatData.getCap(target).consumePosture(caster, posDiff, 0, true);
        }
    }
}
