package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.event.*;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.handlers.TwoHandingHandler;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.UpdateClientResourcePacket;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class NewCombatCapability implements ICombatCapability {
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    private static final AttributeModifier STOPMOVING = new AttributeModifier(WOUND, "expose penalty", -10, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier NOKNOCKBACK = new AttributeModifier(WOUND, "stagger penalty", 10, AttributeModifier.Operation.ADDITION);
    private final WeakReference<LivingEntity> dude;
    private ItemStack prev;
    private int spirit;
    private float posture, rally;
    private int mBind, oBind;
    private int staggerTime, maxStaggerTime, offhandCD;
    private float mpos;
    private int mspi;
    private boolean offhand, knockdown;
    private long lastUpdate;
    private boolean first = true;
    private float cache;//no need to save this because it'll be used within the span of a tick
    private int guardFrame, parryFrame, dodgeFrame, iFrame;
    private Vec3 motion;
    private double mobPosRegenSpd;
    private int mobPosCD, maxMobPosCD;
    private boolean player;
    private HashMap<String, Double> procs = new HashMap<>();
    private int recordingTime = 0;
    private float recordedDamage = 0;
    private int pinTime;

    public NewCombatCapability(LivingEntity e) {
        dude = new WeakReference<>(e);
        player = e instanceof Player;
    }

    private static float getMPos(LivingEntity elb) {
        float ret = 1;
        if (elb == null) return ret;
        MobSpecs.MobInfo mi = MobSpecs.getMobInfo(elb);
        if (mi != null)
            return (float) mi.getMaxPosture();
        else ret = (float) (Math.ceil(10 / 1.09 * Math.sqrt(elb.getBbWidth() * elb.getBbHeight())));
        //if (!(elb instanceof Player)) ret *= 1.5;//a bit too rough
        return ret;
    }

    @Override
    public void resetPosture() {
        LivingEntity e = dude.get();
        SkillUtils.removeAttribute(e, Attributes.MOVEMENT_SPEED, WOUND);
        SkillUtils.removeAttribute(e, Attributes.FLYING_SPEED, WOUND);
        SkillUtils.removeAttribute(e, Attributes.KNOCKBACK_RESISTANCE, WOUND);
        setPosture(getMaxPosture());
    }

    @Override
    public int getSpirit() {
        return spirit;
    }

    @Override
    public void setSpirit(int v) {
        spirit = Mth.clamp(v, 0, getMaxSpirit());
    }

    @Override
    public boolean consumeSpirit(int amount) {
        ConsumeSpiritEvent cse = new ConsumeSpiritEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getAmount();
        final boolean lacking = spirit < amount;
        if (cse.isCanceled()) {
            return cse.getResult() == Event.Result.ALLOW || (cse.getResult() != Event.Result.DENY && !lacking);
        }

        if (cse.getResult() == Event.Result.DEFAULT && lacking) return false;
        amount = Math.min(amount, spirit);
        spirit -= amount;
        //addRank(amount / 5);
        double cd = ResourceConfig.postureRegen;
        //setSpiritGrace((int) cd);
        return cse.getResult() != Event.Result.DENY;
    }

    @Override
    public int addSpirit(int amount) {
        GainSpiritEvent cse = new GainSpiritEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getQuantity();
        int overflow = Math.max(0, spirit + amount - getMaxSpirit());
        setSpirit(spirit + amount);
        return overflow;
    }

    @Override
    public int getMaxSpirit() {
        return mspi;
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
        posture = Mth.clamp(amount, 0, getMaxPosture());
    }

    @Override
    public float addPosture(float amount) {
        GainPostureEvent cse = new GainPostureEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getQuantity();
        float overflow = Math.max(0, posture + amount - getMaxPosture());
        setPosture(posture + amount);
        return overflow;
    }

    @Override
    public float consumePosture(LivingEntity assailant, float amount, boolean breach) {
        //while posture is not empty incoming damage is reduced by posture??? How to calculate damage <> posture?
        //on taking a breaching hit to posture, flag stun, which interrupts all AI, cancels all knockback, and records damage?
        //on taking a breaching hit while stunned, flag knockdown, greatly knockback, make entity invulnerable until end.
        //posture is not consumed and regens at a fixed rate when flagged in either condition, but it becomes gray until it's cleared.
        //it takes about 6s to get back to full.
        //on receiving jump input as player, perform circle sweep with knockback and return to mobility at current posture percentage.
        //rally gets set after posture is consumed with a flag to rally (all external sources of damage).
        // It stays at max for half a second, then loses max(1, 1/(10*rally duration)) of its value per tick until it rounds to the true value.
        if (!PermissionData.getCap(assailant).canDealPostureDamage()) {
            return 0;
        }
        float ret = 0;
        LivingEntity elb = dude.get();
        if (elb == null) return ret;
        //necessary update before polling, TODO mirror onto other stats?
        serverTick();
        //staggered already, no more posture damage
        if (isStunned()) return amount;
        if (!Float.isFinite(posture)) posture = getMaxPosture();
        //resistance go brr
        if (elb.hasEffect(MobEffects.DAMAGE_RESISTANCE) && GeneralConfig.resistance)
            amount *= (1 - (elb.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 0.2f);
        //event for oodles of compat
        ConsumePostureEvent cpe = new ConsumePostureEvent(elb, assailant, amount);
        MinecraftForge.EVENT_BUS.post(cpe);
        //cancel consumption if... canceled
        if (cpe.isCanceled()) return 0;
        amount = cpe.getAmount();
        if ((posture - amount < 0) && breach) {
            //start stun
            ret = posture - amount;
            //I don't like this here but I don't see a good way around it
            float prev = posture;
            posture = 0;
            //if already stunned, a second breaching hit
            final boolean knockdown = isStunned() || player || alreadyProc("forceKnockDown");
            StunEvent se = new StunEvent(elb, assailant, knockdown ? CombatConfig.knockdownDuration : CombatConfig.staggerDuration, knockdown);
            MinecraftForge.EVENT_BUS.post(se);
            if (se.isCanceled()) {
                posture = prev;
                return 0f;
            }
            elb.stopUsingItem();
            if (se.isKnockdown()) {
                knockdown(assailant, se.getLength());
            } else stun(assailant, se.getLength());
//            if (assailant != null)
//                CombatData.getCap(assailant).addRank(se.isKnockdown() ? 0.2f : 0.1f);
            elb.level().playSound(null, elb.getX(), elb.getY(), elb.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            //why was resetting posture set here?

            elb.stopRiding();
            for (Entity rider : elb.getPassengers())
                rider.stopRiding();
            //returns overflow
            return ret;
        }
        float weakness = 1;
        if (elb.hasEffect(MobEffects.HUNGER))
            for (int uwu = 0; uwu < elb.getEffect(MobEffects.HUNGER).getAmplifier() + 1; uwu++)
                weakness *= GeneralConfig.hunger;
        double cooldown = ResourceConfig.postureCD * weakness;
        posture -= amount;
        setRally((float) (amount * elb.getAttributeValue(FootworkAttributes.RALLY_PERCENTAGE.get())));
        if (!player) mobPosCD = maxMobPosCD;
        if (WarCompat.elenaiDodge && elb instanceof ServerPlayer sp)
            ElenaiCompat.manipulateFeather(sp, 0);
        return ret;
    }

    @Override
    public float getRally() {
        return rally;
    }

    @Override
    public void setRally(float v) {
        //only players get rally
        if (player) rally = v;
    }

    @Override
    public void rally(float amount) {
        RallyPostureEvent rpe = new RallyPostureEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(rpe);
        if (rpe.isCanceled()) return;
        amount = Math.min(rpe.getQuantity(), rally);
        rally -= amount;
        setPosture(posture + amount);
    }

    @Override
    public void tickProc(String key, double ticks) {
        procs.put(key, ticks);
    }

    @Override
    public double getProc(String key) {
        return procs.get(key);
    }

    @Override
    public int getMaxStunTime() {
        return maxStaggerTime;
    }

    @Override
    public int getStunTime() {
        return staggerTime;
    }

    @Override
    public void stun(LivingEntity assailant, int time) {
        //leaving stagger
        final LivingEntity e = dude.get();
        if (time == 0 && staggerTime > 0 && e != null) {
            resetPosture();
            maxStaggerTime = 0;
        }//entering stagger
        else if (e != null && time > 0 && staggerTime == 0) {
            pin(time);
            setHandBind(InteractionHand.MAIN_HAND, time);
            setHandBind(InteractionHand.OFF_HAND, time);
        }
        maxStaggerTime = Math.max(maxStaggerTime, time);
        staggerTime = time;
    }

    @Override
    public boolean isKnockdown() {
        return knockdown && isStunned();
    }

    @Override
    public void knockdown(LivingEntity livingEntity, int time) {
        stun(livingEntity, time);
        knockdown = true;
    }

    @Override
    public Vec3 getMotionConsistently() {
        if (dude.get() == null || motion == null) return Vec3.ZERO;
        return dude.get().position().subtract(motion).scale(0.25);
    }

    @Override
    public void serverTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        final int ticks = (int) (elb.level().getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards

        //initialize posture and fracture
        final boolean uninitializedPosture = elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).getBaseValue() == 0d;
        if (uninitializedPosture) {
            final float mPos = getMPos(elb);
            if (uninitializedPosture && !player) {//ew
                elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).setBaseValue(mPos);
                MobSpecs.MobInfo specs = MobSpecs.getMobInfo(elb);
                if (specs == null) specs = MobSpecs.DEFAULT;
                mobPosRegenSpd = specs.getPostureRegenerationSpeed();
                maxMobPosCD = specs.getPostureRegenerationCooldown();
                double permScale = specs.getMaxPostureScaling();
                if (permScale != 1)
                    elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).addPermanentModifier(new AttributeModifier(CombatUtils.main, "json bonus", permScale, AttributeModifier.Operation.MULTIPLY_TOTAL));
                mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
                setPosture(getMaxPosture());
            }
        }

        //update max values
        mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
        if (posture > mpos)
            setPosture(mpos);
        mspi = (int) elb.getAttributeValue(FootworkAttributes.MAX_SPIRIT.get());
        if (spirit > mspi)
            setSpirit(mspi);

        //store motion for further use
        if (ticks > 5 || (lastUpdate + ticks) % 5 != lastUpdate % 5)
            motion = elb.position();

        //damage recording resolution
        --recordingTime;
        if (recordingTime < 0 && recordedDamage > 0 && elb.getKillCredit() != null)
            stopRecording(elb.getLastDamageSource());

        //tick down everything
        //hand bind
        for (InteractionHand h : InteractionHand.values()) {
            if (getHandBind(h) > 0)
                CombatUtils.setHandCooldown(elb, h, 0, true);
        }
        mBind -= ticks;
        int prevOBind = oBind;
        oBind -= ticks;
        if (!CombatUtils.suppress && (oBind > 0 || prevOBind > 0) && (oBind <= 0 || prevOBind <= 0))
            TwoHandingHandler.updateTwoHanding(elb, elb.getMainHandItem(), elb.getMainHandItem());

        //offhand cooldown
        offhandCD += ticks;

        //dodge/block/parry/iframe resolution
        dodgeFrame -= ticks;
        if (elb.isBlocking() || elb.isShiftKeyDown()) guardFrame = 10;
        else {
            if (guardFrame > 0) setParryTime(CombatConfig.parryTime);
            guardFrame = -10;
        }
        parryFrame -= ticks;
        iFrame -= ticks;

        //stun
        if (staggerTime - ticks > 0)
            staggerTime -= ticks;
        else {
            if (staggerTime != 0)
                resetPosture();
            maxStaggerTime = staggerTime = 0;
        }

        //pin
        if (pinTime - ticks > 0)
            pinTime -= ticks;
        else {
            if (pinTime > 0)
                pin(0);
        }

        //regenerate posture
        if (isStunned() && getPosture() < getMaxPosture()) {
            setPosture(getPosture() + getMaxPosture() / getMaxStunTime());
        } else if (!player) handleMobPostureRegen(ticks);
        else if (elb.moveDist > 0) addPosture(0.05f);
        if (getPosture() > getMaxPosture())
            setPosture(getMaxPosture());
        if (prev == null || !ItemStack.matches(elb.getOffhandItem(), prev)) {
            prev = elb.getOffhandItem();
            setOffhandCooldown(0);
        }

        //decrement or clear turn procs
        procs.replaceAll((k, v) -> v - 1);
        procs.entrySet().removeIf(entry -> entry.getValue() < 0);

        lastUpdate = elb.level().getGameTime();
        first = false;
        sync();
    }

    private void sync() {
        LivingEntity elb = dude.get();
        if (elb == null || elb.level().isClientSide) return;
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientResourcePacket(elb.getId(), write()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayer sp)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientResourcePacket(elb.getId(), write()));

    }

    @Override
    public void clientTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        final int ticks = (int) (elb.level().getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        //update max values
        mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
        if (posture > mpos)
            setPosture(mpos);
        //tick down everything
        //hand bind
        for (InteractionHand h : InteractionHand.values()) {
            if (getHandBind(h) > 0)
                CombatUtils.setHandCooldown(elb, h, 0, true);
        }
        mBind -= ticks;
        int prevOBind = oBind;
        oBind -= ticks;
        LivingEntity e = dude.get();
        if (!CombatUtils.suppress && (oBind > 0 || prevOBind > 0) && (oBind <= 0 || prevOBind <= 0) && e != null)
            TwoHandingHandler.updateTwoHanding(e, e.getMainHandItem(), e.getMainHandItem());
        offhandCD += ticks;
        dodgeFrame -= ticks;

        if (staggerTime - ticks > 0)
            staggerTime -= ticks;
        else {
            if (staggerTime != 0)
                resetPosture();
            maxStaggerTime = staggerTime = 0;
        }

        //regenerate posture
        if (isStunned() && getPosture() < getMaxPosture()) {
            setPosture(getPosture() + getMaxPosture() / getMaxStunTime());
        } else if (!player) handleMobPostureRegen(ticks);
        else if (elb.moveDist > 0) addPosture(0.05f);
        if (getPosture() > getMaxPosture())
            setPosture(getMaxPosture());
        if (prev == null || !ItemStack.matches(elb.getOffhandItem(), prev)) {
            prev = elb.getOffhandItem();
            setOffhandCooldown(0);
        }
        lastUpdate = elb.level().getGameTime();
        first = false;
    }

    @Override
    public int getOffhandCooldown() {
        return offhandCD;
    }

    @Override
    public void setOffhandCooldown(int i) {
        offhandCD = i;
    }

    @Override
    public boolean isDodging() {
        return dodgeFrame > 0 && !alreadyProc("cannot_dodge");
    }

    @Override
    public boolean canDodge() {
        return dodgeFrame < -CombatConfig.rollCooldown && !alreadyProc("cannot_dodge");
    }

    @Override
    public int getDodgeTime() {
        return dodgeFrame;
    }

    @Override
    public void setDodgeTime(int i) {
        dodgeFrame = i;
    }

    @Override
    public boolean isParrying() {
        return parryFrame > 0 && !alreadyProc("cannot_parry");
    }

    @Override
    public boolean canParry() {
        return parryFrame < -CombatConfig.parryCD && !alreadyProc("cannot_parry");
    }

    @Override
    public int getParryTime() {
        return parryFrame;
    }

    @Override
    public void setParryTime(int i) {
        parryFrame = i;
    }

    @Override
    public boolean isBlocking() {
        if (dude.get() == null) return false;
        return guardFrame > 0 && !alreadyProc("cannot_block");
    }

    @Override
    public boolean canBlock() {
        return !alreadyProc("cannot_block");
    }

    @Override
    public int getGuardTime() {
        return guardFrame;
    }

    @Override
    public void setGuardTime(int i) {
        guardFrame = i;
    }

    @Override
    public boolean isIframe() {
        return iFrame > 0 && !alreadyProc("cannot_iframe");
    }

    @Override
    public int getIframe() {
        return iFrame;
    }

    @Override
    public void setIframe(int i) {
        iFrame = i;
    }

    @Override
    public int getDamageRecordTime() {
        return recordingTime;
    }

    @Override
    public float getRecordedDamage() {
        return recordedDamage;
    }

    @Override
    public void startRecordingDamage(int time) {
        recordingTime = time;
    }

    @Override
    public void recordDamage(float v) {
        recordedDamage += v;
    }

    @Override
    public void stopRecording(DamageSource damageSource) {
        recordingTime = 0;
        if (dude.get() != null)
            dude.get().hurt(damageSource, recordedDamage);
        recordedDamage = 0;
    }

    @Override
    public int getPinTime() {
        return pinTime;
    }

    @Override
    public void pin(int time) {
        //leaving pin
        final LivingEntity e = dude.get();
        if (time <= 0 && pinTime > 0 && e != null) {
            SkillUtils.removeAttribute(e, Attributes.MOVEMENT_SPEED, WOUND);
            SkillUtils.removeAttribute(e, Attributes.FLYING_SPEED, WOUND);
            SkillUtils.removeAttribute(e, Attributes.KNOCKBACK_RESISTANCE, WOUND);
        }//entering pin
        else if (e != null && time > 0 && pinTime <= 0) {
            SkillUtils.addAttribute(e, Attributes.MOVEMENT_SPEED, STOPMOVING);
            SkillUtils.addAttribute(e, Attributes.FLYING_SPEED, STOPMOVING);
            SkillUtils.addAttribute(e, Attributes.KNOCKBACK_RESISTANCE, NOKNOCKBACK);
        }
        pinTime = time;
    }

    @Override
    public int getHandBind(InteractionHand h) {
        if (!CombatUtils.suppress) {
            if (isStunned()) return 1;
            LivingEntity bro = dude.get();
            if (bro != null) {
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
    public CompoundTag write() {
        CompoundTag c = new CompoundTag();
        c.putInt("spirit", spirit);
        c.putFloat("posture", posture);
        c.putFloat("rally", rally);
        c.putInt("mBind", mBind);
        c.putInt("oBind", oBind);
        c.putInt("staggerTime", staggerTime);
        c.putInt("maxStaggerTime", maxStaggerTime);
        c.putInt("offhandCD", offhandCD);
        c.putBoolean("knockdown", knockdown);
        c.putBoolean("offhand", offhand);
        c.putLong("lastUpdate", lastUpdate);
        c.putBoolean("first", first);
        c.putInt("guard", guardFrame);
        c.putInt("parry", parryFrame);
        c.putInt("dodge", dodgeFrame);
        c.putInt("invul", iFrame);
        c.putInt("mobPosCD", mobPosCD);
        c.putInt("maxMobPosCD", maxMobPosCD);
        c.putDouble("mobPosRegenSpd", mobPosRegenSpd);
        c.putBoolean("player", player);
        CompoundTag proc = new CompoundTag();
        for (Map.Entry<String, Double> e : procs.entrySet()) {
            proc.putDouble(e.getKey(), e.getValue());
        }
        c.put("procs", proc);
        c.putInt("recordingTime", recordingTime);
        c.putFloat("recorded", recordedDamage);
        c.putInt("pin", pinTime);
        return c;
    }

    @Override
    public void read(CompoundTag t) {
        setSpirit(t.getInt("spirit"));
        setPosture(t.getFloat("posture"));
        setRally(t.getFloat("rally"));
        setHandBind(InteractionHand.MAIN_HAND, t.getInt("mBind"));
        setHandBind(InteractionHand.OFF_HAND, t.getInt("oBind"));
        maxStaggerTime = t.getInt("maxStaggerTime");
        if (t.getBoolean("knockdown"))
            knockdown(t.getInt("staggerTime"));
        else stun(t.getInt("staggerTime"));
        setOffhandCooldown(t.getInt("offhandCD"));
        offhand = t.getBoolean("offhand");
        lastUpdate = t.getLong("lastUpdate");
        first = t.getBoolean("first");
        setGuardTime(t.getInt("guard"));
        setParryTime(t.getInt("parry"));
        setDodgeTime(t.getInt("dodge"));
        setIframe(t.getInt("invul"));
        mobPosCD = t.getInt("mobPosCD");
        maxMobPosCD = t.getInt("maxMobPosCD");
        mobPosRegenSpd = t.getDouble("mobPosRegenSpd");
        player = t.getBoolean("player");
        if (t.contains("procs")) {
            procs.clear();
            CompoundTag c = (CompoundTag) t.get("procs");
            if (c != null) for (String id : c.getAllKeys()) {
                procs.put(id, c.getDouble(id));
            }
        }
        recordingTime = t.getInt("recordingTime");
        recordedDamage = t.getFloat("recorded");
        pin(t.getInt("pin"));
    }

    @Override
    public boolean isOffhandAttack() {
        return offhand;
    }

    @Override
    public void setOffhandAttack(boolean offhandAttack) {
        offhand = offhandAttack;
    }

    private void handleMobPostureRegen(int ticks) {
        mobPosCD -= ticks;
        float mult = 1;
        LivingEntity elb = dude.get();
        if (elb != null) {
            mult *= elb.getHealth() / elb.getMaxHealth();
            mult *= Math.min(CombatUtils.getCooledAttackStrength(elb, InteractionHand.MAIN_HAND, 0.5f), CombatUtils.getCooledAttackStrength(elb, InteractionHand.MAIN_HAND, 0.5f));
            int exp = elb.hasEffect(MobEffects.POISON) ? (elb.getEffect(MobEffects.POISON).getAmplifier() + 1) : 0;
            float poison = 1;
            for (int j = 0; j < exp; j++) {
                poison *= GeneralConfig.poison;
            }
            mult *= poison;
        }
        if (mobPosCD < 0) {
            mobPosCD = 0;
            int overflow = -mobPosCD;
            setPosture((float) (getPosture() + overflow * mobPosRegenSpd * mult));
        }
    }
}
