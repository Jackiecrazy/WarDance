package jackiecrazy.wardance.capability;

import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateClientPacket;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.PacketDistributor;
import org.w3c.dom.Attr;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;

public class CombatCapability implements ICombatCapability {
    public static final float MAXQI = 10;
    public static final int COOLDOWN = 30;
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    private static final int MAXDOWNTIME = 50;
    private static final AttributeModifier STAGGERA = new AttributeModifier(WOUND, "stagger armor debuff", -9, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier STAGGERS = new AttributeModifier(WOUND, "stagger speed debuff", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private WeakReference<LivingEntity> dude = null;
    private float qi, spirit, posture, combo, mpos, mspi, wounding, burnout, fatigue;
    private int qcd, scd, pcd, ccd, mBind, oBind;
    private int staggert, staggerc, ocd, shield, roll;
    private boolean offhand, combat;
    private long lastUpdate;

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
        setTrueMaxSpirit(10);
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
        setQiGrace(COOLDOWN);
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
    public int decrementQiGrace(int amount) {
        qcd -= amount;
        if (qcd < 0) {
            int temp = qcd;
            qcd = 0;
            return -temp;
        }
        return 0;
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
        setSpiritGrace(COOLDOWN);
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
    public int decrementSpiritGrace(int amount) {
        scd -= amount;
        if (scd < 0) {
            int temp = scd;
            scd = 0;
            return -temp;
        }
        return 0;
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
            posture = 0;
            setStaggerCount(3);
            setStaggerTime(Math.min((int) (amount * 20), 60));
            LivingEntity elb = dude.get();
            if (elb == null) return false;
            elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            elb.getAttribute(Attributes.MOVEMENT_SPEED).applyPersistentModifier(STAGGERS);
            elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).applyPersistentModifier(STAGGERA);
            return false;
        }
        posture -= amount;
        fatigue += amount / 10f;
        setPostureGrace(COOLDOWN);
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
    public int decrementPostureGrace(int amount) {
        pcd -= amount;
        if (pcd < 0) {
            int temp = pcd;
            pcd = 0;
            return -temp;
        }
        return 0;
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
    public int decrementComboGrace(int amount) {
        ccd -= amount;
        if (ccd < 0) {
            int temp = ccd;
            ccd = 0;
            return -temp;
        }
        return 0;
    }


    @Override
    public int getStaggerTime() {
        return staggert;
    }

    @Override
    public void setStaggerTime(int amount) {
        staggert = amount;
        if (amount == 0 && dude.get() != null) {
            LivingEntity elb = dude.get();
            elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
        }
    }

    @Override
    public int decrementStaggerTime(int amount) {
        if (staggert - amount > 0)
            staggert -= amount;
        else {
            LivingEntity elb = dude.get();
            if (staggert > 0 && elb != null) {
                elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
                elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            }
            int temp = staggert;
            staggert = 0;
            return -temp;
        }
        return 0;
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
        else {
            setStaggerTime(0);
            staggerc = 0;
        }
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

    @Override
    public void update() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        int ticks = (int) (elb.world.getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        setTrueMaxPosture((float) (5 * Math.ceil(elb.getWidth()) * Math.ceil(elb.getHeight()) + elb.getTotalArmorValue() / 2d));
        decrementComboGrace(ticks);
        int qiExtra = decrementQiGrace(ticks);
        int spExtra = decrementSpiritGrace(ticks);
        int poExtra = decrementPostureGrace(ticks);
        decrementHandBind(Hand.MAIN_HAND, ticks);
        decrementHandBind(Hand.OFF_HAND, ticks);
        decrementOffhandCooldown(ticks);
        decrementRollTime(ticks);
        decrementShieldTime(ticks);
        int stExtra = decrementStaggerTime(ticks);
        //check max posture, max spirit, decrement bind and offhand cooldown
        if (getPostureGrace() == 0 && getStaggerTime() == 0 && getPosture() < getMaxPosture()) {
            addPosture(getPPS() * poExtra);
        }
        if (getSpiritGrace() == 0 && getStaggerTime() == 0 && getSpirit() < getMaxSpirit()) {
            addSpirit(getPPS() * spExtra);
        }
        if (getQiGrace() == 0) {
            setQi(getQi() - qiExtra * 0.1f);
        }
        if (getComboGrace() == 0) {
            setCombo((float) Math.floor(getCombo()));
        }
        lastUpdate = elb.world.getGameTime();
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientPacket(elb.getEntityId(), write()));

    }

    @Override
    public void read(CompoundNBT c) {
        setQi(c.getFloat("qi"));
        setPosture(c.getFloat("posture"));
        setCombo(c.getFloat("combo"));
        setSpirit(c.getFloat("spirit"));
        setTrueMaxPosture(c.getFloat("maxpos"));
        setTrueMaxSpirit(c.getFloat("maxspi"));
        setBurnout(c.getFloat("burnout"));
        setWounding(c.getFloat("wounding"));
        setFatigue(c.getFloat("fatigue"));
        setComboGrace(c.getInt("combocd"));
        setQiGrace(c.getInt("qicd"));
        setPostureGrace(c.getInt("posturecd"));
        setSpiritGrace(c.getInt("spiritcd"));
        setShieldTime(c.getInt("shield"));
        setStaggerCount(c.getInt("staggerc"));
        setStaggerTime(c.getInt("staggert"));
        setOffhandCooldown(c.getInt("offhandcd"));
        setRollTime(c.getInt("roll"));
        setHandBind(Hand.MAIN_HAND, c.getInt("bMain"));
        setHandBind(Hand.OFF_HAND, c.getInt("bOff"));
        setOffhandAttack(c.getBoolean("offhand"));
        toggleCombatMode(c.getBoolean("combat"));
        lastUpdate = c.getLong("lastUpdate");
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT c = new CompoundNBT();
        c.putFloat("qi", getQi());
        c.putFloat("posture", getPosture());
        c.putFloat("combo", getCombo());
        c.putFloat("spirit", getSpirit());
        c.putFloat("maxpos", getTrueMaxPosture());
        c.putFloat("maxspi", getTrueMaxSpirit());
        c.putFloat("burnout", getBurnout());
        c.putFloat("fatigue", getFatigue());
        c.putFloat("wounding", getWounding());
        c.putInt("combocd", getComboGrace());
        c.putInt("qicd", getQiGrace());
        c.putInt("posturecd", getPostureGrace());
        c.putInt("spiritcd", getSpiritGrace());
        c.putInt("shield", getShieldTime());
        c.putInt("staggerc", getStaggerCount());
        c.putInt("staggert", getStaggerTime());
        c.putInt("offhandcd", getOffhandCooldown());
        c.putInt("roll", getRollTime());
        c.putInt("bMain", getHandBind(Hand.MAIN_HAND));
        c.putInt("bOff", getHandBind(Hand.OFF_HAND));
        c.putBoolean("offhand", isOffhandAttack());
        c.putBoolean("combat", isCombatMode());
        c.putLong("lastUpdate", lastUpdate);
        return c;
    }

    private float getPPS() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        float nausea = elb instanceof PlayerEntity || elb.getActivePotionEffect(Effects.NAUSEA) == null ? 0 : (elb.getActivePotionEffect(Effects.NAUSEA).getAmplifier() + 1) * 0.05f;
        float armorMod = Math.max(1f - ((float) elb.getTotalArmorValue() / 40f), 0);
        float healthMod = elb.getHealth() / elb.getMaxHealth();
        float speedMod = 0.2f / (float) Math.max(0.2, GeneralUtils.getSpeedSq(elb));
        if (getStaggerTime() > 0) {
            return getMaxPosture() * armorMod * speedMod * healthMod / (1.5f * MAXDOWNTIME);
        }
        return (0.2f * armorMod * healthMod * speedMod) - nausea;
    }
}
