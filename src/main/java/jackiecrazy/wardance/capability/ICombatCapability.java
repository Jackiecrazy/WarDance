package jackiecrazy.wardance.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

public interface ICombatCapability {
    //might, spirit, posture, combo
    //might cooldown, spirit cooldown, posture cooldown, combo grace period
    //stagger timer, stagger counter for downed attacks
    //offhand cooldown, shield parry time, sidestep/roll timer
    //set, get, increment/decrement, consume (resource only)
    //is offhand attack, combat mode
    //TODO skills
    float getMight();

    void setMight(float amount);

    float addMight(float amount);

    default boolean consumeMight(float amount) {
        return consumeMight(amount, 0);
    }

    boolean consumeMight(float amount, float above);

    int getMightGrace();

    void setMightGrace(int amount);

    int decrementMightGrace(int amount);

    float getSpirit();

    void setSpirit(float amount);

    float addSpirit(float amount);

    default boolean consumeSpirit(float amount) {
        return consumeSpirit(amount, 0);
    }

    boolean consumeSpirit(float amount, float above);

    int getSpiritGrace();

    void setSpiritGrace(int amount);

    int decrementSpiritGrace(int amount);

    float getPosture();

    void setPosture(float amount);

    float addPosture(float amount);

    default boolean doConsumePosture(float amount) {
        return consumePosture(amount, 0) == 0;
    }

    default float consumePosture(float amount) {
        return consumePosture(amount, 0);
    }

    /**
     * returns -1 if staggered
     */
    float consumePosture(float amount, float above);

    int getPostureGrace();

    void setPostureGrace(int amount);

    int decrementPostureGrace(int amount);

    float getCombo();

    void setCombo(float amount);

    float addCombo(float amount);

    default boolean consumeCombo(float amount) {
        return consumeCombo(amount, 0);
    }

    boolean consumeCombo(float amount, float above);

    float getTrueMaxPosture();

    void setTrueMaxPosture(float amount);

    float getTrueMaxSpirit();

    void setTrueMaxSpirit(float amount);

    default float getMaxPosture() {
        return Math.max(0.1f, getTrueMaxPosture() - getFatigue());
    }

    default float getMaxSpirit() {
        return Math.max(0.1f, getTrueMaxSpirit() - getBurnout());
    }

    int getComboGrace();

    void setComboGrace(int amount);

    int decrementComboGrace(int amount);

    int getStaggerTime();

    void setStaggerTime(int amount);

    int decrementStaggerTime(int amount);

    int getStaggerCount();

    void setStaggerCount(int amount);

    void decrementStaggerCount(int amount);

    int getShieldTime();

    void setShieldTime(int amount);

    void decrementShieldTime(int amount);

    int getShieldCount();

    void setShieldCount(int amount);

    void decrementShieldCount(int amount);

    int getOffhandCooldown();

    void setOffhandCooldown(int amount);

    void addOffhandCooldown(int amount);

    /**
     * for the sake of convenience, positive is sidestep and negative is dodge
     */
    int getRollTime();

    void setRollTime(int amount);

    void decrementRollTime(int amount);

    boolean isOffhandAttack();

    void setOffhandAttack(boolean off);

    boolean isCombatMode();

    void toggleCombatMode(boolean on);

    float getWounding();

    void setWounding(float amount);

    float getFatigue();

    void setFatigue(float amount);

    float getBurnout();

    void setBurnout(float amount);

    int getHandBind(Hand h);

    void setHandBind(Hand h, int amount);

    void decrementHandBind(Hand h, int amount);

    void update();

    void sync();

    void read(CompoundNBT tag);

    boolean isValid();

    CompoundNBT write();
}
