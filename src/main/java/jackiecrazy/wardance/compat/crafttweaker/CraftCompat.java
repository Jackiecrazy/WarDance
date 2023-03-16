package jackiecrazy.wardance.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Expansion("crafttweaker.api.entity.MCLivingEntity")
public class CraftCompat {
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
    public boolean isStaggeringStrike(LivingEntity entity) {
        return CombatData.getCap(entity).isStaggeringStrike();
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
        return CombatData.getCap(entity).getRank();
    }

    @ZenCodeType.Method
    public void setCombo(LivingEntity entity, float amount) {
        CombatData.getCap(entity).setRank(amount);
    }

    @ZenCodeType.Method
    public float addCombo(LivingEntity entity, float amount) {
        return CombatData.getCap(entity).addRank(amount);
    }

    @ZenCodeType.Method
    public boolean consumeCombo(LivingEntity entity, float amount, float above) {
        return CombatData.getCap(entity).consumeRank(amount, above);
    }

    @ZenCodeType.Method
    public float getMaxMight(LivingEntity entity) {
        return CombatData.getCap(entity).getMaxMight();
    }

    @ZenCodeType.Method
    public int getStaggerTime(LivingEntity entity) {
        return CombatData.getCap(entity).getStaggerTime();
    }

    @ZenCodeType.Method
    public void stagger(LivingEntity entity, int amount) {
        CombatData.getCap(entity).stagger(amount);
    }

    @ZenCodeType.Method
    public int decrementStaggerTime(LivingEntity entity, int amount) {
        return CombatData.getCap(entity).decrementStaggerTime(amount);
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
    public int getHandBind(LivingEntity entity, InteractionHand h) {
        return CombatData.getCap(entity).getHandBind(h);
    }

    @ZenCodeType.Method
    public void setHandBind(LivingEntity entity, InteractionHand h, int amount) {
        CombatData.getCap(entity).setHandBind(h, amount);
    }

    @ZenCodeType.Method
    public void decrementHandBind(LivingEntity entity, InteractionHand h, int amount) {
        CombatData.getCap(entity).decrementHandBind(h, amount);
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
}
