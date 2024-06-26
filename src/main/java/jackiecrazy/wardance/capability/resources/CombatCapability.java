package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.*;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.event.FractureEvent;
import jackiecrazy.wardance.handlers.TwoHandingHandler;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.UpdateClientResourcePacket;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.ComboRanks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
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
    public static final int EVADE_CHARGE = 2000;//100 seconds
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
    private float evade;
    private float qcd, scd, pcd, ccd;
    private int mBind, oBind;
    private int staggert, mstaggert, offhandcd, roll, expose, mexpose, sweepAngle = -1;
    private float mpos, mspi, mmight, mfrac;
    private boolean offhand, combat, incomingPain, knockdown;
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
    private int internal_parry_timer = 0;

    public CombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
    }

    private static float getMPos(LivingEntity elb) {
        float ret = 1;
        if (elb == null) return ret;
        if (GeneralUtils.getResourceLocationFromEntity(elb) != null && MobSpecs.mobMap.containsKey(elb.getType()))
            return (float) MobSpecs.mobMap.get(elb.getType()).getMaxPosture();
        else ret = (float) (Math.ceil(10 / 1.09 * Math.sqrt(elb.getBbWidth() * elb.getBbHeight())));
        //if (!(elb instanceof Player)) ret *= 1.5;//a bit too rough
        return ret;
    }

    @Override
    public void updateDefenselessStatus() {
        if (!incomingPain) return;
        incomingPain = false;
        knockdown = false;
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
        }
        GainMightEvent gme = new GainMightEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(gme);
        if (gme.isCanceled()) return -1;
        amount = gme.getQuantity();
        float temp = might + amount;
        setMight(temp);
        setMightGrace((int) grace);
        addRank(amount / 5);
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
        addRank(amount / 3);
        return cse.getResult() != Event.Result.DENY;
    }

    @Override
    public int getMightGrace() {
        return (int) qcd;
    }

    @Override
    public void setMightGrace(int amount) {
        qcd = amount;
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
        addRank(amount / 5);
        double cd = ResourceConfig.spiritCD;
        setSpiritGrace((int) cd);
        return cse.getResult() != Event.Result.DENY;
    }

    @Override
    public int getSpiritGrace() {
        return (int) scd;
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
        if (!PermissionData.getCap(assailant).canDealPostureDamage()) {
            return 0;
        }
        float ret = 0;
        LivingEntity elb = dude.get();
        if (elb == null) return ret;
        //necessary update before polling, TODO mirror onto other stats?
        serverTick();
        //staggered already, no more posture damage
        if (getStunTime() > 0 || getExposeTime() > 0 || posture <= 0) return amount;
        if (!Float.isFinite(posture)) posture = getMaxPosture();
        //resistance go brr
        if (elb.hasEffect(MobEffects.DAMAGE_RESISTANCE) && GeneralConfig.resistance)
            amount *= (1 - (elb.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 0.2f);
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
        if (above > 0 && posture - amount < above) {
            //posture floor, set and bypass stagger test
            ret = amount - above;
            amount = posture - above;
        } else if (posture - amount < 0) {
            //stun related hijinks
            if (consumeEvade()) {
                return 0;
            }
            ret = posture - amount;
            //I don't like this here but I don't see a good way around it
            float prev = posture;
            posture = 0;
            if (addFracture(assailant, 1)) {
                final boolean knockdown = elb.hasEffect(FootworkEffects.UNSTEADY.get());
                StunEvent se = new StunEvent(elb, assailant, knockdown ? CombatConfig.knockdownDuration : CombatConfig.staggerDuration, knockdown);
                MinecraftForge.EVENT_BUS.post(se);
                if (se.isCanceled()) {
                    posture = prev;
                    return 0f;
                }
                elb.stopUsingItem();
                if (se.isKnockdown()) {
                    knockdown(se.getLength());
                    elb.removeEffect(FootworkEffects.UNSTEADY.get());
                } else stun(se.getLength());
                if (assailant != null)
                    CombatData.getCap(assailant).addRank(se.isKnockdown() ? 0.2f : 0.1f);
                elb.level().playSound(null, elb.getX(), elb.getY(), elb.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
            //why was resetting posture set here?

//            elb.stopRiding();
//            for (Entity rider : elb.getPassengers())
//                rider.stopRiding();
            staggerTickExisted = elb.tickCount;
            //returns overflow
            return ret;
        }
        float weakness = 1;
        if (elb.hasEffect(MobEffects.HUNGER))
            for (int uwu = 0; uwu < elb.getEffect(MobEffects.HUNGER).getAmplifier() + 1; uwu++)
                weakness *= GeneralConfig.hunger;
        double cooldown = ResourceConfig.postureCD * weakness;
        posture -= amount;
        setPostureGrace((int) cooldown);
        if (WarCompat.elenaiDodge && elb instanceof ServerPlayer sp)
            ElenaiCompat.manipulateFeather(sp, 0);
        sync();
        return ret;
    }

    @Override
    public int getPostureGrace() {
        return (int) pcd;
    }

    @Override
    public void setPostureGrace(int amount) {
        pcd = amount;
    }

    @Override
    public int getMaxStunTime() {
        return mstaggert;
    }

    @Override
    public int getStunTime() {
        return staggert;
    }

    @Override
    public void stun(int time) {
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
            incomingPain = true;
        }
        mstaggert = Math.max(mstaggert, time);
        staggert = time;
    }

    @Override
    public int getMaxKnockdownTime() {
        return knockdown ? mstaggert : 0;
    }

    @Override
    public int getKnockdownTime() {
        return knockdown ? staggert : 0;
    }

    @Override
    public void knockdown(int time) {
        //leaving stagger
        knockdown = true;
        final LivingEntity e = dude.get();
        if (time == 0 && staggert > 0 && e != null) {
            updateDefenselessStatus();
            mstaggert = 0;
        }//entering stagger
        else if (e != null && time > 0 && staggert == 0) {
            e.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            e.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(EXPOSEA);

            final AttributeInstance fly = e.getAttribute(Attributes.FLYING_SPEED);
            if (fly != null) {
                fly.removeModifier(WOUND);
                fly.addPermanentModifier(EXPOSEA);
            }
            incomingPain = true;
        }
        mstaggert = Math.max(mstaggert, time);
        staggert = time;
    }

    @Override
    public boolean isStunned() {
        return getStunTime() > 0 && incomingPain;
    }

    @Override
    public boolean isKnockedDown() {
        return isStunned() && knockdown;
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

    /**
     *
     * @return false if fractures overflow
     */
    @Override
    public boolean addFracture(@Nullable LivingEntity livingEntity, int i) {
        FractureEvent fe = new FractureEvent(dude.get(), i, livingEntity);
        MinecraftForge.EVENT_BUS.post(fe);
        if (fe.isCanceled()) return true;
        i = fe.getAmount();
        if (getFractureCount() + i >= getMaxFracture()) {
            ExposeEvent se = new ExposeEvent(dude.get(), livingEntity, CombatConfig.exposeDuration); //magic number
            MinecraftForge.EVENT_BUS.post(se);
            if (se.isCanceled()) return false;
            dude.get().stopUsingItem();
            expose(se.getLength());
            sync();
            if (livingEntity != null)
                CombatData.getCap(livingEntity).addRank(0.4f);
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
            if (e != null && e.level() instanceof ServerLevel server)
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
            stun(0);
            e.level().playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            e.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WOUND);
            e.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(EXPOSEA);

            final AttributeInstance fly = e.getAttribute(Attributes.FLYING_SPEED);
            if (fly != null) {
                fly.removeModifier(WOUND);
                fly.addPermanentModifier(EXPOSEA);
            }
            e.getAttribute(Attributes.ARMOR).removeModifier(WOUND);
            e.getAttribute(Attributes.ARMOR).addPermanentModifier(EXPOSEA);
            if (e instanceof Player)
                e.addEffect(new MobEffectInstance(FootworkEffects.EXPOSED.get(), time, 0));
            incomingPain = true;
        }
        mexpose = Math.max(mexpose, time);
        expose = time;
    }

    @Override
    public boolean isExposed() {
        return getExposeTime() > 0 && incomingPain;
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

    public int getComboRank() {
        float workingCombo = this.getRank();
        if (workingCombo >= 9.0F) {
            return 6;
        } else if (workingCombo >= 6.0F) {
            return 5;
        } else {
            return workingCombo >= 4.0F ? 4 : (int) workingCombo;
        }
    }

    @Override
    public void setAdrenalineCooldown(int amount) {
        adrenaline = amount;
    }

    @Override
    public boolean halvedAdrenaline() {
        return CombatConfig.adrenaline >= 0 && adrenaline != 0;
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
        if (!PermissionData.getCap(dude.get()).canEnterCombatMode()) {
            combat = false;
            return;
        }
        combat = on;
    }

    @Override
    public int getHandBind(InteractionHand h) {
        if (!CombatUtils.suppress) {
            if (isVulnerable()) return 1;
            LivingEntity bro = dude.get();
            if (dude.get() != null) {
                if (h == InteractionHand.OFF_HAND && (WeaponStats.isTwoHanded(bro.getOffhandItem(), bro, InteractionHand.OFF_HAND) || (WeaponStats.isTwoHanded(bro.getMainHandItem(), bro, InteractionHand.MAIN_HAND) && WeaponStats.lookupStats(bro.getOffhandItem()) != null)))
                    return 1;
            }
        }
        if (h == InteractionHand.OFF_HAND) {
            return oBind;
        }
        return mBind;
    }

    @Override
    public void setHandBind(InteractionHand h, int amount) {
        final LivingEntity e = dude.get();
        switch (h) {
            case MAIN_HAND -> {
                mBind = amount;
            }
            case OFF_HAND -> {
                if (!CombatUtils.suppress && (oBind == 0 || amount == 0) && oBind != amount && e != null)
                    TwoHandingHandler.updateTwoHanding(e, e.getMainHandItem(), e.getMainHandItem());
                oBind = amount;
            }
        }
    }

    @Override
    public boolean consumeEvade() {
        if (evade >= EVADE_CHARGE) {
            evade = 0;
            return true;
        }
        return false;
    }

    @Override
    public int getEvade() {
        return (int) evade;
    }

    @Override
    public void setEvade(int value) {
        evade = value;
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
        final int ticks = (int) (elb.level().getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        //initialize posture and fracture

        final boolean uninitializedPosture = elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).getBaseValue() == 0d;
        final boolean uninitializedFracture = elb.getAttribute(FootworkAttributes.MAX_FRACTURE.get()).getBaseValue() == 0d;
        if (uninitializedPosture || uninitializedFracture) {
            final float mPos = getMPos(elb);
            if (uninitializedPosture) {//ew
                elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).setBaseValue(mPos);
                double permScale = MobSpecs.mobMap.getOrDefault(elb.getType(), MobSpecs.DEFAULT).getMaxPostureScaling();
                if (permScale != 1)
                    elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).addPermanentModifier(new AttributeModifier(CombatUtils.main, "json bonus", permScale, AttributeModifier.Operation.MULTIPLY_TOTAL));
                elb.getAttribute(FootworkAttributes.POSTURE_REGEN.get()).setBaseValue(mPos);
                mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
                setPosture(getMaxPosture());
            }
            if (uninitializedFracture) {//ew
                double fracs = Math.log(elb.getMaxHealth()) - 1;
                if (elb instanceof Player)
                    elb.getAttribute(FootworkAttributes.MAX_FRACTURE.get()).setBaseValue(3);
                else
                    elb.getAttribute(FootworkAttributes.MAX_FRACTURE.get()).setBaseValue(Math.floor(fracs));
            }
        }
        //update max values
        vision = (float) elb.getAttributeValue(Attributes.FOLLOW_RANGE);
        mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
        if (posture > mpos)
            setPosture(mpos);
        mspi = (float) elb.getAttributeValue(FootworkAttributes.MAX_SPIRIT.get());
        if (spirit > mspi)
            setSpirit(mspi);
        mmight = (float) elb.getAttributeValue(FootworkAttributes.MAX_MIGHT.get());
        if (might > mmight)
            setMight(mmight);
        mfrac = (int) elb.getAttributeValue(FootworkAttributes.MAX_FRACTURE.get());
        if (elb.hasEffect(FootworkEffects.SLEEP.get()) || elb.hasEffect(FootworkEffects.PARALYSIS.get()) || elb.hasEffect(FootworkEffects.PETRIFY.get()))
            vision = -1;
        //store motion for further use
        if (ticks > 5 || (lastUpdate + ticks) % 5 != lastUpdate % 5)
            motion = elb.position();
        //every once in a while clear invalid fractures
        internal_fracture_timer -= ticks;
        if (internal_fracture_timer < 0) {
            clearFracture(null, true);
            internal_fracture_timer = 400;
        }
        //detect failed parrying
        if (internal_parry_timer > 0) {
            internal_parry_timer -= ticks;
            if (internal_parry_timer <= 0) {
                //failed parry
                if (getHandBind(InteractionHand.OFF_HAND) <= 0) oBind = CombatConfig.parryCD;
                else mBind = CombatConfig.parryCD;
            }
        }
        evade += ticks * elb.getAttributeValue(FootworkAttributes.EVASION.get());
        //tick down everything
        if (adrenaline > 0)
            adrenaline -= Math.min(adrenaline, ticks);
        float qiExtra = decrementMightGrace((float) (ticks / elb.getAttributeValue(FootworkAttributes.MIGHT_GRACE.get())));
        float spExtra = decrementSpiritGrace((float) (ticks * elb.getAttributeValue(FootworkAttributes.SPIRIT_COOLDOWN.get())));
        float poExtra = decrementPostureGrace((float) (ticks * elb.getAttributeValue(FootworkAttributes.POSTURE_COOLDOWN.get())));
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
        if ((getPostureGrace() == 0 || getStunTime() > 0 || getExposeTime() > 0) && getPosture() < getMaxPosture()) {
            if (getStunTime() > 0 || getExposeTime() > 0)
                setPosture(getMaxPosture() * poExtra / Math.max(getMaxExposeTime(), getMaxStunTime()));
            else setPosture(getPosture() + getPPT() * (poExtra));

        }
        if (getPosture() > getMaxPosture())
            setPosture(getMaxPosture());
        if (getSpiritGrace() == 0 && getStunTime() == 0 && getSpirit() < getMaxSpirit()) {
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
            if (getComboRank() == ComboRanks.SS)
                decay = 0.025f;
            if (getComboRank() == ComboRanks.SSS)
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
        lastUpdate = elb.level().getGameTime();
        first = false;
        sync();
    }

    @Override
    public void sync() {
        LivingEntity elb = dude.get();
        if (elb == null || elb.level().isClientSide) return;
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientResourcePacket(elb.getId(), quickWrite()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayer sp)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientResourcePacket(elb.getId(), write()));

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
    public int getParryingTick() {
        return parrying;
    }

    @Override
    public void setParryingTick(int parrying) {
        this.parrying = parrying;
        if (parrying != 0)
            internal_parry_timer = CombatConfig.parryTime;
        else internal_parry_timer = 0;
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
        c.putInt("mstaggert", getMaxStunTime());
        c.putInt("staggert", getStunTime());
        c.putInt("expose", getExposeTime());
        c.putInt("mexpose", getMaxExposeTime());
        c.putInt("offhandcd", getOffhandCooldown());
        c.putInt("roll", getRollTime());
        c.putInt("mainBind", mBind);
        c.putInt("offBind", oBind);
        c.putBoolean("offhand", isOffhandAttack());
        c.putBoolean("combat", isCombatMode());
        c.putBoolean("painful", incomingPain);
        c.putBoolean("knocked", knockdown);
        c.putLong("lastUpdate", lastUpdate);
        c.putBoolean("first", first);
        c.putInt("parrying", parrying);
        c.putInt("adrenaline", adrenaline);
        c.putInt("shattercd", getEvade());
        c.putInt("sweep", getForcedSweep());
        c.putBoolean("rolling", dude.get() instanceof Player p && p.getForcedPose() == Pose.SLEEPING);
        if (!tempOffhand.isEmpty())
            c.put("temp", tempOffhand.save(new CompoundTag()));
        return c;
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
        incomingPain = c.getBoolean("painful");
        knockdown = c.getBoolean("knocked");
        setPosture(c.getFloat("posture"));
        setEvade(c.getInt("shattercd"));
        if (c.contains("fractures")) {
            fractures.clear();
            CompoundTag t = (CompoundTag) c.get("fractures");
            if (t != null) for (String id : t.getAllKeys()) {
                fractures.put(UUID.fromString(id), t.getInt(id));
            }
        }
        if (!c.contains("qi")) return;
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
            if (c.getBoolean("rolling"))
                p.setForcedPose(Pose.SLEEPING);
            else if (temp == 0)
                p.setForcedPose(null);
        }
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

    public int decrementMightGrace(int amount) {
        return (int) decrementPostureGrace((float) amount);
    }

    public void decrementStaggerTime(int amount) {
        if (staggert - amount > 0)
            staggert -= amount;
        else {
            int temp = staggert;
            if (staggert != 0)
                updateDefenselessStatus();
            mstaggert = staggert = 0;
        }
    }

    public void decrementExposeTime(int amount) {
        if (expose - amount > 0)
            expose -= amount;
        else {
            int temp = expose;
            if (expose != 0)
                updateDefenselessStatus();
            expose = mexpose = 0;
        }
    }

    public void addOffhandCooldown(int amount) {
        offhandcd += amount;
    }

    public void decrementHandBind(InteractionHand h, int amount) {
        switch (h) {
            case MAIN_HAND -> mBind -= Math.min(amount, mBind);
            case OFF_HAND -> {
                int prev = oBind;
                oBind -= Math.min(amount, oBind);
                LivingEntity e = dude.get();
                if (!CombatUtils.suppress && (oBind == 0 || prev == 0) && prev != amount && e != null)
                    TwoHandingHandler.updateTwoHanding(e, e.getMainHandItem(), e.getMainHandItem());
            }
        }
    }

    private float decrementMightGrace(float amount) {
        qcd -= amount;
        if (qcd < 0) {
            float temp = qcd;
            qcd = 0;
            return -temp;
        }
        return 0;
    }

    private float decrementSpiritGrace(float amount) {
        scd -= amount;
        if (scd < 0) {
            float temp = scd;
            scd = 0;
            return -temp;
        }
        return 0;
    }

    private float decrementPostureGrace(float amount) {
        pcd -= amount;
        if (pcd < 0) {
            float temp = pcd;
            pcd = 0;
            return -temp;
        }
        return 0;
    }

    public CompoundTag quickWrite() {
        CompoundTag c = new CompoundTag();
        c.putFloat("posture", getPosture());
        c.putInt("staggert", getStunTime());
        c.putInt("mstaggert", getMaxStunTime());
        c.putBoolean("painful", incomingPain);
        c.putInt("expose", getExposeTime());
        c.putInt("mexpose", getMaxExposeTime());
        c.putLong("lastUpdate", lastUpdate);
        c.putFloat("maxposture", getMaxPosture());
        c.putFloat("maxfracture", getMaxFracture());
        c.putBoolean("knocked", knockdown);
        c.putInt("shattercd", getEvade());
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
        float healthMod = elb.getHealth() / elb.getMaxHealth();
        //no speed modifier because it encourages not moving
        float speedMod = elb.isSprinting() ? 0.3f : elb.zza == 0 && elb.xxa == 0 && elb.yya == 0 ? 1f : 0.5f;
        final double ret = (elb.getAttributeValue(FootworkAttributes.POSTURE_REGEN.get()) / 20 * cooldownMod) * speedMod * exhaustMod * healthMod * poison;
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
