package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoint.on_hurt, ProcPoint.recharge_cast, "noRecharge", "melee", "execution")));
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
        setCooldown(caster, 5);
    }

    protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
        CombatData.getCap(caster).addMight(1);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (CombatUtils.getAttackingItemStack(e.getSource()) == null || CombatUtils.getAttackingItemStack(e.getSource()).isEmpty() || CombatUtils.isWeapon(caster, CombatUtils.getAttackingItemStack(e.getSource())))
                if (isValid(e)) {
                    if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                        execute(e);
//                        CombatData.getCap(target).decrementStaggerTime(CombatData.getCap(target).getStaggerTime());
//                        if (e.getAmount() > target.getHealth()) {
                        uponDeath(caster, target, e.getAmount());
                        markUsed(caster);
                        //}
                    } else {
                        e.setCanceled(true);
                        CombatData.getCap(target).consumePosture(e.getAmount());
                    }
                }
        }
    }

    protected void execute(LivingHurtEvent e) {
        e.setAmount(e.getEntityLiving().getHealth());
        e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
    }

    protected boolean isValid(LivingHurtEvent e) {
        return e.getEntityLiving().getHealth() < e.getEntityLiving().getMaxHealth() * 0.3;
    }

    public static class Warning extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        protected boolean isValid(LivingHurtEvent e) {
            return true;
        }

        @Override
        protected void execute(LivingHurtEvent e) {
            e.setAmount(e.getAmount() + (float) CoupDeGrace.getLife(e.getEntityLiving()));
            e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
            CombatData.getCap(e.getEntityLiving()).decrementStaggerTime(CombatData.getCap(e.getEntityLiving()).getStaggerTime());
        }

        @Override
        public void onEffectEnd(LivingEntity caster, SkillData stats) {
            //do nothing! This can be toggled on and off without consequence.
        }
    }

    public static class Silencer extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }

        @Override
        protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
        }

        @Override
        protected boolean isValid(LivingHurtEvent e) {
            return e.getEntityLiving().getHealth() < e.getEntityLiving().getMaxHealth() * 0.3 * CombatUtils.getDamageMultiplier(CombatUtils.AWARENESS.UNAWARE, CombatUtils.getAttackingItemStack(e.getSource()));
        }
    }

    public static class Reinvigorate extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addFatigue(-amount / 10);
            cap.addWounding(-amount / 10);
            cap.addBurnout(-amount / 10);
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
