package jackiecrazy.wardance.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

public class DummyCombatCap implements ICombatCapability{


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
    public float consumePosture(float amount, float above) {
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
    public float getCombo() {
        return 0;
    }

    @Override
    public void setCombo(float amount) {

    }

    @Override
    public float addCombo(float amount) {
        return 0;
    }

    @Override
    public boolean consumeCombo(float amount, float above) {
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
    public int getComboGrace() {
        return 0;
    }

    @Override
    public void setComboGrace(int amount) {

    }

    @Override
    public int decrementComboGrace(int amount) {
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
    public int getShieldCount() {
        return 0;
    }

    @Override
    public void setShieldCount(int amount) {

    }

    @Override
    public void decrementShieldCount(int amount) {

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
    public float getShatter() {
        return 0;
    }

    @Override
    public void setShatter(float value) {

    }

    @Override
    public float consumeShatter(float value) {
        return 0;
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
    public void update() {

    }

    @Override
    public void sync() {

    }

    @Override
    public void read(CompoundNBT tag) {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public CompoundNBT write() {
        return null;
    }
}
