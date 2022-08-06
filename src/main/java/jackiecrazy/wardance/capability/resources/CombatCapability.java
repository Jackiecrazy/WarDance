package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.event.*;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateClientPacket;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.eventbus.api.Event;
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
    int lastRangeTick = 0;
    private ItemStack prev;
    private float might, spirit, posture, rank, mpos, mspi, wounding, burnout, fatigue, mainReel, offReel, maxMight, resolve, barrier, mbar, vision;
    private int shatterCD;
    private int qcd, scd, pcd, ccd;
    private int mBind;
    private int oBind;
    private int retina;
    private int staggert, mstaggert, mstaggerc, staggerc, offhandcd, barriercd, roll, sweepAngle = -1;
    private boolean offhand, combat;
    private long lastUpdate;
    private boolean first, shattering, shieldDown;
    private float cache;//no need to save this because it'll be used within the span of a tick
    private int parrying;
    private long staggerTickExisted;
    private int sweeping;
    private int adrenaline;
    private ItemStack tempOffhand = ItemStack.EMPTY;
    private Vector3d motion;

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
        setTrueMaxSpirit((float) GeneralUtils.getAttributeValueSafe(e, WarAttributes.MAX_SPIRIT.get()));
    }

    private static float getMPos(LivingEntity elb) {
        float ret = 1;
        if (elb == null) return ret;
        if (GeneralUtils.getResourceLocationFromEntity(elb) != null && CombatUtils.customPosture.containsKey(GeneralUtils.getResourceLocationFromEntity(elb)))
            ret = CombatUtils.customPosture.get(GeneralUtils.getResourceLocationFromEntity(elb));
        else ret = (float) (Math.ceil(10 / 1.09 * elb.getBbWidth() * elb.getBbHeight()) + elb.getArmorValue() / 2d);
        if (elb instanceof PlayerEntity) ret *= 1.5;
        ret += GeneralUtils.getAttributeValueSafe(elb, WarAttributes.MAX_POSTURE.get());
        return ret;
    }

    @Override
    public float getResolve() {
        return resolve;
    }

    @Override
    public void setResolve(float amount) {
        resolve = amount;
    }

    @Override
    public float getMight() {
        return might;
    }

    @Override
    public void setMight(float amount) {
        float cap = maxMight;
        if (!Float.isFinite(might)) might = 0;
        else might = MathHelper.clamp(amount, 0, cap);
    }

    @Override
    public float addMight(float amount) {
        GainMightEvent gme = new GainMightEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(gme);
        if (gme.isCanceled()) return -1;
        amount = gme.getQuantity();
        float temp = might + amount;
        setMight(temp);
        setMightGrace(ResourceConfig.qiGrace);
        addRank(amount * 0.1f);
        return temp % 10;
    }

    @Override
    public boolean consumeMight(float amount, float above) {
        ConsumeMightEvent cse = new ConsumeMightEvent(dude.get(), amount, above);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getAmount();
        final boolean lacking = might - amount < above;
        if (cse.isCanceled()) {
            return cse.getResult() == Event.Result.ALLOW || (cse.getResult() != Event.Result.DENY && !lacking);
        }

        if (cse.getResult() == Event.Result.DEFAULT && lacking) return false;
        amount = Math.min(amount, might - above);
        might -= amount;
        return cse.getResult() != Event.Result.DENY;
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
        if (!Float.isFinite(spirit)) spirit = 0;
        else spirit = MathHelper.clamp(amount, 0, getMaxSpirit());
    }

    @Override
    public float addSpirit(float amount) {
        float overflow = Math.max(0, spirit + amount - mspi);
        setSpirit(spirit + amount);
        return overflow;
    }

    @Override
    public boolean consumeSpirit(float amount, float above) {
        ConsumeSpiritEvent cse = new ConsumeSpiritEvent(dude.get(), amount, above);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getAmount();
        final boolean lacking = spirit - amount < above;
        if (cse.isCanceled()) {
            return cse.getResult() == Event.Result.ALLOW || (cse.getResult() != Event.Result.DENY && !lacking);
        }

        if (cse.getResult() == Event.Result.DEFAULT && lacking) return false;
        amount = Math.min(amount, spirit - above);
        spirit -= amount;
        setBurnout(this.getBurnout() + amount * ResourceConfig.burnout);
        setSpiritGrace(ResourceConfig.spiritCD);
        return cse.getResult() != Event.Result.DENY;
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
        if (!Float.isFinite(posture)) posture = 0;
        else posture = MathHelper.clamp(amount, 0, getMaxPosture());
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
        return dude.get().tickCount == staggerTickExisted;
    }

    @Override
    public float consumePosture(LivingEntity assailant, float amount, float above, boolean force) {
        float ret = 0;
        LivingEntity elb = dude.get();
        if (elb == null) return ret;
        //staggered already, no more posture damage
        if (staggert > 0) return amount;
        if (!Float.isFinite(posture)) posture = getMaxPosture();
        //event for oodles of compat
        ConsumePostureEvent cpe = new ConsumePostureEvent(elb, assailant, amount, above);
        MinecraftForge.EVENT_BUS.post(cpe);
        //cancel consumption if... canceled
        if (cpe.isCanceled()) return 0;
        amount = cpe.getAmount();
        //test if posture forced to breach cap
        if (cpe.getResult() != Event.Result.ALLOW && amount > getTrueMaxPosture() * CombatConfig.posCap && !force) {
            //hard cap per attack, knock back
            ret = amount - getTrueMaxPosture() * CombatConfig.posCap;
            amount = getTrueMaxPosture() * CombatConfig.posCap;
        }
        if (elb.hasEffect(Effects.DAMAGE_RESISTANCE) && GeneralConfig.resistance)
            amount *= (1 - (elb.getEffect(Effects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 0.2f);
        if (above > 0 && posture - amount < above) {
            //posture floor, set and bypass stagger test
            ret = amount - above;
            amount = posture - above;
        } else if (posture - amount < 0) {
            posture = 0;
            StaggerEvent se = new StaggerEvent(elb, assailant, CombatConfig.staggerDurationMin + Math.max(0, (int) ((elb.getMaxHealth() - elb.getHealth()) / elb.getMaxHealth() * (CombatConfig.staggerDuration - CombatConfig.staggerDurationMin))), CombatConfig.staggerHits);
            MinecraftForge.EVENT_BUS.post(se);
            if (se.isCanceled()) return 0f;
            setStaggerCount(se.getCount());
            setStaggerTime(se.getLength());
            elb.level.playSound(null, elb.getX(), elb.getY(), elb.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            elb.removeVehicle();
            for (Entity rider : elb.getPassengers())
                rider.removeVehicle();
            staggerTickExisted = elb.tickCount;
            return -1f;
        }
        float weakness = 1;
        if (elb.hasEffect(Effects.HUNGER))
            for (int uwu = 0; uwu < elb.getEffect(Effects.HUNGER).getAmplifier() + 1; uwu++)
                weakness *= GeneralConfig.hunger;
        float cooldown = ResourceConfig.postureCD * weakness;
        cooldown += ResourceConfig.armorPostureCD * elb.getArmorValue() / 20f;
        posture -= amount;
        addFatigue(amount * ResourceConfig.fatigue);
        setPostureGrace((int) cooldown);
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
    public float getRank() {
        return rank;
    }

    @Override
    public void setRank(float amount) {
//        if (WarCompat.elenaiDodge && dude.get() instanceof ServerPlayerEntity && ResourceConfig.elenaiC) {
//            ElenaiCompat.manipulateRegenTime(dude.get(), );
//        }
        if (!Float.isFinite(rank)) rank = 0;
        else rank = MathHelper.clamp(amount, 0, 10);
    }

    @Override
    public void setAdrenalineCooldown(int amount) {
        adrenaline = amount;
    }

    @Override
    public boolean halvedAdrenaline() {
        return adrenaline != 0;
    }

    @Override
    public float addRank(float amount) {
        float overflow = Math.max(0, rank + amount - 10);
        setRank(rank + amount);
        return overflow;
    }

    @Override
    public boolean consumeRank(float amount, float above) {
        if (rank - amount < above) return false;
        rank -= amount;
        return true;
    }

    @Override
    public float getTrueMaxPosture() {
        if (mpos == 0) mpos = getMPos(dude.get());
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
        if (mspi == 0) return 10;
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
    public int getMaxStaggerTime() {
        return mstaggert;
    }

    private void setComboGrace(int amount) {
        ccd = amount;
    }

    private int decrementComboGrace(int amount) {
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
            mstaggert = 0;
        } else if (dude.get() != null && amount > 0 && staggert == 0) {
            LivingEntity elb = dude.get();
            elb.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            elb.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(STAGGERS);
            elb.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            elb.getAttribute(Attributes.ARMOR).addPermanentModifier(STAGGERA);
            elb.getAttribute(Attributes.ARMOR).removeModifier(MORE);
            elb.getAttribute(Attributes.ARMOR).addPermanentModifier(STAGGERSA);
        }
        mstaggert = Math.max(mstaggert, amount);
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
            staggert = mstaggert = 0;
            return -temp;
        }
        return 0;
    }

    @Override
    public int getMaxStaggerCount() {
        return mstaggerc;
    }

    @Override
    public int getStaggerCount() {
        return staggerc;
    }

    @Override
    public void setStaggerCount(int amount) {
        mstaggerc = Math.max(mstaggerc, amount);
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
            mstaggerc = 0;
        }
    }

    @Override
    public float getMaxBarrier() {
        return mbar;
    }

    @Override
    public void setMaxBarrier(float amount) {
        mbar = amount;
    }

    @Override
    public int getBarrierCooldown() {
        return barriercd;
    }

    @Override
    public void setBarrierCooldown(int amount) {
        barriercd = amount;
    }

    @Override
    public void decrementBarrierCooldown(int amount) {
        if (barriercd - amount > 0)
            barriercd -= amount;
        else {
            barriercd = 0;
            //setBarrier(0);
        }
    }

    @Override
    public float getBarrier() {
        return barrier;
    }

    @Override
    public void setBarrier(float amount) {
        barrier = amount;
    }

    @Override
    public float consumeBarrier(float amount) {
        if (shieldDown) return 0;
        float prev;
        if (barrier - amount > 0) {
            prev = amount;
            barrier -= amount;
        } else {
            prev = barrier;
            barrier = 0;
            shieldDown = true;
        }
        setBarrierCooldown((int) (dude.get().getAttributeValue(WarAttributes.BARRIER_COOLDOWN.get())));
        return prev;
    }

    @Override
    public void addBarrier(float amount) {
        barrier += amount;
        final float max = getMaxBarrier();
        if (barrier > max) {
            barrier = max;
            shieldDown = max == 0;
        }
    }

    @Override
    public int getOffhandCooldown() {
        return offhandcd;
    }

    @Override
    public void setOffhandCooldown(int amount) {
        offhandcd = amount;
    }

    @Override
    public void addOffhandCooldown(int amount) {
        offhandcd += amount;
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
            dude.get().getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(WOUND, "wounding", -wounding, AttributeModifier.Operation.ADDITION));
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
                break;
            case OFF_HAND:
                oBind = amount;
                break;
        }
    }

    @Override
    public void decrementHandBind(Hand h, int amount) {
        switch (h) {
            case MAIN_HAND:
                mBind -= Math.min(amount, mBind);
                break;
            case OFF_HAND:
                oBind -= Math.min(amount, oBind);
                break;
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
        if (prev == null || !ItemStack.matches(elb.getOffhandItem(), prev)) {
            prev = elb.getOffhandItem();
            offhandcd = 0;
        }
    }

    @Override
    public void serverTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        final int ticks = (int) (elb.level.getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        //update max values
        vision = (float) elb.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (elb.hasEffect(WarEffects.SLEEP.get()) || elb.hasEffect(WarEffects.PARALYSIS.get()) || elb.hasEffect(WarEffects.PETRIFY.get()))
            vision = -1;
        setTrueMaxPosture(getMPos(elb));
        setTrueMaxSpirit((float) elb.getAttributeValue(WarAttributes.MAX_SPIRIT.get()));
        setMaxMight((float) elb.getAttributeValue(WarAttributes.MAX_MIGHT.get()));
        setMaxBarrier((float) (elb.getAttributeValue(WarAttributes.BARRIER.get()) * getMaxPosture()));
        //update internal retina values
        int light = StealthUtils.getActualLightLevel(elb.level, elb.blockPosition());
        for (long x = lastUpdate + ticks; x > lastUpdate; x--) {
            if (x % 3 == 0) {
                if (light > retina)
                    retina++;
                if (light < retina)
                    retina--;
            }
        }
        //initialize posture
        if (first)
            setPosture(getMaxPosture());
        //store motion for further use
        if (ticks > 5 || (lastUpdate + ticks) % 5 != lastUpdate % 5)
            motion = elb.position();
        //tick down everything
        if (adrenaline > 0)
            adrenaline -= Math.min(adrenaline, ticks);
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
        decrementBarrierCooldown(ticks);
        decrementStaggerTime(ticks);
        //regenerate posture
        if (getPostureGrace() == 0 && getStaggerTime() == 0 && getPosture() < getMaxPosture()) {
            addPosture(getPPT() * (poExtra));
        }
        //regenerate barrier
        if (getBarrierCooldown() == 0 && getStaggerTime() == 0) {
            addBarrier(getBPT() * (ticks));
        }
        //enforce shieldlessness without barrier
        if (getMaxBarrier() == 0) shieldDown = true;
        //disable shields if barrier still down
        if (shieldDown) {
            for (Hand h : Hand.values()) {
                if (CombatUtils.isShield(elb, elb.getItemInHand(h)) && getHandBind(h) < 5) {
                    setHandBind(h, 7);
                }
            }
        }
        float nausea = elb instanceof PlayerEntity || !elb.hasEffect(Effects.CONFUSION) ? 0 : (elb.getEffect(Effects.CONFUSION).getAmplifier() + 1) * GeneralConfig.nausea;
        addPosture(-nausea * ticks);
        //shatter handling
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
        //might decay
        if (getMightGrace() == 0) {
            float over = qiExtra * 0.01f;
            float decay = getMight();
            setMight(getMight() - over);
            decay -= getMight();
            setResolve(resolve + decay);
//            final float heal = over * -0.05f * (getMight()+2)/2;
//            addWounding(heal);
//            addFatigue(heal);
//            addBurnout(heal);
        }
        //reduce rank
        if (might == 0) {
            float decay = 0.01f;
            if (getComboRank() == 6)
                decay = 0.025f;
            if (getComboRank() == 7)
                decay = 0.05f;
            decay *= ticks;
            rank -= decay;
            if (rank < 0 && elb instanceof PlayerEntity) {
                rank = 0;
                addFatigue(-decay);
                addWounding(-decay);
                addBurnout(-decay);
            }
        }
        if (prev == null || !ItemStack.matches(elb.getOffhandItem(), prev)) {
            prev = elb.getOffhandItem();
            setOffhandCooldown(0);
        }
        lastUpdate = elb.level.getGameTime();
        first = false;
        sync();
    }

    @Override
    public void sync() {

        LivingEntity elb = dude.get();
        if (elb == null || elb.level.isClientSide) return;
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientPacket(elb.getId(), quickWrite()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) elb), new UpdateClientPacket(elb.getId(), write()));

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
        setTrueMaxSpirit(c.getFloat("maxspi"));
        setMaxMight(c.getFloat("maxmight"));
        setBurnout(c.getFloat("burnout"));
        setWounding(c.getFloat("wounding"));
        setTrueMaxPosture(c.getFloat("maxpos"));
        setPosture(c.getFloat("posture"));
        setFatigue(c.getFloat("fatigue"));
        setStaggerTime(c.getInt("staggert"));
        lastUpdate = c.getLong("lastUpdate");
        setShatterCooldown(c.getInt("shattercd"));
        setMaxBarrier(c.getFloat("maxBarrier"));
        setBarrier(c.getFloat("barrier"));
        mstaggerc = c.getInt("mstaggerc");
        mstaggert = c.getInt("mstaggert");
        setStaggerCount(c.getInt("staggerc"));
        retina = c.getInt("retina");
        vision = c.getFloat("vision");
        shieldDown=c.getBoolean("shieldDown");
        if (!c.contains("qi")) return;
        setMight(c.getFloat("qi"));
        setResolve(c.getFloat("resolve"));
        setRank(c.getFloat("combo"));
        setSpirit(c.getFloat("spirit"));
        setMightGrace(c.getInt("qicd"));
        setPostureGrace(c.getInt("posturecd"));
        setSpiritGrace(c.getInt("spiritcd"));
        setBarrierCooldown(c.getInt("shield"));
        setOffhandCooldown(c.getInt("offhandcd"));
        setRollTime(c.getInt("roll"));
        setHandBind(Hand.MAIN_HAND, c.getInt("mainBind"));
        setHandBind(Hand.OFF_HAND, c.getInt("offBind"));
        setOffhandAttack(c.getBoolean("offhand"));
        toggleCombatMode(c.getBoolean("combat"));
        setForcedSweep(c.getInt("sweep"));
        setHandReel(Hand.MAIN_HAND, c.getFloat("mainReel"));
        setHandReel(Hand.OFF_HAND, c.getFloat("offReel"));
        first = c.getBoolean("first");
        adrenaline = c.getInt("adrenaline");
        parrying = c.getInt("parrying");
        setTempItemStack(ItemStack.of(c.getCompound("temp")));
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
        return dude.get().position().subtract(motion).scale(0.25);
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT c = new CompoundNBT();
        c.putInt("retina", getRetina());
        c.putFloat("vision", visionRange());
        c.putFloat("qi", getMight());
        c.putFloat("resolve", getResolve());
        c.putFloat("posture", getPosture());
        c.putFloat("combo", getRank());
        c.putFloat("spirit", getSpirit());
        c.putFloat("maxpos", getTrueMaxPosture());
        c.putFloat("maxspi", getTrueMaxSpirit());
        c.putFloat("burnout", getBurnout());
        c.putFloat("fatigue", getFatigue());
        c.putFloat("wounding", getWounding());
        c.putFloat("maxmight", getMaxMight());
        c.putInt("qicd", getMightGrace());
        c.putInt("posturecd", getPostureGrace());
        c.putInt("spiritcd", getSpiritGrace());
        c.putInt("shield", getBarrierCooldown());
        c.putInt("mstaggerc", getMaxStaggerCount());
        c.putInt("mstaggert", getMaxStaggerTime());
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
        c.putFloat("maxBarrier", mbar);
        c.putFloat("barrier", barrier);
        c.putBoolean("first", first);
        c.putInt("parrying", parrying);
        c.putInt("adrenaline", adrenaline);
        c.putInt("shattercd", getShatterCooldown());
        c.putInt("sweep", getForcedSweep());
        c.putBoolean("rolling", dude.get() instanceof PlayerEntity && ((PlayerEntity) dude.get()).getForcedPose() == Pose.SLEEPING);
        c.putBoolean("shieldDown", shieldDown);
        if (!tempOffhand.isEmpty())
            c.put("temp", tempOffhand.save(new CompoundNBT()));
        return c;
    }

    @Override
    public void addRangedMight(boolean pass) {
        LivingEntity shooter = dude.get();
        if (shooter == null) return;
        if (pass)
            addMight(MathHelper.clamp((shooter.tickCount - lastRangeTick) * 0.01f, 0, 0.3f));
        lastRangeTick = shooter.tickCount;
    }

    @Override
    public int getRetina() {
        return retina;
    }

    @Override
    public float visionRange() {
        return vision;
    }

    public CompoundNBT quickWrite() {
        CompoundNBT c = new CompoundNBT();
        c.putFloat("posture", getPosture());
        c.putFloat("maxpos", getTrueMaxPosture());
        c.putFloat("fatigue", getFatigue());
        c.putInt("staggert", getStaggerTime());
        c.putInt("mstaggert", getMaxStaggerTime());
        c.putInt("staggerc", getStaggerCount());
        c.putInt("mstaggerc", getMaxStaggerCount());
        c.putLong("lastUpdate", lastUpdate);
        c.putInt("shattercd", getShatterCooldown());
        c.putFloat("maxBarrier", mbar);
        c.putFloat("barrier", barrier);
        c.putInt("retina", retina);
        c.putFloat("vision", vision);
        return c;
    }

    private float getPPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.hasEffect(Effects.POISON) ? (elb.getEffect(Effects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.hasEffect(WarEffects.EXHAUSTION.get()) ? 1 - elb.getEffect(WarEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        float armorMod = 2.5f + Math.min(elb.getArmorValue(), 20) * 0.125f;
        float cooldownMod = Math.min(CombatUtils.getCooledAttackStrength(elb, Hand.MAIN_HAND, 0.5f), CombatUtils.getCooledAttackStrength(elb, Hand.MAIN_HAND, 0.5f));
        float healthMod = 0.25f + elb.getHealth() / elb.getMaxHealth() * 0.75f;
        if (CasterData.getCap(elb).isSkillUsable(WarSkills.BOULDER_BRACE.get())) {
            armorMod = 2.5f;
            healthMod = 1;
        }
        //Vector3d spd = elb.getMotion();
        //float speedMod = (float) Math.min(1, 0.007f / (spd.x * spd.x + spd.z * spd.z));
//        if (getStaggerTime() > 0) {
//            return getMaxPosture() * armorMod * speedMod * healthMod / (1.5f * ResourceConfig.staggerDuration);
//        }
        //0.2f
        final float ret = (((getMaxPosture() / (armorMod * 20)) * cooldownMod)) * exhaustMod * healthMod * poison;
        GainPostureEvent ev = new GainPostureEvent(elb, ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }

    private float getBPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.hasEffect(Effects.POISON) ? (elb.getEffect(Effects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.hasEffect(WarEffects.EXHAUSTION.get()) ? 1 - elb.getEffect(WarEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        float armorMod = 2.5f + Math.min(elb.getArmorValue(), 20) * 0.125f;
        float healthMod = 0.25f + elb.getHealth() / elb.getMaxHealth() * 0.75f;
        if (CasterData.getCap(elb).isSkillUsable(WarSkills.BOULDER_BRACE.get())) {
            armorMod = 2.5f;
            healthMod = 1;
        }
        //Vector3d spd = elb.getMotion();
        //float speedMod = (float) Math.min(1, 0.007f / (spd.x * spd.x + spd.z * spd.z));
//        if (getStaggerTime() > 0) {
//            return getMaxPosture() * armorMod * speedMod * healthMod / (1.5f * ResourceConfig.staggerDuration);
//        }
        //0.2f
        final float ret = (((getMaxPosture() / (armorMod * 20)))) * exhaustMod * healthMod * poison;
        GainBarrierEvent ev = new GainBarrierEvent(elb, ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }

    private float getSPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.hasEffect(Effects.POISON) ? (elb.getEffect(Effects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.hasEffect(WarEffects.EXHAUSTION.get()) ? 1 - elb.getEffect(WarEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        float armorMod = 5f + Math.min(elb.getArmorValue(), 20) * 0.25f;
        //float healthMod = 0.25f + elb.getHealth() / elb.getMaxHealth() * 0.75f;
        final float ret = (getMaxSpirit() / (armorMod * 20)) * exhaustMod * poison;
        RegenSpiritEvent ev = new RegenSpiritEvent(elb, ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }
}
