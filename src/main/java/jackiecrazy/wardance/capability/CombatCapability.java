package jackiecrazy.wardance.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class CombatCapability implements ICombatCapability {
    public static final float MAXQI = 10;
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");

    private WeakReference<LivingEntity> dude = null;
    private float qi, spirit, posture, combo, mpos, mspi, wounding, burnout, fatigue;
    private int qcd, scd, pcd, ccd, mBind, oBind;
    private int staggert, staggerc, ocd, shield, roll;
    private boolean offhand, combat;

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
    }

    @Override
    public float getQi() {
        return qi;
    }

    @Override
    public void setQi(float amount) {
        qi = MathHelper.clamp(amount, 0, MAXQI);
    }

    @Override
    public float addQi(float amount) {
        float temp = qi + amount;
        setQi(temp);
        return temp % 10;
    }

    @Override
    public boolean consumeQi(float amount, float above) {
        if (qi - amount < above) return false;
        qi -= amount;
        fatigue += amount / 10f;
        return true;
    }

    @Override
    public int getQiGrace() {
        return qcd;
    }

    @Override
    public void setQiGrace(int amount) {
        qcd = amount;
    }

    @Override
    public void decrementQiGrace(int amount) {
        qcd -= amount;
        if (qcd < 0) qcd = 0;
    }

    @Override
    public float getSpirit() {
        return spirit;
    }

    @Override
    public void setSpirit(float amount) {
        spirit = MathHelper.clamp(amount, 0, mspi);
    }

    @Override
    public float addSpirit(float amount) {
        float overflow = Math.max(0, spirit + amount - mspi);
        setSpirit(spirit + amount);
        return overflow;
    }

    @Override
    public boolean consumeSpirit(float amount, float above) {
        if (spirit - amount < above) return false;
        spirit -= amount;
        burnout += amount / 10f;
        return true;
    }

    @Override
    public int getSpiritGrace() {
        return scd;
    }

    @Override
    public void setSpiritGrace(int amount) {
        scd = amount;
    }

    @Override
    public void decrementSpiritGrace(int amount) {
        if (scd - amount > 0)
            scd -= amount;
        else scd = 0;
    }

    @Override
    public float getPosture() {
        return posture;
    }

    @Override
    public void setPosture(float amount) {
        posture = MathHelper.clamp(amount, 0, getTrueMaxPosture());
    }

    @Override
    public float addPosture(float amount) {
        float overflow = Math.max(0, posture + amount - getTrueMaxPosture());
        setPosture(posture + amount);
        return overflow;
    }

    @Override
    public boolean consumePosture(float amount, float above) {
        if (posture - amount < above) {
            //TODO stagger. armor down, knockback, ban moving, other theatrics
            posture=0;
            setStaggerCount(3);
            setStaggerTime(Math.min((int)(amount*20), 60));
        }
        posture -= amount;
        fatigue += amount / 10f;
        return true;
    }

    @Override
    public int getPostureGrace() {
        return pcd;
    }

    @Override
    public void setPostureGrace(int amount) {
        pcd = amount;
    }

    @Override
    public void decrementPostureGrace(int amount) {
        if (pcd - amount > 0)
            pcd -= amount;
        else pcd = 0;
    }


    @Override
    public float getCombo() {
        return combo;
    }

    @Override
    public void setCombo(float amount) {
        combo = MathHelper.clamp(amount, 0, 10);
    }

    @Override
    public float addCombo(float amount) {
        float overflow = Math.max(0, combo + amount - 10);
        setCombo(combo + amount);
        return overflow;
    }

    @Override
    public boolean consumeCombo(float amount, float above) {
        if (combo - amount < above) return false;
        combo -= amount;
        return true;
    }

    @Override
    public float getTrueMaxPosture() {
        return mpos;
    }

    @Override
    public void setTrueMaxPosture(float amount) {
        mpos = amount;
    }

    @Override
    public float getTrueMaxSpirit() {
        return mspi;
    }

    @Override
    public void setTrueMaxSpirit(float amount) {
        mspi = amount;
    }

    @Override
    public int getComboGrace() {
        return ccd;
    }

    @Override
    public void setComboGrace(int amount) {
        ccd = amount;
    }

    @Override
    public void decrementComboGrace(int amount) {
        if (ccd - amount > 0)
            ccd -= amount;
        else ccd = 0;
    }


    @Override
    public int getStaggerTime() {
        return staggert;
    }

    @Override
    public void setStaggerTime(int amount) {
        staggert = amount;
    }

    @Override
    public void decrementStaggerTime(int amount) {
        if (staggert - amount > 0)
            staggert -= amount;
        else staggert = 0;
    }


    @Override
    public int getStaggerCount() {
        return staggerc;
    }

    @Override
    public void setStaggerCount(int amount) {
        staggerc = amount;
    }

    @Override
    public void decrementStaggerCount(int amount) {
        if (staggerc - amount > 0)
            staggerc -= amount;
        else staggerc = 0;
    }


    @Override
    public int getShieldTime() {
        return shield;
    }

    @Override
    public void setShieldTime(int amount) {
        shield = amount;
    }

    @Override
    public void decrementShieldTime(int amount) {
        if (shield - amount > 0)
            shield -= amount;
        else shield = 0;
    }


    @Override
    public int getOffhandCooldown() {
        return ocd;
    }

    @Override
    public void setOffhandCooldown(int amount) {
        ocd = amount;
    }

    @Override
    public void decrementOffhandCooldown(int amount) {
        if (ocd - amount > 0)
            ocd -= amount;
        else ocd = 0;
    }


    @Override
    public int getRollTime() {
        return roll;
    }

    @Override
    public void setRollTime(int amount) {
        roll = amount;
    }

    @Override
    public void decrementRollTime(int amount) {
        if (roll - amount > 0)
            roll -= amount;
        else roll = 0;
    }


    @Override
    public boolean isOffhandAttack() {
        return offhand;
    }

    @Override
    public void setOffhandAttack(boolean off) {
        offhand = off;
    }

    @Override
    public boolean isCombatMode() {
        return combat;
    }

    @Override
    public void toggleCombatMode(boolean on) {
        combat = on;
    }

    @Override
    public float getWounding() {
        return wounding;
    }

    @Override
    public void setWounding(float amount) {
        wounding = amount;
        if (dude.get() != null) {
            dude.get().getAttribute(Attributes.MAX_HEALTH).removeModifier(WOUND);
            dude.get().getAttribute(Attributes.MAX_HEALTH).applyNonPersistentModifier(new AttributeModifier(WOUND, "wounding", -amount, AttributeModifier.Operation.ADDITION));
        }
    }

    @Override
    public float getFatigue() {
        return fatigue;
    }

    @Override
    public void setFatigue(float amount) {
        fatigue = amount;
    }

    @Override
    public float getBurnout() {
        return burnout;
    }

    @Override
    public void setBurnout(float amount) {
        burnout = amount;
    }

    @Override
    public int getHandBind(Hand h) {
        if (h == Hand.OFF_HAND) {
            return oBind;
        }
        return mBind;
    }

    @Override
    public void setHandBind(Hand h, int amount) {
        switch (h) {
            case MAIN_HAND:
                mBind = amount;
            case OFF_HAND:
                oBind = amount;
        }
    }

    @Override
    public void decrementHandBind(Hand h, int amount) {
        switch (h) {
            case MAIN_HAND:
                mBind -= amount;
            case OFF_HAND:
                oBind -= amount;
        }
    }
}
