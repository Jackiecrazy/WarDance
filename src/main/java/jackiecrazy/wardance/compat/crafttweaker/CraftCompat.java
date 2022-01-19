package jackiecrazy.wardance.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Expansion("crafttweaker.api.entity.MCLivingEntity")
public class CraftCompat {
    @ZenCodeType.Method
    public static float getResolve(LivingEntity entity) {
        return CombatData.getCap(entity).getResolve();
    }

    @ZenCodeType.Method
    public static void setResolve(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setResolve(amount);
    }

    @ZenCodeType.Method
    public static float getMight(LivingEntity entity) {
        return CombatData.getCap(entity).getMight();
    }

    @ZenCodeType.Method
    public static void setMight(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setMight(amount);
    }

    @ZenCodeType.Method
    public static float addMight(LivingEntity entity, float amount) {
        return CombatData.getCap(entity).addMight(amount);
    }

    @ZenCodeType.Method
    public static boolean consumeMight(LivingEntity entity, float amount, float above) {
        return CombatData.getCap(entity).consumeMight(amount, above);
    }

    @ZenCodeType.Method
    public int getMightGrace(LivingEntity entity) {
        return CombatData.getCap(entity).getMightGrace();
    }

    @ZenCodeType.Method
    public void setMightGrace(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setMightGrace(amount);
    }

    @ZenCodeType.Method
    public int decrementMightGrace(LivingEntity entity, int amount) {
        return CombatData.getCap(entity).decrementMightGrace(amount);
    }

    @ZenCodeType.Method
    public float getSpirit(LivingEntity entity) {
        return CombatData.getCap(entity).getSpirit();
    }

    @ZenCodeType.Method
    public void setSpirit(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setSpirit(amount);
    }

    @ZenCodeType.Method
    public float addSpirit(LivingEntity entity, float amount) {
        return CombatData.getCap(entity).addSpirit(amount);
    }

    @ZenCodeType.Method
    public boolean consumeSpirit(LivingEntity entity, float amount, float above) {
        return CombatData.getCap(entity).consumeSpirit(amount, above);
    }

    @ZenCodeType.Method
    public int getSpiritGrace(LivingEntity entity) {
        return CombatData.getCap(entity).getMightGrace();
    }

    @ZenCodeType.Method
    public void setSpiritGrace(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setSpiritGrace(amount);
    }

    @ZenCodeType.Method
    public int decrementSpiritGrace(LivingEntity entity, int amount) {
        return CombatData.getCap(entity).decrementSpiritGrace(amount);
    }

    @ZenCodeType.Method
    public float getPosture(LivingEntity entity) {
        return CombatData.getCap(entity).getPosture();
    }

    @ZenCodeType.Method
    public void setPosture(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setPosture(amount);
    }

    @ZenCodeType.Method
    public float addPosture(LivingEntity entity, float amount) {
        return CombatData.getCap(entity).addPosture(amount);
    }

    @ZenCodeType.Method
    public boolean isFirstStaggerStrike(LivingEntity entity) {
        return CombatData.getCap(entity).isFirstStaggerStrike();
    }

    @ZenCodeType.Method
    public float consumePosture(LivingEntity entity, LivingEntity assailant, float amount, float above, boolean force) {
        return CombatData.getCap(entity).consumePosture(assailant, amount, above, force);
    }

    @ZenCodeType.Method
    public int getPostureGrace(LivingEntity entity) {
        return CombatData.getCap(entity).getPostureGrace();
    }

    @ZenCodeType.Method
    public void setPostureGrace(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setPostureGrace(amount);
    }

    @ZenCodeType.Method
    public int decrementPostureGrace(LivingEntity entity, int amount) {
        return CombatData.getCap(entity).decrementPostureGrace(amount);
    }

    @ZenCodeType.Method
    public float getCombo(LivingEntity entity) {
        return CombatData.getCap(entity).getCombo();
    }

    @ZenCodeType.Method
    public void setCombo(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setCombo(amount);
    }

    @ZenCodeType.Method
    public float addCombo(LivingEntity entity, float amount) {
        return CombatData.getCap(entity).addCombo(amount);
    }

    @ZenCodeType.Method
    public boolean consumeCombo(LivingEntity entity, float amount, float above) {
        return CombatData.getCap(entity).consumeCombo(amount, above);
    }

    @ZenCodeType.Method
    public float getTrueMaxPosture(LivingEntity entity) {
        return CombatData.getCap(entity).getTrueMaxPosture();
    }

    @ZenCodeType.Method
    public void setTrueMaxPosture(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setTrueMaxPosture(amount);
    }

    @ZenCodeType.Method
    public float getTrueMaxSpirit(LivingEntity entity) {
        return CombatData.getCap(entity).getTrueMaxSpirit();
    }

    @ZenCodeType.Method
    public void setTrueMaxSpirit(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setTrueMaxSpirit(amount);
    }

    @ZenCodeType.Method
    public float getMaxMight(LivingEntity entity) {
        return CombatData.getCap(entity).getMaxMight();
    }

    @ZenCodeType.Method
    public void setMaxMight(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setMaxMight(amount);
    }

    @ZenCodeType.Method
    public int getComboGrace(LivingEntity entity) {
        return CombatData.getCap(entity).getComboGrace();
    }

    @ZenCodeType.Method
    public void setComboGrace(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setComboGrace(amount);
    }

    @ZenCodeType.Method
    public int decrementComboGrace(LivingEntity entity, int amount) {
        return CombatData.getCap(entity).decrementComboGrace(amount);
    }

    @ZenCodeType.Method
    public int getStaggerTime(LivingEntity entity) {
        return CombatData.getCap(entity).getStaggerTime();
    }

    @ZenCodeType.Method
    public void setStaggerTime(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setStaggerTime(amount);
    }

    @ZenCodeType.Method
    public int decrementStaggerTime(LivingEntity entity, int amount) {
        return CombatData.getCap(entity).decrementStaggerTime(amount);
    }

    @ZenCodeType.Method
    public int getStaggerCount(LivingEntity entity) {
        return CombatData.getCap(entity).getStaggerCount();
    }

    @ZenCodeType.Method
    public void setStaggerCount(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setStaggerCount(amount);
    }

    @ZenCodeType.Method
    public void decrementStaggerCount(LivingEntity entity, int amount) {
        CombatData.getCap(entity).decrementStaggerCount(amount);
    }

    @ZenCodeType.Method
    public int getShieldTime(LivingEntity entity) {
        return CombatData.getCap(entity).getShieldTime();
    }

    @ZenCodeType.Method
    public void setShieldTime(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setShieldTime(amount);
    }

    @ZenCodeType.Method
    public void decrementShieldTime(LivingEntity entity, int amount) {
        CombatData.getCap(entity).decrementShieldTime(amount);
    }

    @ZenCodeType.Method
    public int getShieldCount(LivingEntity entity) {
        return CombatData.getCap(entity).getShieldCount();
    }

    @ZenCodeType.Method
    public void setShieldCount(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setShieldCount(amount);
    }

    @ZenCodeType.Method
    public void decrementShieldCount(LivingEntity entity, int amount) {
        CombatData.getCap(entity).decrementShieldCount(amount);
    }

    @ZenCodeType.Method
    public int getOffhandCooldown(LivingEntity entity) {
        return CombatData.getCap(entity).getOffhandCooldown();
    }

    @ZenCodeType.Method
    public void setOffhandCooldown(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setOffhandCooldown(amount);
    }

    @ZenCodeType.Method
    public void addOffhandCooldown(LivingEntity entity, int amount) {
        CombatData.getCap(entity).addOffhandCooldown(amount);
    }

    @ZenCodeType.Method
    public int getRollTime(LivingEntity entity) {
        return CombatData.getCap(entity).getRollTime();
    }

    @ZenCodeType.Method
    public void setRollTime(LivingEntity entity, int amount) {
        CombatData.getCap(entity).setRollTime(amount);
    }

    @ZenCodeType.Method
    public void decrementRollTime(LivingEntity entity, int amount) {
        CombatData.getCap(entity).decrementRollTime(amount);
    }

    @ZenCodeType.Method
    public boolean isCombatMode(LivingEntity entity) {
        return CombatData.getCap(entity).isCombatMode();
    }

    @ZenCodeType.Method
    public void toggleCombatMode(LivingEntity entity, boolean on) {
        CombatData.getCap(entity).toggleCombatMode(on);
    }

    @ZenCodeType.Method
    public float getWounding(LivingEntity entity) {
        return CombatData.getCap(entity).getWounding();
    }

    @ZenCodeType.Method
    public void setWounding(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setWounding(amount);
    }

    @ZenCodeType.Method
    public float getFatigue(LivingEntity entity) {
        return CombatData.getCap(entity).getFatigue();
    }

    @ZenCodeType.Method
    public void setFatigue(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setFatigue(amount);
    }

    @ZenCodeType.Method
    public float getBurnout(LivingEntity entity) {
        return CombatData.getCap(entity).getBurnout();
    }

    @ZenCodeType.Method
    public void setBurnout(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setBurnout(amount);
    }

    @ZenCodeType.Method
    public void addWounding(LivingEntity entity, float amount) {
        CombatData.getCap(entity).addWounding(amount);
    }

    @ZenCodeType.Method
    public void addFatigue(LivingEntity entity, float amount) {
        CombatData.getCap(entity).addFatigue(amount);
    }

    @ZenCodeType.Method
    public void addBurnout(LivingEntity entity, float amount) {
        CombatData.getCap(entity).addBurnout(amount);
    }

    @ZenCodeType.Method
    public int getHandBind(LivingEntity entity, Hand h) {
        return CombatData.getCap(entity).getHandBind(h);
    }

    @ZenCodeType.Method
    public void setHandBind(LivingEntity entity, Hand h, int amount) {
        CombatData.getCap(entity).setHandBind(h, amount);
    }

    @ZenCodeType.Method
    public void decrementHandBind(LivingEntity entity, Hand h, int amount) {
        CombatData.getCap(entity).decrementHandBind(h, amount);
    }

    @ZenCodeType.Method
    public float getHandReel(LivingEntity entity, Hand hand) {
        return CombatData.getCap(entity).getHandReel(hand);
    }

    @ZenCodeType.Method
    public void setHandReel(LivingEntity entity, Hand hand, float value) {
        CombatData.getCap(entity).setHandReel(hand, value);
    }

    @ZenCodeType.Method
    public int getShatterCooldown(LivingEntity entity) {
        return CombatData.getCap(entity).getShatterCooldown();
    }

    @ZenCodeType.Method
    public void setShatterCooldown(LivingEntity entity, int value) {
        CombatData.getCap(entity).setShatterCooldown(value);
    }

    @ZenCodeType.Method
    public int decrementShatterCooldown(LivingEntity entity, int value) {
        return CombatData.getCap(entity).decrementShatterCooldown(value);
    }

    @ZenCodeType.Method
    public boolean isSkillSelectable(LivingEntity entity, String s) {
        if (Skill.getSkill(s) == null) return false;
        return CasterData.getCap(entity).isSkillSelectable(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public void setSkillSelectable(LivingEntity entity, String s, boolean selectable) {
        if (Skill.getSkill(s) != null)
            CasterData.getCap(entity).setSkillSelectable(Skill.getSkill(s), selectable);
    }

    @ZenCodeType.Method
    public void removeActiveSkill(LivingEntity entity, String s) {
        if (Skill.getSkill(s) != null)
            CasterData.getCap(entity).removeActiveSkill(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public void clearActiveSkills(LivingEntity entity) {
        CasterData.getCap(entity).clearActiveSkills();
    }

    @ZenCodeType.Method
    public boolean isSkillActive(LivingEntity entity, String s) {
        if (Skill.getSkill(s) == null) return false;
        return CasterData.getCap(entity).isSkillActive(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public boolean isTagActive(LivingEntity entity, String s) {
        if (Skill.getSkill(s) == null) return false;
        return CasterData.getCap(entity).isSkillSelectable(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public void removeActiveTag(LivingEntity entity, String s) {
        CasterData.getCap(entity).removeActiveTag(s);
    }

    @ZenCodeType.Method
    public void markSkillUsed(LivingEntity e, String s) {
        if (Skill.getSkill(s) != null)
            CasterData.getCap(e).markSkillUsed(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public void setSkillCooldown(LivingEntity entity, String s, float amount) {
        if (Skill.getSkill(s) != null)
            CasterData.getCap(entity).setSkillCooldown(Skill.getSkill(s), amount);
    }

    @ZenCodeType.Method
    public boolean isSkillCoolingDown(LivingEntity entity, String s) {
        if (Skill.getSkill(s) == null) return false;
        return CasterData.getCap(entity).isSkillCoolingDown(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public void decrementSkillCooldown(LivingEntity entity, String s, float amount) {
        if (Skill.getSkill(s) != null)
            CasterData.getCap(entity).decrementSkillCooldown(Skill.getSkill(s), amount);
    }

    @ZenCodeType.Method
    public void coolSkill(LivingEntity entity, String s) {
        if (Skill.getSkill(s) != null)
            CasterData.getCap(entity).coolSkill(Skill.getSkill(s));
    }

    @ZenCodeType.Method
    public float getSkillCooldown(LivingEntity entity, String s) {
        if (Skill.getSkill(s) == null) return 0;
        return CasterData.getCap(entity).getSkillCooldown(Skill.getSkill(s)).getDuration();
    }

    @ZenCodeType.Method
    public void clearSkillCooldowns(LivingEntity entity) {
        CasterData.getCap(entity).clearSkillCooldowns();
    }
}
