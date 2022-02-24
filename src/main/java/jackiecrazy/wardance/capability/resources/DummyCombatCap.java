package jackiecrazy.wardance.capability.resources;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class DummyCombatCap implements ICombatCapability {


    @Override
    public float getResolve() {
        return 0;
    }

    @Override
    public void setResolve(float amount) {

    }

    @Override
    public float getMight() {
        return 0;
    }

    @Override
    public void setMight(float amount) {

    }

    @Override
    public float addMight(float amount) {
        return 0;
    }

    @Override
    public boolean consumeMight(float amount, float above) {
        return false;
    }

    @Override
    public int getMightGrace() {
        return 0;
    }

    @Override
    public void setMightGrace(int amount) {

    }

    @Override
    public int decrementMightGrace(int amount) {
        return 0;
    }

    @Override
    public float getSpirit() {
        return 0;
    }

    @Override
    public void setSpirit(float amount) {

    }

    @Override
    public float addSpirit(float amount) {
        return 0;
    }

    @Override
    public boolean consumeSpirit(float amount, float above) {
        return false;
    }

    @Override
    public int getSpiritGrace() {
        return 0;
    }

    @Override
    public void setSpiritGrace(int amount) {

    }

    @Override
    public int decrementSpiritGrace(int amount) {
        return 0;
    }

    @Override
    public float getPosture() {
        return 0;
    }

    @Override
    public void setPosture(float amount) {

    }

    @Override
    public float addPosture(float amount) {
        return 0;
    }

    @Override
    public boolean isFirstStaggerStrike() {
        return false;
    }

    @Override
    public float consumePosture(LivingEntity assailant, float amount, float above, boolean force) {
        return 0;
    }

    @Override
    public int getPostureGrace() {
        return 0;
    }

    @Override
    public void setPostureGrace(int amount) {

    }

    @Override
    public int decrementPostureGrace(int amount) {
        return 0;
    }

    @Override
    public float getRank() {
        return 0;
    }

    @Override
    public void setRank(float amount) {

    }

    @Override
    public void setAdrenalineCooldown(int amount) {

    }

    @Override
    public boolean halvedAdrenaline() {
        return false;
    }

    @Override
    public float addRank(float amount) {
        return 0;
    }

    @Override
    public boolean consumeRank(float amount, float above) {
        return false;
    }

    @Override
    public float getTrueMaxPosture() {
        return 0;
    }

    @Override
    public void setTrueMaxPosture(float amount) {

    }

    @Override
    public float getTrueMaxSpirit() {
        return 0;
    }

    @Override
    public void setTrueMaxSpirit(float amount) {

    }

    @Override
    public float getMaxMight() {
        return 0;
    }

    @Override
    public void setMaxMight(float amount) {

    }

    private int getComboGrace() {
        return 0;
    }

    private void setComboGrace(int amount) {

    }

    private int decrementComboGrace(int amount) {
        return 0;
    }

    @Override
    public int getStaggerTime() {
        return 0;
    }

    @Override
    public void setStaggerTime(int amount) {

    }

    @Override
    public int decrementStaggerTime(int amount) {
        return 0;
    }

    @Override
    public int getStaggerCount() {
        return 0;
    }

    @Override
    public void setStaggerCount(int amount) {

    }

    @Override
    public void decrementStaggerCount(int amount) {

    }

    @Override
    public int getShieldTime() {
        return 0;
    }

    @Override
    public void setShieldTime(int amount) {

    }

    @Override
    public void decrementShieldTime(int amount) {

    }

    @Override
    public float getShieldBarrier() {
        return 0;
    }

    @Override
    public void setShieldBarrier(float amount) {

    }

    @Override
    public void decrementShieldBarrier(float amount) {

    }

    @Override
    public int getOffhandCooldown() {
        return 0;
    }

    @Override
    public void setOffhandCooldown(int amount) {

    }

    @Override
    public void addOffhandCooldown(int amount) {

    }

    @Override
    public int getRollTime() {
        return 0;
    }

    @Override
    public void setRollTime(int amount) {

    }

    @Override
    public void decrementRollTime(int amount) {

    }

    @Override
    public boolean isOffhandAttack() {
        return false;
    }

    @Override
    public void setOffhandAttack(boolean off) {

    }

    @Override
    public boolean isCombatMode() {
        return false;
    }

    @Override
    public void toggleCombatMode(boolean on) {

    }

    @Override
    public float getWounding() {
        return 0;
    }

    @Override
    public void setWounding(float amount) {

    }

    @Override
    public float getFatigue() {
        return 0;
    }

    @Override
    public void setFatigue(float amount) {

    }

    @Override
    public float getBurnout() {
        return 0;
    }

    @Override
    public void setBurnout(float amount) {

    }

    @Override
    public void addWounding(float amount) {

    }

    @Override
    public void addFatigue(float amount) {

    }

    @Override
    public void addBurnout(float amount) {

    }

    @Override
    public int getHandBind(Hand h) {
        return 0;
    }

    @Override
    public void setHandBind(Hand h, int amount) {

    }

    @Override
    public void decrementHandBind(Hand h, int amount) {

    }

    @Override
    public float getHandReel(Hand hand) {
        return 0;
    }

    @Override
    public void setHandReel(Hand hand, float value) {

    }

    @Override
    public boolean consumeShatter(float value) {
        return false;
    }

    @Override
    public int getShatterCooldown() {
        return 0;
    }

    @Override
    public void setShatterCooldown(int value) {

    }

    @Override
    public int decrementShatterCooldown(int value) {
        return 0;
    }

    @Override
    public float getCachedCooldown() {
        return 0;
    }

    @Override
    public void setCachedCooldown(float value) {

    }

    @Override
    public int getForcedSweep() {
        return 0;
    }

    @Override
    public void setForcedSweep(int angle) {

    }

    @Override
    public void clientTick() {

    }

    @Override
    public void serverTick() {

    }

    @Override
    public void sync() {

    }

    @Override
    public void setTempItemStack(ItemStack is) {

    }

    @Override
    public ItemStack getTempItemStack() {
        return null;
    }

    @Override
    public void read(CompoundNBT tag) {

    }

    @Override
    public void setParryingTick(int parrying) {

    }

    @Override
    public int getParryingTick() {
        return 0;
    }

    @Override
    public void setSweepTick(int tick) {

    }

    @Override
    public int getSweepTick() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Vector3d getMotionConsistently() {
        return Vector3d.ZERO;
    }

    @Override
    public CompoundNBT write() {
        return null;
    }

    @Override
    public void addRangedMight(boolean pass) {

    }
}
