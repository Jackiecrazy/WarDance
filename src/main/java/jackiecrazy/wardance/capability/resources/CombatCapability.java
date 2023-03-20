package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.*;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateClientPacket;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class CombatCapability implements ICombatCapability {

    /*
    posture is a long bar that is almost guaranteed to be lethal when broken. It recharges at a steady pace after not being consumed for a few moments. When staggered your posture will rapidly regenerate until full again, though stagger will immediately break after 1 attack
    breach is a small bar on the posture bar that records posture damage from the last few seconds, decaying constantly. If it exceeds a threshold, the entity is stunned and bound for a brief moment, which is also broken by attacks, and until breach finishes cannot be breached again
    greatly reduce attack knockback to encourage use of breach, but chaff mobs can be breached easily
    remove rank, decay, stagger count, barrier, and armor association with max posture
    rank effects rolled into base might/fury functionality, skill note: max fury can auto-cast buff or manual (i.e. core style can be active or passive)
    spirit is a line of symbols, 3-5, which are consumed in increments of 1 to cast certain skills
    might/fury is a bar that charges up to twice, and can either be automatically consumed by certain passives or manually expended to cast powerful skills

     */

    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    private static final AttributeModifier EXPOSEA = new AttributeModifier(WOUND, "expose penalty", -10, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier STAGGER = new AttributeModifier(WOUND, "stagger penalty", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private final WeakReference<LivingEntity> dude;
    int lastRangeTick = 0;
    HashMap<UUID, Integer> fractures = new HashMap<>();
    /**
     * only used on the client
     */
    ArrayList<LivingEntity> fractureQueue = new ArrayList<>();
    private ItemStack prev;
    private float might, spirit, posture, rank, vision;
    private int shatterCD;
    private int qcd, scd, pcd, ccd;
    private int mBind, oBind;
    private int staggert, mstaggert, offhandcd, roll, expose, mexpose, sweepAngle = -1;
    private float mpos, mspi, mmight, mfrac;
    private boolean offhand, combat, painful;
    private long lastUpdate;
    private boolean first = true, client, fractureDirty = true;
    private float cache;//no need to save this because it'll be used within the span of a tick
    private int parrying, retina, fracount;
    private long staggerTickExisted;
    private int sweeping;
    private int adrenaline;
    private ItemStack tempOffhand = ItemStack.EMPTY;
    private Vec3 motion;
    private int internal_fracture_timer = 10;

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
    }

    private static float getMPos(LivingEntity elb) {
        float ret = 1;
        if (elb == null) return ret;
        if (GeneralUtils.getResourceLocationFromEntity(elb) != null && CombatUtils.customPosture.containsKey(GeneralUtils.getResourceLocationFromEntity(elb)))
            ret = CombatUtils.customPosture.get(GeneralUtils.getResourceLocationFromEntity(elb));
        else ret = (float) (Math.ceil(10 / 1.09 * Math.sqrt(elb.getBbWidth() * elb.getBbHeight())));
        if (elb instanceof Player) ret *= 1.5;
        return ret;
    }

    @Override
    public void updateDefenselessStatus() {
        if (!painful) return;
        painful = false;
        setPosture(getMaxPosture());
        LivingEntity e = dude.get();
        e.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
        if (e.getAttribute(Attributes.FLYING_SPEED) != null)
            e.getAttribute(Attributes.FLYING_SPEED).removeModifier(WOUND);
        e.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
    }

    @Override
    public float getMaxMight() {
        return mmight;
    }

    @Override
    public float getMight() {
        return might;
    }

    @Override
    public void setMight(float amount) {
        float cap = getMaxMight();
        if (!Float.isFinite(might)) might = 0;
        else might = Mth.clamp(amount, 0, cap);
    }

    @Override
    public float addMight(float amount) {
        double grace = ResourceConfig.qiGrace;
        if (dude.get() != null) {
            amount *= dude.get().getAttributeValue(FootworkAttributes.MIGHT_GEN.get());
            grace *= dude.get().getAttributeValue(FootworkAttributes.MIGHT_GRACE.get());
        }
        GainMightEvent gme = new GainMightEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(gme);
        if (gme.isCanceled()) return -1;
        amount = gme.getQuantity();
        float temp = might + amount;
        setMight(temp);
        setMightGrace((int) grace);
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
    public float getMaxSpirit() {
        return mspi;
    }

    @Override
    public float getSpirit() {
        return spirit;
    }

    @Override
    public void setSpirit(float amount) {
        if (!Float.isFinite(spirit)) spirit = 0;
        else spirit = Mth.clamp(amount, 0, getMaxSpirit());
    }

    @Override
    public float addSpirit(float amount) {
        if (dude.get() != null)
            amount *= dude.get().getAttributeValue(FootworkAttributes.SPIRIT_GAIN.get());
        GainSpiritEvent cse = new GainSpiritEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getQuantity();
        float overflow = Math.max(0, spirit + amount - getMaxSpirit());
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
        double cd = ResourceConfig.spiritCD;
        if (dude.get() != null)
            cd /= dude.get().getAttributeValue(FootworkAttributes.SPIRIT_COOLDOWN.get());
        setSpiritGrace((int) cd);
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
    public float getMaxPosture() {
        return mpos;
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
        else posture = Mth.clamp(amount, 0, getMaxPosture());
    }

    @Override
    public float addPosture(float amount) {
        if (dude.get() != null)
            amount *= dude.get().getAttributeValue(FootworkAttributes.POSTURE_GAIN.get());
        GainPostureEvent cse = new GainPostureEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getQuantity();
        float overflow = Math.max(0, posture + amount - getMaxPosture());
        setPosture(posture + amount);
        return overflow;
    }

    @Override
    public float consumePosture(LivingEntity assailant, float amount, float above, boolean force) {
        float ret = 0;
        LivingEntity elb = dude.get();
        if (elb == null) return ret;
        //staggered already, no more posture damage
        if (getStaggerTime() > 0 || getExposeTime() > 0) return amount;
        if (!Float.isFinite(posture)) posture = getMaxPosture();
        //event for oodles of compat
        ConsumePostureEvent cpe = new ConsumePostureEvent(elb, assailant, amount, above);
        MinecraftForge.EVENT_BUS.post(cpe);
        //cancel consumption if... canceled
        if (cpe.isCanceled()) return 0;
        amount = cpe.getAmount();
        //test if posture forced to breach cap UPDATE: no more hard caps in 1.19!
//        if (cpe.getResult() != Event.Result.ALLOW && amount > getTrueMaxPosture() * CombatConfig.posCap && !force) {
//            //hard cap per attack, knock back
//            ret = amount - getTrueMaxPosture() * CombatConfig.posCap;
//            amount = getTrueMaxPosture() * CombatConfig.posCap;
//        }
        if (elb.hasEffect(MobEffects.DAMAGE_RESISTANCE) && GeneralConfig.resistance)
            amount *= (1 - (elb.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 0.2f);
        if (above > 0 && posture - amount < above) {
            //posture floor, set and bypass stagger test
            ret = amount - above;
            amount = posture - above;
        } else if (posture - amount < 0) {
            posture = 0;
            if (addFracture(assailant, 1)) {
                StaggerEvent se = new StaggerEvent(elb, assailant, CombatConfig.staggerDuration);
                MinecraftForge.EVENT_BUS.post(se);
                if (se.isCanceled()) return 0f;
                stagger(se.getLength());
                elb.level.playSound(null, elb.getX(), elb.getY(), elb.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
            elb.removeVehicle();
            for (Entity rider : elb.getPassengers())
                rider.removeVehicle();
            staggerTickExisted = elb.tickCount;
            return -1f;
        }
        float weakness = 1;
        if (elb.hasEffect(MobEffects.HUNGER))
            for (int uwu = 0; uwu < elb.getEffect(MobEffects.HUNGER).getAmplifier() + 1; uwu++)
                weakness *= GeneralConfig.hunger;
        double cooldown = ResourceConfig.postureCD * weakness / elb.getAttributeValue(FootworkAttributes.POSTURE_COOLDOWN.get());
        posture -= amount;
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
    public int getMaxStaggerTime() {
        return mstaggert;
    }

    @Override
    public int getStaggerTime() {
        return staggert;
    }

    @Override
    public void stagger(int time) {
        //leaving stagger
        final LivingEntity e = dude.get();
        if (time == 0 && staggert > 0 && e != null) {
            updateDefenselessStatus();
            mstaggert = 0;
        }//entering stagger
        else if (e != null && time > 0 && staggert == 0) {
            e.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            e.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(STAGGER);

            final AttributeInstance fly = e.getAttribute(Attributes.FLYING_SPEED);
            if (fly != null) {
                fly.removeModifier(WOUND);
                fly.addPermanentModifier(STAGGER);
            }
            painful = true;
        }
        mstaggert = Math.max(mstaggert, time);
        staggert = time;
    }

    @Override
    public boolean isStaggered() {
        return getStaggerTime() > 0 && painful;
    }

    @Override
    public int decrementStaggerTime(int amount) {
        if (staggert - amount > 0)
            staggert -= amount;
        else {
            int temp = staggert;
            if (staggert != 0)
                updateDefenselessStatus();
            mstaggert = staggert = 0;
            return -temp;
        }
        return 0;
    }

    @Override
    public int getFractureCount() {
        int ret = 0;
        for (Integer i : fractures.values())
            ret += i;
        return ret;
    }

    @Override
    public int getFractureCount(LivingEntity livingEntity) {
        return fractures.getOrDefault(livingEntity.getUUID(), 0);
    }

    @Override
    public HashMap<UUID, Integer> getFractureList() {
        return fractures;
    }

    @Override
    public boolean addFracture(@Nullable LivingEntity livingEntity, int i) {
        if (getFractureCount() + i >= getMaxFracture()) {
            ExposeEvent se = new ExposeEvent(dude.get(), livingEntity, CombatConfig.exposeDuration); //magic number
            MinecraftForge.EVENT_BUS.post(se);
            if (se.isCanceled()) return false;
            expose(se.getLength());
            clearFracture(null, false);
            return false;
        }
        if (livingEntity == null)
            fractures.merge(WOUND, i, Integer::sum);
        else fractures.merge(livingEntity.getUUID(), i, Integer::sum);
        fractureDirty = true;
        return true;
    }

    @Override
    public void clearFracture(@Nullable LivingEntity of, boolean clearInvalid) {
        if (clearInvalid) {
            final LivingEntity e = dude.get();
            if (e != null && e.level instanceof ServerLevel server)
                fractures.entrySet().removeIf((id) -> server.getEntity(id.getKey()) == null);
        } else {
            if (of == null) fractures.clear();
        }
        if (of != null)
            fractures.remove(of.getUUID());
        fractureDirty = true;
    }

    @Override
    public float getMaxFracture() {
        return mfrac;
    }

    @Override
    public int getMaxExposeTime() {
        return mexpose;
    }

    @Override
    public int getExposeTime() {
        return expose;
    }

    @Override
    public void expose(int time) {
        final LivingEntity e = dude.get();
        //leaving expose
        if (time == 0 && expose > 0 && e != null) {
            updateDefenselessStatus();
            mexpose = 0;
        }//entering expose
        else if (e != null && time > 0 && expose == 0) {
            e.level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            e.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            e.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(EXPOSEA);

            final AttributeInstance fly = e.getAttribute(Attributes.FLYING_SPEED);
            if (fly != null) {
                fly.removeModifier(WOUND);
                fly.addPermanentModifier(EXPOSEA);
            }
            e.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            e.getAttribute(Attributes.ARMOR).addPermanentModifier(EXPOSEA);
            painful = true;
        }
        mexpose = Math.max(mexpose, time);
        expose = time;
    }

    @Override
    public boolean isExposed() {
        return getExposeTime() > 0 && painful;
    }

    @Override
    public int decrementExposeTime(int amount) {
        if (expose - amount > 0)
            expose -= amount;
        else {
            int temp = expose;
            if (expose != 0)
                updateDefenselessStatus();
            expose = mexpose = 0;
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
        else rank = Mth.clamp(amount, 0, 10);
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
            if (dude.get() instanceof Player p) {
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
    public int getHandBind(InteractionHand h) {
        if (isStaggered() || isExposed()) return 10;
        if (h == InteractionHand.OFF_HAND) {
            return oBind;
        }
        return mBind;
    }

    @Override
    public void setHandBind(InteractionHand h, int amount) {
        switch (h) {
            case MAIN_HAND -> mBind = amount;
            case OFF_HAND -> oBind = amount;
        }
    }

    @Override
    public void decrementHandBind(InteractionHand h, int amount) {
        switch (h) {
            case MAIN_HAND -> mBind -= Math.min(amount, mBind);
            case OFF_HAND -> oBind -= Math.min(amount, oBind);
        }
    }

    @Override
    public boolean consumeShatter(float value) {
        return shatterCD > 0;
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
        mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
        mspi = (float) elb.getAttributeValue(FootworkAttributes.MAX_SPIRIT.get());
        mmight = (float) elb.getAttributeValue(FootworkAttributes.MAX_MIGHT.get());
        mfrac = (float) elb.getAttributeValue(FootworkAttributes.MAX_FRACTURE.get());
        if (elb.hasEffect(FootworkEffects.SLEEP.get()) || elb.hasEffect(FootworkEffects.PARALYSIS.get()) || elb.hasEffect(FootworkEffects.PETRIFY.get()))
            vision = -1;
        //initialize posture
        if (first) {
            final float mPos = getMPos(elb);
            elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).setBaseValue(mPos);
            elb.getAttribute(FootworkAttributes.MAX_FRACTURE.get()).setBaseValue(Math.floor(Math.sqrt(mpos) - 1));
            setPosture(getMaxPosture());
        }
        //store motion for further use
        if (ticks > 5 || (lastUpdate + ticks) % 5 != lastUpdate % 5)
            motion = elb.position();
        //every once in a while clear invalid fractures
        internal_fracture_timer -= ticks;
        if (internal_fracture_timer < 0) {
            clearFracture(null, true);
            internal_fracture_timer = 10;
        }
        //tick down everything
        if (adrenaline > 0)
            adrenaline -= Math.min(adrenaline, ticks);
        int qiExtra = decrementMightGrace(ticks);
        int spExtra = decrementSpiritGrace(ticks);
        int poExtra = decrementPostureGrace(ticks);
        for (InteractionHand h : InteractionHand.values()) {
            decrementHandBind(h, ticks);
            if (getHandBind(h) != 0)
                CombatUtils.setHandCooldown(elb, h, 0, true);
        }
        addOffhandCooldown(ticks);
        decrementRollTime(ticks);
        decrementStaggerTime(ticks);
        decrementExposeTime(ticks);
        //regenerate posture
        if ((getPostureGrace() == 0 || getStaggerTime() > 0 || getExposeTime() > 0) && getPosture() < getMaxPosture()) {
            if (getStaggerTime() > 0 || getExposeTime() > 0)
                setPosture(getMaxPosture() * poExtra / Math.max(getMaxExposeTime(), getMaxStaggerTime()));
            else setPosture(getPosture() + getPPT() * (poExtra));

        }
        if (getPosture() > getMaxPosture())
            setPosture(getMaxPosture());
        //regenerate barrier
//        if (getBarrierCooldown() == 0 && getStaggerTime() == 0) {
//            addBarrier(getBPT() * (ticks));
//        }
        //enforce shieldlessness without barrier
        //if (getMaxBarrier() == 0) shieldDown = true;
        //disable shields if barrier still down
//        if (shieldDown) {
//            for (Hand h : Hand.values()) {
//                if (CombatUtils.isShield(elb, elb.getItemInHand(h)) && getHandBind(h) < 5) {
//                    setHandBind(h, 7);
//                }
//            }
//        }
        float nausea = elb instanceof Player || !elb.hasEffect(MobEffects.CONFUSION) ? 0 : (elb.getEffect(MobEffects.CONFUSION).getAmplifier() + 1) * GeneralConfig.nausea;
        if (nausea > 0) consumePosture(nausea * ticks, 0.1f);
        //shatter handling
//        if (shatterCD <= 0) {
//            shatterCD += ticks;
//            if (shatterCD >= 0) {
//                shattering = false;
//                shatterCD = (int) GeneralUtils.getAttributeValueSafe(elb, FootworkAttributes.SHATTER.get());
//            }
//        } else if (shattering) {
//            shatterCD -= ticks;
//            if (shatterCD <= 0) {
//                shatterCD = -ResourceConfig.shatterCooldown;
//            }
//        } else shatterCD = (int) GeneralUtils.getAttributeValueSafe(elb, FootworkAttributes.SHATTER.get());
        if (getSpiritGrace() == 0 && getStaggerTime() == 0 && getSpirit() < getMaxSpirit()) {
            setSpirit(getSpirit() + getSPT() * spExtra);//to not run into spirit increase mod
        }
        //might decay
        if (getMightGrace() == 0) {
            float over = qiExtra * 0.005f;
            float decay = getMight();
            setMight(getMight() - over);
        }
        //reduce rank
        if (might == 0) {
            float decay = 0.01f;
            if (getComboRank() == 6)
                decay = 0.025f;
            if (getComboRank() == 7)
                decay = 0.05f;
            decay *= ticks;
            consumeRank(decay);
//            if (rank < 0 && elb instanceof PlayerEntity) {
//                rank = 0;
//                addFatigue(-decay);
//                addWounding(-decay);
//                addBurnout(-decay);
//            }
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
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayer sp)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientPacket(elb.getId(), write()));

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
    public void read(CompoundTag c) {
        int temp = roll;
        staggert = (c.getInt("staggert"));
        mstaggert = c.getInt("mstaggert");
        expose = (c.getInt("expose"));
        lastUpdate = c.getLong("lastUpdate");
        mexpose = c.getInt("mexpose");
        mpos = c.getFloat("maxposture");
        mfrac = c.getFloat("maxfracture");
        painful = c.getBoolean("painful");
        setPosture(c.getFloat("posture"));
        if (c.contains("fractures")) {
            fractures.clear();
            CompoundTag t = (CompoundTag) c.get("fractures");
            if (t != null) for (String id : t.getAllKeys()) {
                fractures.put(UUID.fromString(id), t.getInt(id));
            }
        }
        if (!c.contains("qi")) return;
        setShatterCooldown(c.getInt("shattercd"));
        mspi = c.getFloat("maxspirit");
        mmight = c.getFloat("maxmight");
        setMight(c.getFloat("qi"));
        setRank(c.getFloat("combo"));
        setSpirit(c.getFloat("spirit"));
        setMightGrace(c.getInt("qicd"));
        setPostureGrace(c.getInt("posturecd"));
        setSpiritGrace(c.getInt("spiritcd"));
        setOffhandCooldown(c.getInt("offhandcd"));
        setRollTime(c.getInt("roll"));
        setHandBind(InteractionHand.MAIN_HAND, c.getInt("mainBind"));
        setHandBind(InteractionHand.OFF_HAND, c.getInt("offBind"));
        setOffhandAttack(c.getBoolean("offhand"));
        toggleCombatMode(c.getBoolean("combat"));
        setForcedSweep(c.getInt("sweep"));
        first = c.getBoolean("first");
        adrenaline = c.getInt("adrenaline");
        parrying = c.getInt("parrying");
        setTempItemStack(ItemStack.of(c.getCompound("temp")));
        if (dude.get() instanceof Player p) {
            if (getRollTime() > CombatConfig.rollEndsAt && c.getBoolean("rolling"))
                p.setForcedPose(Pose.SLEEPING);
            else if (temp == (CombatConfig.rollEndsAt))
                p.setForcedPose(null);
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
    public Vec3 getMotionConsistently() {
        if (dude.get() == null || motion == null) return Vec3.ZERO;
        return dude.get().position().subtract(motion).scale(0.25);
    }

    @Override
    public CompoundTag write() {
        CompoundTag c = new CompoundTag();
        c.putFloat("vision", visionRange());
        c.putFloat("qi", getMight());
        c.putFloat("posture", getPosture());
        c.putFloat("combo", getRank());
        c.putFloat("spirit", getSpirit());
        c.putFloat("maxmight", getMaxMight());
        c.putFloat("maxposture", getMaxPosture());
        c.putFloat("maxspirit", getMaxSpirit());
        c.putFloat("maxfracture", getMaxFracture());
        CompoundTag fractures = new CompoundTag();
        for (Map.Entry<UUID, Integer> e : getFractureList().entrySet()) {
            fractures.putInt(e.getKey().toString(), e.getValue());
        }
        c.put("fractures", fractures);
        c.putInt("qicd", getMightGrace());
        c.putInt("posturecd", getPostureGrace());
        c.putInt("spiritcd", getSpiritGrace());
        c.putInt("mstaggert", getMaxStaggerTime());
        c.putInt("staggert", getStaggerTime());
        c.putInt("expose", getExposeTime());
        c.putInt("mexpose", getMaxExposeTime());
        c.putInt("offhandcd", getOffhandCooldown());
        c.putInt("roll", getRollTime());
        c.putInt("mainBind", getHandBind(InteractionHand.MAIN_HAND));
        c.putInt("offBind", getHandBind(InteractionHand.OFF_HAND));
        c.putBoolean("offhand", isOffhandAttack());
        c.putBoolean("combat", isCombatMode());
        c.putBoolean("painful", painful);
        c.putLong("lastUpdate", lastUpdate);
        c.putBoolean("first", first);
        c.putInt("parrying", parrying);
        c.putInt("adrenaline", adrenaline);
        c.putInt("shattercd", getShatterCooldown());
        c.putInt("sweep", getForcedSweep());
        c.putBoolean("rolling", dude.get() instanceof Player p && p.getForcedPose() == Pose.SLEEPING);
        if (!tempOffhand.isEmpty())
            c.put("temp", tempOffhand.save(new CompoundTag()));
        return c;
    }

    @Override
    public void addRangedMight(boolean pass) {
        LivingEntity shooter = dude.get();
        if (shooter == null) return;
        if (pass)
            addMight(Mth.clamp((shooter.tickCount - lastRangeTick) * 0.002f, 0, 0.06f));
        lastRangeTick = shooter.tickCount;
    }

    @Override
    public boolean isStaggeringStrike() {
        if (dude.get() == null) return false;
        return Objects.requireNonNull(dude.get()).tickCount == staggerTickExisted;
    }

    @Override
    public int getRetina() {
        return retina;
    }

    @Override
    public float visionRange() {
        return vision;
    }

    public CompoundTag quickWrite() {
        CompoundTag c = new CompoundTag();
        c.putFloat("posture", getPosture());
        c.putInt("staggert", getStaggerTime());
        c.putInt("mstaggert", getMaxStaggerTime());
        c.putBoolean("painful", painful);
        c.putInt("expose", getExposeTime());
        c.putInt("mexpose", getMaxExposeTime());
        c.putLong("lastUpdate", lastUpdate);
        c.putFloat("maxposture", getMaxPosture());
        c.putFloat("maxfracture", getMaxFracture());
        if (fractureDirty) {
            CompoundTag fractures = new CompoundTag();
            for (Map.Entry<UUID, Integer> e : getFractureList().entrySet()) {
                fractures.putInt(e.getKey().toString(), e.getValue());
            }
            c.put("fractures", fractures);
            fractureDirty = false;
        }
        return c;
    }

    private float getPPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.hasEffect(MobEffects.POISON) ? (elb.getEffect(MobEffects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.hasEffect(FootworkEffects.EXHAUSTION.get()) ? 1 - elb.getEffect(FootworkEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        float armorMod = 2.5f + Math.min(elb.getArmorValue(), 20) * 0.125f;
        float cooldownMod = Math.min(CombatUtils.getCooledAttackStrength(elb, InteractionHand.MAIN_HAND, 0.5f), CombatUtils.getCooledAttackStrength(elb, InteractionHand.MAIN_HAND, 0.5f));
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
        final double ret = (elb.getAttributeValue(FootworkAttributes.POSTURE_REGEN.get()) / 20 * cooldownMod) * exhaustMod * healthMod * poison;
        RegenPostureEvent ev = new RegenPostureEvent(elb, (float) ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }

    private float getSPT() {
        LivingEntity elb = dude.get();
        if (elb == null) return 0;
        int exp = elb.hasEffect(MobEffects.POISON) ? (elb.getEffect(MobEffects.POISON).getAmplifier() + 1) : 0;
        float poison = 1;
        for (int j = 0; j < exp; j++) {
            poison *= GeneralConfig.poison;
        }
        float exhaustMod = Math.max(0, elb.hasEffect(FootworkEffects.EXHAUSTION.get()) ? 1 - elb.getEffect(FootworkEffects.EXHAUSTION.get()).getAmplifier() * 0.2f : 1);
        //float armorMod = 5f + Math.min(elb.getArmorValue(), 20) * 0.25f;
        //float healthMod = 0.25f + elb.getHealth() / elb.getMaxHealth() * 0.75f;
        final double ret = GeneralUtils.getAttributeValueSafe(elb, FootworkAttributes.SPIRIT_REGEN.get()) / 20 * exhaustMod * poison;
        RegenSpiritEvent ev = new RegenSpiritEvent(elb, (float) ret);
        MinecraftForge.EVENT_BUS.post(ev);
        return ev.getQuantity();
    }
}
