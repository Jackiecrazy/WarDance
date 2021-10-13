package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.EntityAwarenessEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CoupDeGrace extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", SkillTags.melee, SkillTags.on_hurt, SkillTags.recharge_cast, SkillTags.change_parry_result, "execution")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Collections.singletonList("execution")));

    protected float getDamage(LivingEntity caster, LivingEntity target) {
        return GeneralUtils.getMaxHealthBeforeWounding(target) * (1 - (target.getHealth() / GeneralUtils.getMaxHealthBeforeWounding(target))) * 0.2f;
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
        return this.getClass() == CoupDeGrace.class ? null : WarSkills.COUP_DE_GRACE.get();
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CasterData.getCap(caster).isSkillActive(this))
            return CastStatus.ALLOWED;
        if (CasterData.getCap(caster).isSkillCoolingDown(this))
            return CastStatus.COOLDOWN;
        if (CombatData.getCap(caster).getMight() < mightConsumption(caster))
            return CastStatus.MIGHT;
        return CastStatus.ALLOWED;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 3;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isTagActive("execution"))
            CasterData.getCap(caster).removeActiveTag("execution");
        else {
            activate(caster, 1);
            CombatData.getCap(caster).consumeMight(3);
        }
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 2);
    }

    protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
        if (amount > target.getHealth())
            CombatData.getCap(caster).addMight(3);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
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
                } else {
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
            if (amount > target.getHealth())
                CombatData.getCap(caster).addSpirit(CombatData.getCap(target).getSpirit() / 2);
            CombatData.getCap(target).consumeSpirit(CombatData.getCap(target).getSpirit() / 2);
            FakeExplosion.explode(caster.world, caster, target.getPosX(), target.getPosY(), target.getPosZ(), (float) Math.sqrt(CombatData.getCap(target).getTrueMaxPosture()), new CombatDamageSource("player", caster).setProxy(target).setExplosion().setMagicDamage(), 2 * CombatData.getCap(target).getSpirit());
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
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", SkillTags.melee, SkillTags.change_awareness, SkillTags.on_hurt, SkillTags.recharge_cast, SkillTags.change_parry_result, "execution")));

        @Override
        public Tag<String> getTags(LivingEntity caster) {
            return tag;
        }

        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public boolean onCast(LivingEntity caster) {
            CombatData.getCap(caster).setForcedSweep(360);
            return super.onCast(caster);
        }

        @Override
        public boolean willKillOnCast(LivingEntity caster, LivingEntity target) {
            return target.getHealth() < (GeneralUtils.getMaxHealthBeforeWounding(target) * 0.05f) + GeneralUtils.getAttributeValueSafe(caster, Attributes.ATTACK_DAMAGE);
        }

        @Override
        public void onEffectEnd(LivingEntity caster, SkillData stats) {
            if (!stats.isCondition())
                setCooldown(caster, 5);
            CombatData.getCap(caster).setForcedSweep(-1);
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof LivingHurtEvent) {
                LivingHurtEvent e = (LivingHurtEvent) procPoint;
                if (e.getEntityLiving() == caster) return;
                e.getEntityLiving().setHealth(e.getEntityLiving().getHealth() - GeneralUtils.getMaxHealthBeforeWounding(target) * 0.05f);
                e.setAmount(e.getAmount() * 2);
                if (e.getEntityLiving().getHealth() - e.getAmount() <= 0)
                    stats.flagCondition(true);
            }
            if (procPoint instanceof EntityAwarenessEvent)
                ((EntityAwarenessEvent) procPoint).setAwareness(CombatUtils.Awareness.ALERT);
            if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getAttacker() == caster) {
                procPoint.setResult(Event.Result.DENY);
                target.setLastAttackedEntity(caster);
                markUsed(caster);
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
                if (s.getTags(caster).contains("physical"))
                    isc.coolSkill(s);
            }
        }
    }

}
