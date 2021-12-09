package jackiecrazy.wardance.capability.resources;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.event.GainMightEvent;
import jackiecrazy.wardance.event.GainPostureEvent;
import jackiecrazy.wardance.event.RegenSpiritEvent;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateClientPacket;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class CombatCapability implements ICombatCapability {

    public static final float MAXQI = 10;
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    public static final UUID MORE = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c9");
    private static final AttributeModifier STAGGERA = new AttributeModifier(WOUND, "stagger armor debuff", -7, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier STAGGERSA = new AttributeModifier(MORE, "additional stagger armor penalty", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier STAGGERS = new AttributeModifier(WOUND, "stagger speed debuff", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private final WeakReference<LivingEntity> dude;
    private ItemStack prev;
    private float qi, spirit, posture, combo, mpos, mspi, wounding, burnout, fatigue, mainReel, offReel, maxMight;
    private int shatterCD;
    private int qcd, scd, pcd, ccd, mBind, oBind;
    private int staggert, staggerc, ocd, shield, sc, roll, sweepAngle = -1;
    private boolean offhand, combat;
    private long lastUpdate;
    private boolean first, shattering;
    private float cache;//no need to save this because it'll be used within the span of a tick
    private int parrying;
    private long staggerTickExisted;
    private int recoveryTimer, sweeping;
    private ItemStack tempOffhand = ItemStack.EMPTY;
    private Vector3d motion;

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
        setTrueMaxSpirit((float) GeneralUtils.getAttributeValueSafe(e, WarAttributes.MAX_SPIRIT.get()));
    }

    private static float getMPos(LivingEntity elb) {
        float ret = 0;
        if (GeneralUtils.getResourceLocationFromEntity(elb) != null && CombatUtils.customPosture.containsKey(GeneralUtils.getResourceLocationFromEntity(elb)))
            ret = CombatUtils.customPosture.get(GeneralUtils.getResourceLocationFromEntity(elb));
        else ret = (float) (Math.ceil(10 / 1.09 * elb.getWidth() * elb.getHeight()) + elb.getTotalArmorValue() / 2d);
        if (elb instanceof PlayerEntity) ret *= 2;
        ret += GeneralUtils.getAttributeValueSafe(elb, WarAttributes.MAX_POSTURE.get());
        return ret;
    }

    @Override
    public float getMight() {
        return qi;
    }

    @Override
    public void setMight(float amount) {
        float cap = maxMight;
        qi = MathHelper.clamp(amount, 0, cap);
    }

    @Override
    public float addMight(float amount) {
        GainMightEvent gme = new GainMightEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(gme);
        amount = gme.getQuantity();
        float temp = qi + amount;
        setMight(temp);
        setMightGrace(ResourceConfig.qiGrace);
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
        setBurnout(this.getBurnout() + amount * ResourceConfig.burnout);
        setSpiritGrace(ResourceConfig.spiritCD);
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
    public boolean isFirstStaggerStrike() {
        if (dude.get() == null) return false;
        return dude.get().ticksExisted == staggerTickExisted;
    }

    @Override
    public float consumePosture(LivingEntity assailant, float amount, float above, boolean force) {
        float ret = 0;
        LivingEntity elb = dude.get();
        if (elb == null) return ret;
        if (staggert > 0) return amount;
        if (!Float.isFinite(posture)) posture = getMaxPosture();
        if (elb.isPotionActive(Effects.RESISTANCE) && GeneralConfig.resistance)
            amount *= (1 - (elb.getActivePotionEffect(Effects.RESISTANCE).getAmplifier() + 1) * 0.2f);
        if (amount > getTrueMaxPosture() * CombatConfig.posCap && !force) {
            //hard cap, knock back
            ret = amount - getTrueMaxPosture() * CombatConfig.posCap;
            amount = getTrueMaxPosture() * CombatConfig.posCap;
        }
        if (above > 0 && posture - amount < above) {
            //hard cap, set it there and do nothing else
            ret = amount - above;
            amount = posture - above;
        } else if (posture - amount < 0) {
            posture = 0;
            StaggerEvent se = new StaggerEvent(elb, assailant, CombatConfig.staggerDurationMin + Math.max(0, (int) ((elb.getMaxHealth() - elb.getHealth()) / elb.getMaxHealth() * (CombatConfig.staggerDuration - CombatConfig.staggerDurationMin))), CombatConfig.staggerHits);
            MinecraftForge.EVENT_BUS.post(se);
            if (se.isCanceled()) return 0f;
            setStaggerCount(se.getCount());
            setStaggerTime(se.getLength());
            elb.world.playSound(null, elb.getPosX(), elb.getPosY(), elb.getPosZ(), SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            elb.getAttribute(Attributes.MOVEMENT_SPEED).applyPersistentModifier(STAGGERS);
            elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).applyPersistentModifier(STAGGERA);
            elb.getAttribute(Attributes.ARMOR).removeModifier(MORE);
            elb.getAttribute(Attributes.ARMOR).applyPersistentModifier(STAGGERSA);
            elb.dismount();
            staggerTickExisted = elb.ticksExisted;
            return -1f;
        }
        float weakness = 1;
        float cooldown = ResourceConfig.postureCD * weakness;
        cooldown += ResourceConfig.armorPostureCD * elb.getTotalArmorValue() / 20f;
        if (elb.isPotionActive(Effects.HUNGER))
            for (int uwu = 0; uwu < elb.getActivePotionEffect(Effects.HUNGER).getAmplifier() + 1; uwu++)
                weakness *= GeneralConfig.hunger;
        posture -= amount;
        addFatigue(amount * ResourceConfig.fatigue);
        setPostureGrace((int) cooldown);
        recoveryTimer = (int) (cooldown * 1.5f) + CombatConfig.recovery;
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
//        if (WarCompat.elenaiDodge && dude.get() instanceof ServerPlayerEntity && ResourceConfig.elenaiC) {
//            ElenaiCompat.manipulateRegenTime(dude.get(), );
//        }
        combo = MathHelper.clamp(amount, 0, 10);
    }

    @Override
    public float addCombo(float amount) {
        float overflow = Math.max(0, combo + amount - 10);
        setCombo(combo + amount);
        setComboGrace(ResourceConfig.comboGrace);
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
        float perc = posture / mpos;
        float temp = mpos;
        mpos = amount;
        if (mpos != temp)
            posture = perc * mpos;
        if (Float.isNaN(posture))
            posture = mpos;
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
    public float getMaxMight() {
        return maxMight;
    }

    @Override
    public void setMaxMight(float amount) {
        maxMight = amount;
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
        if (amount == 0 && staggert > 0 && dude.get() != null) {
            LivingEntity elb = dude.get();
            elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).removeModifier(MORE);
        }
        staggert = amount;
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
                elb.getAttribute(Attributes.ARMOR).removeModifier(MORE);
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
            setStaggerTime(0);
            setPosture(getMaxPosture());
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
        else if (roll != 0) {
            if (dude.get() instanceof PlayerEntity) {
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
            boolean reg = (ForgeRegistries.ENTITIES.getKey(dude.get().getType()) != null && ResourceConfig.immortal.contains(ForgeRegistries.ENTITIES.getKey(dude.get().getType()).toString()));
            if (!ResourceConfig.immortalWL == reg) {
                wounding = 0;
            }
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
        if (dude.get() != null) {
            boolean reg = (ForgeRegistries.ENTITIES.getKey(dude.get().getType()) != null && ResourceConfig.immortal.contains(ForgeRegistries.ENTITIES.getKey(dude.get().getType()).toString()));
            if (!ResourceConfig.immortalWL == reg) {
                fatigue = 0;
            }
        }
    }

    @Override
    public float getBurnout() {
        return burnout;
    }

    @Override
    public void setBurnout(float amount) {
        burnout = Math.max(0, amount);
        if (dude.get() != null) {
            boolean reg = (ForgeRegistries.ENTITIES.getKey(dude.get().getType()) != null && ResourceConfig.immortal.contains(ForgeRegistries.ENTITIES.getKey(dude.get().getType()).toString()));
            if (!ResourceConfig.immortalWL == reg) {
                burnout = 0;
            }
        }
    }

    @Override
    public void addWounding(float amount) {
        setWounding(wounding + amount);
    }

    @Override
    public void addFatigue(float amount) {
        setFatigue(fatigue + amount);
    }

    @Override
    public void addBurnout(float amount) {
        setBurnout(burnout + amount);
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
                mBind -= Math.min(amount, mBind);
            case OFF_HAND:
                oBind -= Math.min(amount, oBind);
        }
    }

    @Override
    public float getHandReel(Hand hand) {
        if (hand == Hand.OFF_HAND)
            return offReel;
        return mainReel;
    }

    @Override
    public void setHandReel(Hand hand, float value) {
        if (hand == Hand.OFF_HAND)
            offReel = value;
        else mainReel = value;
    }

    @Override
    public boolean consumeShatter(float value) {
        shattering = true;
        return shatterCD > 0;
    }

    @Override
    public int getShatterCooldown() {
        return shatterCD;
    }

    @Override
    public void setShatterCooldown(int value) {
        shattering = value != 0;
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
    public int getForcedSweep() {
        return sweepAngle;
    }

    @Override
    public void setForcedSweep(int angle) {
        sweepAngle = angle;
    }

    @Override
    public void clientTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        if (prev == null || !ItemStack.areItemStacksEqual(elb.getHeldItemOffhand(), prev)) {
            prev = elb.getHeldItemOffhand();
            ocd = 0;
        }
    }

    @Override
    public void serverTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        int ticks = (int) (elb.world.getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        setTrueMaxPosture(getMPos(elb));
        setTrueMaxSpirit((float) elb.getAttributeValue(WarAttributes.MAX_SPIRIT.get()));
        setMaxMight((float) elb.getAttributeValue(WarAttributes.MAX_MIGHT.get()));
        if (first)
            setPosture(getMaxPosture());
        decrementComboGrace(ticks);
        recoveryTimer -= ticks;
        if (elb.ticksExisted % 5 == 0)
            motion = elb.getPositionVec();
        int qiExtra = decrementMightGrace(ticks);
        int spExtra = decrementSpiritGrace(ticks);
        int poExtra = decrementPostureGrace(ticks);
        for (Hand h : Hand.values()) {
            decrementHandBind(h, ticks);
            if (getHandBind(h) != 0)
                CombatUtils.setHandCooldown(elb, h, 0, true);
        }
        addOffhandCooldown(ticks);
        decrementRollTime(ticks);
        decrementShieldTime(ticks);
        decrementStaggerTime(ticks);
        //check max posture, max spirit, decrement bind and offhand cooldown
        if (getPostureGrace() == 0 && getStaggerTime() == 0 && getPosture() < getMaxPosture()) {
            addPosture(getPPT() * (poExtra));
        }
        float nausea = elb instanceof PlayerEntity || !elb.isPotionActive(Effects.NAUSEA) ? 0 : (elb.getActivePotionEffect(Effects.NAUSEA).getAmplifier() + 1) * GeneralConfig.nausea;
        addPosture(-nausea * ticks);
        if (shatterCD <= 0) {
            shatterCD += ticks;
            if (shatterCD >= 0) {
                shattering = false;
                shatterCD = (int) GeneralUtils.getAttributeValueSafe(elb, WarAttributes.SHATTER.get());
            }
        } else if (shattering) {
            shatterCD -= ticks;
            if (shatterCD <= 0) {
                shatterCD = -ResourceConfig.shatterCooldown;
            }
        } else shatterCD = (int) GeneralUtils.getAttributeValueSafe(elb, WarAttributes.SHATTER.get());
        if (getSpiritGrace() == 0 && getStaggerTime() == 0 && getSpirit() < getMaxSpirit()) {
            addSpirit(getSPT() * spExtra);
        }
        if (getMightGrace() == 0) {
            float over = qiExtra * 0.01f;
            setMight(getMight() - over);
        }
        if (getComboGrace() == 0) {
            if (combo >= 9) combo = 9;
            if (combo >= 4.08) combo -= 0.05;
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
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientPacket(elb.getEntityId(), quickWrite()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) elb), new UpdateClientPacket(elb.getEntityId(), write()));

    }

    @Override
    public ItemStack getTempItemStack() {
        return tempOffhand;
    }

    @Override
    public void setTempItemStack(ItemStack is) {
        tempOffhand = is == null ? ItemStack.EMPTY : is;
    }

    @Override
    public void read(CompoundNBT c) {
        int temp = roll;
        setPosture(c.getFloat("posture"));
        setTrueMaxPosture(c.getFloat("maxpos"));
        setFatigue(c.getFloat("fatigue"));
        setStaggerTime(c.getInt("staggert"));
        lastUpdate = c.getLong("lastUpdate");
        setShatterCooldown(c.getInt("shattercd"));
        if (!c.contains("qi")) return;
        setMight(c.getFloat("qi"));
        setCombo(c.getFloat("combo"));
        setSpirit(c.getFloat("spirit"));
        setTrueMaxSpirit(c.getFloat("maxspi"));
        setMaxMight(c.getFloat("maxmight"));
        setBurnout(c.getFloat("burnout"));
        setWounding(c.getFloat("wounding"));
        setComboGrace(c.getInt("combocd"));
        setMightGrace(c.getInt("qicd"));
        setPostureGrace(c.getInt("posturecd"));
        setSpiritGrace(c.getInt("spiritcd"));
        setShieldTime(c.getInt("shield"));
        int count = c.getInt("staggerc");
        setStaggerCount(staggerc);
        setOffhandCooldown(c.getInt("offhandcd"));
        setRollTime(c.getInt("roll"));
        setHandBind(Hand.MAIN_HAND, c.getInt("mainBind"));
        setHandBind(Hand.OFF_HAND, c.getInt("offBind"));
        setOffhandAttack(c.getBoolean("offhand"));
        toggleCombatMode(c.getBoolean("combat"));
        setShieldCount(c.getInt("shieldC"));
        setForcedSweep(c.getInt("sweep"));
        setHandReel(Hand.MAIN_HAND, c.getFloat("mainReel"));
        setHandReel(Hand.OFF_HAND, c.getFloat("offReel"));
        recoveryTimer = c.getInt("stumble");
        first = c.getBoolean("first");
        parrying = c.getInt("parrying");
        setTempItemStack(ItemStack.read(c.getCompound("temp")));
        if (dude.get() instanceof PlayerEntity) {
            if (getRollTime() > CombatConfig.rollEndsAt && c.getBoolean("rolling"))
                ((PlayerEntity) dude.get()).setForcedPose(Pose.SLEEPING);
            else if (temp == (CombatConfig.rollEndsAt))
                ((PlayerEntity) dude.get()).setForcedPose(null);
        }
    }

    @Override
    public int getParryingTick() {
        return parrying;
    }

    @Override
    public void setParryingTick(int parrying) {
        this.parrying = parrying;
    }

    @Override
    public int getSweepTick() {
        return sweeping;
    }

    @Override
    public void setSweepTick(int tick) {
        sweeping = tick;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Vector3d getMotionConsistently() {
        if (dude.get() == null || motion == null) return Vector3d.ZERO;
        return dude.get().getPositionVec().subtract(motion).scale(0.25);
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
        c.putFloat("maxmight", getMaxMight());
        c.putInt("combocd", getComboGrace());
        c.putInt("qicd", getMightGrace());
        c.putInt("posturecd", getPostureGrace());
        c.putInt("spiritcd", getSpiritGrace());
        c.putInt("shield", getShieldTime());
        c.putInt("staggerc", getStaggerCount());
        c.putInt("staggert", getStaggerTime());
        c.putInt("offhandcd", getOffhandCooldown());
        c.putInt("roll", getRollTime());
        c.putInt("mainBind", getHandBind(Hand.MAIN_HAND));
        c.putInt("offBind", getHandBind(Hand.OFF_HAND));
        c.putFloat("mainReel", getHandReel(Hand.MAIN_HAND));
        c.putFloat("offReel", getHandReel(Hand.OFF_HAND));
        c.putBoolean("offhand", isOffhandAttack());
        c.putBoolean("combat", isCombatMode());
        c.putLong("lastUpdate", lastUpdate);
        c.putInt("shieldC", sc);
        c.putBoolean("first", first);
        c.putInt("parrying", parrying);
        c.putInt("shattercd", getShatterCooldown());
        c.putInt("sweep", getForcedSweep());
        c.putInt("stumble", recoveryTimer);
        c.putBoolean("rolling", dude.get() instanceof PlayerEntity && ((PlayerEntity) dude.get()).getForcedPose() == Pose.SLEEPING);
        if (!tempOffhand.isEmpty())
            c.put("temp", tempOffhand.write(new CompoundNBT()));
        return c;
    }

    public CompoundNBT quickWrite() {
        CompoundNBT c = new CompoundNBT();
        c.putFloat("posture", getPosture());
        c.putFloat("maxpos", getTrueMaxPosture());
        c.putFloat("fatigue", getFatigue());
        c.putInt("staggert", getStaggerTime());
        c.putLong("lastUpdate", lastUpdate);
        c.putInt("shattercd", getShatterCooldown());
        return c;
    }

    private float getPPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.isPotionActive(Effects.POISON) ? (elb.getActivePotionEffect(Effects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.isPotionActive(WarEffects.EXHAUSTION.get()) ? 1 - elb.getActivePotionEffect(WarEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        float armorMod = 2.5f + Math.min(elb.getTotalArmorValue(), 20) * 0.125f;
        float cooldownMod = Math.min(CombatUtils.getCooledAttackStrength(elb, Hand.MAIN_HAND, 0.5f), CombatUtils.getCooledAttackStrength(elb, Hand.MAIN_HAND, 0.5f));
        float healthMod = 0.25f + elb.getHealth() / elb.getMaxHealth() * 0.75f;
        if(CasterData.getCap(elb).isSkillUsable(WarSkills.BOULDER_BRACE.get())){
            armorMod=2.5f;
            healthMod=1;
        }
        float recovery = 0;
        if (recoveryTimer <= CombatConfig.recovery && recoveryTimer > 0 && posture < getMaxPosture() * CombatConfig.posCap) {
            recovery = (getMaxPosture() * CombatConfig.posCap) / CombatConfig.recovery;
        }
        //Vector3d spd = elb.getMotion();
        //float speedMod = (float) Math.min(1, 0.007f / (spd.x * spd.x + spd.z * spd.z));
//        if (getStaggerTime() > 0) {
//            return getMaxPosture() * armorMod * speedMod * healthMod / (1.5f * ResourceConfig.staggerDuration);
//        }
        //0.2f
        final float ret = (((getMaxPosture() / (armorMod * 20)) * cooldownMod) + recovery) * exhaustMod * healthMod * poison;
        GainPostureEvent ev = new GainPostureEvent(elb, ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }

    private float getSPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.isPotionActive(Effects.POISON) ? (elb.getActivePotionEffect(Effects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.isPotionActive(WarEffects.EXHAUSTION.get()) ? 1 - elb.getActivePotionEffect(WarEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        float armorMod = 5f + Math.min(elb.getTotalArmorValue(), 20) * 0.25f;
        float healthMod = 0.25f + elb.getHealth() / elb.getMaxHealth() * 0.75f;
        final float ret = (getMaxSpirit() / (armorMod * 20)) * exhaustMod * healthMod * poison;
        RegenSpiritEvent ev = new RegenSpiritEvent(elb, ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }
}
