package jackiecrazy.wardance.capability;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateClientPacket;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class CombatCapability implements ICombatCapability {

    public static final float MAXQI = 10;
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    private static final AttributeModifier STAGGERA = new AttributeModifier(WOUND, "stagger armor debuff", -5, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier STAGGERS = new AttributeModifier(WOUND, "stagger speed debuff", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private final WeakReference<LivingEntity> dude;
    private ItemStack prev;
    private float qi, spirit, posture, combo, mpos, mspi, wounding, burnout, fatigue;
    private float shatter;
    private int shatterCD;
    private int qcd, scd, pcd, ccd, mBind, oBind;
    private int staggert, staggerc, ocd, shield, sc, roll;
    private boolean offhand, combat;
    private long lastUpdate;
    private boolean first;
    private float cache;//no need to save this because it'll be used within the span of a tick

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
        setTrueMaxSpirit(10);
    }

    @Override
    public float getMight() {
        return qi;
    }

    @Override
    public void setMight(float amount) {
        qi = MathHelper.clamp(amount, 0, MAXQI);
    }

    @Override
    public float addMight(float amount) {
        float temp = qi + amount;
        setMight(temp);
        setMightGrace(CombatConfig.qiGrace);
        return temp % 10;
    }

    @Override
    public boolean consumeMight(float amount, float above) {
        if (qi - amount < above) return false;
        qi -= amount;
        return true;
    }

    @Override
    public int getMightGrace() {
        return qcd;
    }

    @Override
    public void setMightGrace(int amount) {
        qcd = amount;
    }

    @Override
    public int decrementMightGrace(int amount) {
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
        spirit = MathHelper.clamp(amount, 0, getMaxSpirit());
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
        burnout += amount * CombatConfig.burnout;
        setSpiritGrace(CombatConfig.spiritCD);
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
        posture = MathHelper.clamp(amount, 0, getMaxPosture());
    }

    @Override
    public float addPosture(float amount) {
        float overflow = Math.max(0, posture + amount - getMaxPosture());
        setPosture(posture + amount);
        return overflow;
    }

    @Override
    public float consumePosture(float amount, float above) {
        float ret = 0;
        LivingEntity elb = dude.get();
        if (amount > getTrueMaxPosture() * CombatConfig.posCap) {
            //hard cap, knock back
            ret = amount - getTrueMaxPosture() * CombatConfig.posCap;
            amount = getTrueMaxPosture() * CombatConfig.posCap;
        }
        if (posture - amount < above) {
            posture = 0;
            if (elb == null) return ret;
            setStaggerCount(CombatConfig.staggerHits);
            setStaggerTime(CombatConfig.staggerDurationMin + Math.max(0, (int) (elb.getHealth() / elb.getMaxHealth() * (CombatConfig.staggerDuration - CombatConfig.staggerDurationMin))));
            elb.world.playSound(null, elb.getPosX(), elb.getPosY(), elb.getPosZ(), SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            elb.getAttribute(Attributes.MOVEMENT_SPEED).applyPersistentModifier(STAGGERS);
            elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).applyPersistentModifier(STAGGERA);
            return -1f;
        }
        float weakness = 1;
        if (elb != null && elb.getActivePotionEffect(Effects.WEAKNESS) != null) {
            weakness = elb.getActivePotionEffect(Effects.WEAKNESS).getAmplifier() + 1;
        }
        posture -= amount;
        fatigue += amount * CombatConfig.fatigue;
        setPostureGrace((int) (CombatConfig.postureCD * weakness));
        sync();
        return ret;
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
//        if (WarCompat.elenaiDodge && dude.get() instanceof ServerPlayerEntity && CombatConfig.elenaiC) {
//            ElenaiCompat.manipulateRegenTime(dude.get(), );
//        }
        combo = MathHelper.clamp(amount, 0, 10);
    }

    @Override
    public float addCombo(float amount) {
        float overflow = Math.max(0, combo + amount - 10);
        setCombo(combo + amount);
        setComboGrace(CombatConfig.comboGrace);
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
        if (getPosture() > getMaxPosture()) setPosture(getMaxPosture());
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
                setPosture(getMaxPosture());
                staggerc = 0;
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
            setPosture(getMaxPosture());
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
        else {
            shield = 0;
            setShieldCount(0);
        }
    }

    @Override
    public int getShieldCount() {
        return sc;
    }

    @Override
    public void setShieldCount(int amount) {
        sc = amount;
    }

    @Override
    public void decrementShieldCount(int amount) {
        sc -= Math.min(sc, amount);
        setShieldTime(0);
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
    public void addOffhandCooldown(int amount) {
        ocd += amount;
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
        if (roll + amount < 0)
            roll += amount;
        else if (roll - amount > 0)
            roll -= amount;
        else {
            if (roll < 0 && dude.get() instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity) dude.get();
                p.setForcedPose(null);
            }
            roll = 0;
        }
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
        wounding = Math.max(0, amount);
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
        fatigue = Math.max(0, amount);
    }

    @Override
    public float getBurnout() {
        return burnout;
    }

    @Override
    public void setBurnout(float amount) {
        burnout = Math.max(0, amount);
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
    public float getShatter() {
        return shatter;
    }

    @Override
    public void setShatter(float value) {
        shatter = value;
    }

    @Override
    public float consumeShatter(float value) {
        float ret = 0;
        shatter -= value;
        if (shatter < 0) {
            ret = -shatter;
            shatter = 0;
        }
        setShatterCooldown(CombatConfig.shatterCooldown);
        return ret;
    }

    @Override
    public int getShatterCooldown() {
        return shatterCD;
    }

    @Override
    public void setShatterCooldown(int value) {
        shatterCD = value;
    }

    @Override
    public int decrementShatterCooldown(int value) {
        int ret = 0;
        shatterCD -= value;
        if (shatterCD < 0) {
            ret = -shatterCD;
            shatterCD = 0;
        }
        return ret;
    }

    @Override
    public float getCachedCooldown() {
        return cache;
    }

    @Override
    public void setCachedCooldown(float value) {
        cache = value;
    }

    @Override
    public void update() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        int ticks = (int) (elb.world.getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        setTrueMaxPosture((float) (Math.ceil(10 / 1.09 * elb.getWidth() * elb.getHeight()) + elb.getTotalArmorValue() / 2d));
        if (first)
            setPosture(getMaxPosture());
        decrementComboGrace(ticks);
        int qiExtra = decrementMightGrace(ticks);
        int spExtra = decrementSpiritGrace(ticks);
        int poExtra = decrementPostureGrace(ticks);
        decrementHandBind(Hand.MAIN_HAND, ticks);
        decrementHandBind(Hand.OFF_HAND, ticks);
        addOffhandCooldown(ticks);
        if (!(elb instanceof PlayerEntity))
            elb.ticksSinceLastSwing += ticks;
        decrementRollTime(ticks);
        decrementShieldTime(ticks);
        decrementStaggerTime(ticks);
        int shcd = decrementShatterCooldown(ticks);
        //check max posture, max spirit, decrement bind and offhand cooldown
        if (getPostureGrace() == 0 && getStaggerTime() == 0 && getPosture() < getMaxPosture()) {
            addPosture(getPPS() * (poExtra));
        }
        if (shcd != 0) {
            setShatter((float) elb.getAttributeValue(WarAttributes.SHATTER.get()));
        }
        if (getSpiritGrace() == 0 && getStaggerTime() == 0 && getSpirit() < getMaxSpirit()) {
            addSpirit(getPPS() * spExtra);
        }
        if (getMightGrace() == 0) {
            float over = qiExtra * 0.01f;
            setMight(getMight() - over);
            if (getMight() > 0) {
                int divisor = 0;
                if (fatigue > 0) divisor++;
                if (burnout > 0) divisor++;
                if (wounding > 0) divisor++;
                if (divisor > 0) {
                    float heal = over / divisor;
                    setWounding(wounding - heal);
                    setFatigue(fatigue - heal);
                    setBurnout(burnout - heal);
                }
            }
        }
        if (getComboGrace() == 0) {
            setCombo((float) Math.floor(getCombo()));
        }
        if (prev == null || !ItemStack.areItemStacksEqual(elb.getHeldItemOffhand(), prev)) {
            prev = elb.getHeldItemOffhand();
            setOffhandCooldown(0);
        }
        lastUpdate = elb.world.getGameTime();
        first = false;
        sync();
    }

    @Override
    public void sync() {
        LivingEntity elb = dude.get();
        if (elb == null || elb.world.isRemote) return;
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientPacket(elb.getEntityId(), write()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) elb), new UpdateClientPacket(elb.getEntityId(), write()));

    }

    @Override
    public void read(CompoundNBT c) {
        int temp = roll;
        setMight(c.getFloat("qi"));
        setPosture(c.getFloat("posture"));
        setCombo(c.getFloat("combo"));
        setSpirit(c.getFloat("spirit"));
        setTrueMaxPosture(c.getFloat("maxpos"));
        setTrueMaxSpirit(c.getFloat("maxspi"));
        setBurnout(c.getFloat("burnout"));
        setWounding(c.getFloat("wounding"));
        setFatigue(c.getFloat("fatigue"));
        setComboGrace(c.getInt("combocd"));
        setMightGrace(c.getInt("qicd"));
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
        setShieldCount(c.getInt("shieldC"));
        setShatterCooldown(c.getInt("shattercd"));
        setShatter(c.getFloat("shatter"));
        lastUpdate = c.getLong("lastUpdate");
        first = c.getBoolean("first");
        if (dude.get() instanceof PlayerEntity) {
            if (MovementUtils.hasInvFrames(dude.get()))
                ((PlayerEntity) dude.get()).setForcedPose(Pose.SWIMMING);
            else if (temp == -(int) (CombatConfig.rollEndsAt))
                ((PlayerEntity) dude.get()).setForcedPose(null);
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT c = new CompoundNBT();
        c.putFloat("qi", getMight());
        c.putFloat("posture", getPosture());
        c.putFloat("combo", getCombo());
        c.putFloat("spirit", getSpirit());
        c.putFloat("maxpos", getTrueMaxPosture());
        c.putFloat("maxspi", getTrueMaxSpirit());
        c.putFloat("burnout", getBurnout());
        c.putFloat("fatigue", getFatigue());
        c.putFloat("wounding", getWounding());
        c.putInt("combocd", getComboGrace());
        c.putInt("qicd", getMightGrace());
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
        c.putInt("shieldC", sc);
        c.putBoolean("first", first);
        c.putInt("shattercd", getShatterCooldown());
        c.putFloat("shatter", getShatter());
        return c;
    }

    private float getPPS() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        float nausea = elb instanceof PlayerEntity || elb.getActivePotionEffect(Effects.NAUSEA) == null ? 0 : (elb.getActivePotionEffect(Effects.NAUSEA).getAmplifier() + 1) * CombatConfig.nausea;
        int exp = elb.getActivePotionEffect(Effects.POISON) == null ? 0 : (elb.getActivePotionEffect(Effects.POISON).getAmplifier() + 1);
        float poison = CombatConfig.poison;
        for (int j = 0; j < exp; j++) {
            poison *= poison;
        }
        float armorMod = Math.max(1f - ((float) elb.getTotalArmorValue() / 40f), 0);
        float healthMod = elb.getHealth() / elb.getMaxHealth();
        Vector3d spd = elb.getMotion();
        float speedMod = (float) Math.min(1, 0.007f / (spd.x * spd.x + spd.z * spd.z));
        if (getStaggerTime() > 0) {
            return getMaxPosture() * armorMod * speedMod * healthMod / (1.5f * CombatConfig.staggerDuration);
        }
        return (0.2f * armorMod * healthMod * speedMod) - nausea;
    }
}
