package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CoupDeGrace extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "afterArmor", "noRecharge", "melee", "execution")));
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
        return this.getClass() == CoupDeGrace.class ? null : WarSkills.COUP_DE_GRACE.get();
    }

    public boolean canCast(LivingEntity caster) {
//        ISkillCapability cap = CasterData.getCap(caster);
//        if (cap.isSkillActive(this)) return true;
//        for (String s : getIncompatibleTags(caster).getAllElements())
//            if (cap.isTagActive(s)) return false;
//        return !cap.isSkillCoolingDown(this) && (getParentSkill() == null || getParentSkill().canCast(caster));
        return true;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isTagActive("execution"))
            CasterData.getCap(caster).removeActiveTag("execution");
        else activate(caster, 1);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        CasterData.getCap(caster).removeActiveTag("execution");
    }

    protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
        CombatData.getCap(caster).addMight(1);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingDamageEvent) {
            LivingDamageEvent e = (LivingDamageEvent) procPoint;
            if (CombatUtils.getAttackingItemStack(e.getSource()) == null || CombatUtils.getAttackingItemStack(e.getSource()).isEmpty() || CombatUtils.isWeapon(caster, CombatUtils.getAttackingItemStack(e.getSource())))
                if (isValid(e)) {
                    e.setAmount(e.getAmount() + (float) CoupDeGrace.getLife(target));
                    CombatData.getCap(target).decrementStaggerTime(CombatData.getCap(target).getStaggerTime());
                    if (e.getAmount() > target.getHealth()) {
                        uponDeath(caster, target, e.getAmount());
                    }
                }
        }
    }

    protected boolean isValid(LivingDamageEvent e) {
        return CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike();
    }

    public static class Silencer extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        protected boolean isValid(LivingDamageEvent e) {
            if (e.getSource().getTrueSource() instanceof LivingEntity && CombatUtils.getAwareness((LivingEntity) e.getSource().getTrueSource(), e.getEntityLiving()) == CombatUtils.AWARENESS.UNAWARE)
                return true;
            return super.isValid(e);
        }
    }

    public static class Reinvigorate extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addFatigue( Math.max(-amount / 10, -cap.getFatigue()*0.3f));
            cap.addWounding(Math.max(-amount / 10, -cap.getWounding()*0.3f));
            cap.addBurnout(Math.max(-amount / 10, -cap.getBurnout()*0.3f));
            cap.addPosture(amount / 10);
            cap.addSpirit(amount / 10);
            caster.heal(amount / 10);
        }
    }

    public static class Frenzy extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
            ISkillCapability isc = CasterData.getCap(caster);
            final Set<Skill> skills = new HashSet<>(isc.getSkillCooldowns().keySet());
            for (Skill s : skills) {
                if (s.getTags(caster).contains("physical"))
                    isc.coolSkill(s);
            }
        }
    }

}
