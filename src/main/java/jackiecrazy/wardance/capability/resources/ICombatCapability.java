package jackiecrazy.wardance.capability.resources;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public interface ICombatCapability {
    //might, spirit, posture, combo
    //might cooldown, spirit cooldown, posture cooldown, combo grace period
    //stagger timer, stagger counter for downed attacks
    //offhand cooldown, shield parry time, sidestep/roll timer
    //set, get, increment/decrement, consume (resource only)
    //is offhand attack, combat mode
    //shatter, shatter cooldown
    float getResolve();

    void setResolve(float amount);

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

    default float consumePosture(LivingEntity attacker, float amount) {
        return consumePosture(attacker, amount, 0);
    }

    default float consumePosture(float amount, float above) {
        return consumePosture(null, amount, above, false);
    }

    default float consumePosture(LivingEntity attacker, float amount, float above) {
        return consumePosture(attacker, amount, above, false);
    }

    boolean isFirstStaggerStrike();

    float consumePosture(LivingEntity assailant, float amount, float above, boolean force);

    int getPostureGrace();

    void setPostureGrace(int amount);

    int decrementPostureGrace(int amount);

    float getRank();

    void setRank(float amount);

    default int getComboRank() {
        float workingCombo = getRank();
        if (workingCombo >= 9)
            return 7;//SSStylish!
        if (workingCombo >= 6) {
            return 6;//SShowtime!
        }
        if (workingCombo >= 4) {
            return 5;//Sweet!
        }
        return (int) workingCombo;//all other ranks
    }

    void setAdrenalineCooldown(int amount);
    boolean halvedAdrenaline();

    float addRank(float amount);

    default boolean consumeRank(float amount) {
        return consumeRank(amount, 0);
    }

    boolean consumeRank(float amount, float above);

    float getTrueMaxPosture();

    void setTrueMaxPosture(float amount);

    float getTrueMaxSpirit();

    void setTrueMaxSpirit(float amount);

    float getMaxMight();

    void setMaxMight(float amount);

    default float getMaxPosture() {
        return Math.max(0.1f, getTrueMaxPosture() - getFatigue());
    }

    default float getMaxSpirit() {
        return Math.max(0.1f, getTrueMaxSpirit() - getBurnout());
    }

    int getMaxStaggerTime();

    int getStaggerTime();

    void setStaggerTime(int amount);

    int decrementStaggerTime(int amount);

    int getMaxStaggerCount();

    int getStaggerCount();

    void setStaggerCount(int amount);

    void decrementStaggerCount(int amount);

    int getBarrierCooldown();

    void setBarrierCooldown(int amount);

    void decrementBarrierCooldown(int amount);

    float getMaxBarrier();

    void setMaxBarrier(float amount);

    float getBarrier();

    void setBarrier(float amount);

    float consumeBarrier(float amount);

    void addBarrier(float amount);

    int getOffhandCooldown();

    void setOffhandCooldown(int amount);

    void addOffhandCooldown(int amount);

    /**
     * for the sake of convenience, positive is subject to cooldown and negatives are free
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

    void addWounding(float amount);

    void addFatigue(float amount);

    void addBurnout(float amount);

    int getHandBind(Hand h);

    void setHandBind(Hand h, int amount);

    void decrementHandBind(Hand h, int amount);

    float getHandReel(Hand hand);

    void setHandReel(Hand hand, float value);

    boolean consumeShatter(float value);

    int getShatterCooldown();

    void setShatterCooldown(int value);

    int decrementShatterCooldown(int value);

    float getCachedCooldown();

    void setCachedCooldown(float value);

    int getForcedSweep();

    void setForcedSweep(int angle);

    void clientTick();

    void serverTick();

    void sync();

    ItemStack getTempItemStack();

    void setTempItemStack(ItemStack is);

    void read(CompoundNBT tag);

    int getParryingTick();//hey, it's useful for future "smart" entities as well.

    void setParryingTick(int parrying);

    int getSweepTick();

    void setSweepTick(int tick);

    boolean isValid();

    Vector3d getMotionConsistently();//I can't believe I have to do this.

    CompoundNBT write();

    void addRangedMight(boolean pass);
}
