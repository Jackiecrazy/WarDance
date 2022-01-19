package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.EntityAwarenessEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CoupDeGrace extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.normal_attack, ProcPoints.on_hurt, ProcPoints.recharge_cast, ProcPoints.change_parry_result, "execution")));

    protected float getDamage(LivingEntity caster, LivingEntity target) {
        return (GeneralUtils.getMaxHealthBeforeWounding(target) - target.getHealth()) * 0.2f;
    }

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

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.coup_de_grace;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CasterData.getCap(caster).isSkillActive(this))
            return CastStatus.ALLOWED;
        return super.castingCheck(caster);
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 3;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isTagActive("special"))
            CasterData.getCap(caster).removeActiveTag("special");
        else {
            activate(caster, 1);
            CombatData.getCap(caster).consumeMight(mightConsumption(caster));
        }
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 2);
    }

    protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
        if (amount > target.getHealth())
            CombatData.getCap(caster).addMight(6);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
        if (procPoint instanceof LivingHurtEvent) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (e.getEntityLiving() != caster) {
                if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                    e.setAmount(e.getAmount() + getDamage(caster, target));
                    e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
                    CombatData.getCap(target).decrementStaggerTime(CombatData.getCap(target).getStaggerTime());
                    deathCheck(caster, target, e.getAmount());
                    markUsed(caster);
                    //}
                } else if (target.getHealth() < getDamage(caster, target)) {
                    e.setCanceled(true);
                    CombatData.getCap(target).consumePosture(caster, e.getAmount());
                }
            }
        }
    }

    public boolean willKillOnCast(LivingEntity caster, LivingEntity target) {
        return target.getHealth() < getDamage(caster, target);
    }

    public static class Rupture extends CoupDeGrace {
        //detonate the entire spirit bar and leech half. Size of explosion is determined by size of mob, damage is determined by spirit.
        //danse macabre: proc percentage scales with combo
        //reaping: deal weapon damage*2+5% max health in a wide area. Enemies below that line take true damage, and skill refreshes on any death
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            CombatData.getCap(target).consumeSpirit(CombatData.getCap(target).getSpirit() / 2);
            FakeExplosion.explode(caster.world, caster, target.getPosX(), target.getPosY(), target.getPosZ(), (float) Math.sqrt(CombatData.getCap(target).getTrueMaxPosture()), new CombatDamageSource("explosion.player", caster).setProxy(target).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setExplosion().setMagicDamage(), 4 * CombatData.getCap(target).getSpirit());
        }
    }

    public static class DanseMacabre extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        protected float getDamage(LivingEntity caster, LivingEntity target) {
            return GeneralUtils.getMaxHealthBeforeWounding(target) * (1 - (target.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(target))) * (0.2f + 0.2f * (CombatData.getCap(caster).getCombo() / 10));
        }
    }

    public static class Reaping extends CoupDeGrace {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.change_awareness, ProcPoints.on_hurt, ProcPoints.recharge_cast, ProcPoints.change_parry_result, "execution")));
        private final Tag<String> tague = Tag.getTagFromContents(new HashSet<>(Arrays.asList(SkillTags.special, SkillTags.offensive)));

        @Override
        public Tag<String> getTags(LivingEntity caster) {
            return tague;
        }

        @Override
        public Tag<String> getProcPoints(LivingEntity caster) {
            return tag;
        }

        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public boolean onCast(LivingEntity caster) {
            CombatData.getCap(caster).setForcedSweep(360);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_RAVAGER_CELEBRATE, SoundCategory.PLAYERS, 0.8f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            return super.onCast(caster);
        }

        @Override
        public float mightConsumption(LivingEntity caster) {
            return 0;
        }

        @Override
        public boolean willKillOnCast(LivingEntity caster, LivingEntity target) {
            return target.getHealth() < (GeneralUtils.getMaxHealthBeforeWounding(target) * 0.10f);
        }

        @Override
        public void onEffectEnd(LivingEntity caster, SkillData stats) {
            if (!stats.isCondition())
                setCooldown(caster, 5);
            CombatData.getCap(caster).setForcedSweep(-1);
        }

        @Override
        public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
            if (procPoint instanceof LivingHurtEvent) {
                LivingHurtEvent e = (LivingHurtEvent) procPoint;
                if (e.getEntityLiving() == caster) return;
                if (e.getEntityLiving().getHealth() < GeneralUtils.getMaxHealthBeforeWounding(target) * 0.10f+e.getAmount()) {
                    stats.flagCondition(true);
                    e.getSource().setDamageIsAbsolute().setDamageBypassesArmor();
                }
                e.setAmount(e.getAmount()+GeneralUtils.getMaxHealthBeforeWounding(target) * 0.10f);
            }
            if (procPoint instanceof EntityAwarenessEvent)
                ((EntityAwarenessEvent) procPoint).setAwareness(CombatUtils.Awareness.ALERT);
            if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getAttacker() == caster) {
                procPoint.setResult(Event.Result.DENY);
                target.setLastAttackedEntity(caster);
                markUsed(caster);
            }
            if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
                ((LivingAttackEvent) procPoint).getSource().setDamageIsAbsolute();
            }
        }
    }

    public static class Reinvigorate extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addFatigue(-1);
            cap.addWounding(-1);
            cap.addBurnout(-1);
        }
    }

    public static class Frenzy extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            ISkillCapability isc = CasterData.getCap(caster);
            final Set<Skill> skills = new HashSet<>(isc.getSkillCooldowns().keySet());
            for (Skill s : skills) {
                if (s.getProcPoints(caster).contains("physical"))
                    isc.coolSkill(s);
            }
        }
    }

}
