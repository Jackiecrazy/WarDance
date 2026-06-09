package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.Footwork;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.event.*;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.permission.PermissionData;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.event.DamageRetconEvent;
import jackiecrazy.wardance.handlers.TwoHandingHandler;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.UpdateClientResourcePacket;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.MobilityUtils;
import jackiecrazy.wardance.utils.ReworkConstants;
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
import java.util.*;

public class NewCombatCapability implements ICombatCapability {
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    public static final int RALLY_CD = 20;
    private static final AttributeModifier STOPMOVING = new AttributeModifier(WOUND, "expose penalty", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier NOKNOCKBACK = new AttributeModifier(WOUND, "stagger penalty", 10, AttributeModifier.Operation.ADDITION);
    private final WeakReference<LivingEntity> dude;
    private ItemStack prev;
    private float spirit;
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
    //private List<HitInfo> onGuard=new ArrayList<>(), onParry=new ArrayList<>(), onDodge=new ArrayList<>(), onIframe=new ArrayList<>();
    private Vec3 motion;
    private double mobPosRegenSpd = 0.3;
    private int mobPosCD = 60, maxMobPosCD = 60, spiritCD, maxSpiritCD = 20;
    private boolean player;
    private HashMap<String, Double> procs = new HashMap<>();
    private float recordedDamage = 0;
    private int pinTime;
    private boolean dirty = false;
    private int healthyCooldown = 100;

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
        else
            ret = (float) (Math.ceil(ReworkConstants.POSTURE_QI * 10 / 1.09 * Math.sqrt(elb.getBbWidth() * elb.getBbHeight())));
        ret = Math.min(ret, 400);//sanity
        if (elb instanceof Player) ret *= 1.5f;
        return ret;
    }

    @Override
    public void resetPosture() {
        LivingEntity e = dude.get();
        SkillUtils.removeAttribute(e, Attributes.MOVEMENT_SPEED, WOUND);
        SkillUtils.removeAttribute(e, Attributes.FLYING_SPEED, WOUND);
        SkillUtils.removeAttribute(e, Attributes.KNOCKBACK_RESISTANCE, WOUND);
        setPosture(getMaxPosture());
        knockdown = false;
    }

    @Override
    public float getSpirit() {
        return spirit;
    }

    @Override
    public void setSpirit(float v) {
        spirit = Mth.clamp(v, 0, getMaxSpirit());
        dirty = true;
    }

    /**
     * cancel to not consume spirit
     * allow to return true, deny to return false
     * if not canceled and denied, the spirit will be consumed but will return false.
     * if not canceled and allowed, the spirit will be consumed to the limit, but will always return true.
     */
    @Override
    public float doConsumeSpirit(float amount) {
        ConsumeSpiritEvent cse = new ConsumeSpiritEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getAmount();
        dirty = true;
        final boolean lacking = spirit < amount;
        if (cse.isCanceled()) {
            if (cse.getResult() != Event.Result.DENY)
                return 0;
            else {
                return 5;
            }
        }
        spiritCD = maxSpiritCD;
        if (cse.getResult() == Event.Result.DEFAULT && lacking) return amount - spirit;
        final float finalized = spirit - amount;
        if (cse.getResult() == Event.Result.DENY) {
            setSpirit(finalized);
            return 5;
        } else if (cse.getResult() == Event.Result.ALLOW) {
            setSpirit(finalized);
            return 0;
        }
        if (!lacking) {
            setSpirit(finalized);
            return 0;
        }
        return amount - spirit;
    }

    @Override
    public boolean consumeSpirit(float amount) {
        return doConsumeSpirit(amount) <= 0;
    }

    @Override
    public float addSpirit(float amount) {
        GainSpiritEvent cse = new GainSpiritEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(cse);
        amount = cse.getQuantity();
        float ret = 0;
        spirit += amount;
        if (spirit < 0) spirit = 0;
        if (spirit > getMaxSpirit()) {
            ret = spirit - getMaxSpirit();
            spirit = getMaxSpirit();
        }
        //addPosture(amount * ReworkConstants.SPIRIT_QI);
        dirty = true;
        return ret;
    }

    @Override
    public float getMaxSpirit() {
        return mspi;
    }

    @Override
    public float getMaxPosture() {
        if (dude.get() != null) {
            initializePostureIfNew(dude.get());
        }
        return mpos;
    }

    @Override
    public float getPosture() {
        return posture;
    }

    @Override
    public void setPosture(float amount) {
        float prev = posture;
        posture = Mth.clamp(amount, 0, mpos);
        dirty |= prev == posture;
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
    public float consumePosture(LivingEntity assailant, float amount, float rallyPerc, BreachLevel breachLevel) {
        //WarDance.LOGGER.debug("consume posture check 1");
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
        dirty = true;
        //knocked down already, no more posture damage
        if (isStunned()) return amount;
        if (!Float.isFinite(posture)) posture = getMaxPosture();

        //resistance go brr
        if (elb.hasEffect(MobEffects.DAMAGE_RESISTANCE) && GeneralConfig.resistance)
            amount *= (1 - (elb.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 0.2f);
        if (elb.hasEffect(FootworkEffects.ENFEEBLE.get()))
            amount *= (1 + (elb.getEffect(FootworkEffects.ENFEEBLE.get()).getAmplifier() + 1) * 0.2f);
        if (elb.hasEffect(FootworkEffects.COUNTERSTRIKE.get())) {
            int cycles = elb.getEffect(FootworkEffects.COUNTERSTRIKE.get()).getAmplifier() + 1;
            while (cycles > 0) {
                cycles--;
                amount *= (1.15f);
            }
        }
        if (elb.hasEffect(FootworkEffects.UNSTEADY.get())) {
            int cycles = elb.getEffect(FootworkEffects.UNSTEADY.get()).getAmplifier() + 1;
            while (cycles > 0) {
                cycles--;
                amount *= (1.25f);
            }
            elb.removeEffect(FootworkEffects.UNSTEADY.get());
        }


        //event for oodles of compat
        ConsumePostureEvent cpe = new ConsumePostureEvent(elb, assailant, amount);
        MinecraftForge.EVENT_BUS.post(cpe);

        //cancel consumption if... canceled
        if (cpe.isCanceled()) return 0;
        amount = cpe.getAmount();

        //players heal internal damage
        if (assailant instanceof Player p && StylishData.getCap(p).isDeathDoor()) {
            CombatData.getCap(p).retconDamage((float) (amount / ReworkConstants.POSTURE_QI));
        }
        mobPosCD = maxMobPosCD;

        //stun check
        if ((isStunned() || posture - amount <= 0) && (breachLevel == BreachLevel.STUN || breachLevel == BreachLevel.KNOCKDOWN)) {
            //start stun
            ret = posture - amount;
            //I don't like this here but I don't see a good way around it
            float prev = posture;
            //if already stunned, a second breaching hit knocks down
            final boolean knockdown = (posture <= 0 && player);//breachLevel == BreachLevel.KNOCKDOWN || isStunned() || alreadyProc("forceKnockDown") ||
            posture = 0;
            StunEvent se = new StunEvent(elb, assailant, knockdown ? (elb instanceof Player ? CombatConfig.knockdownDurationPlayer : CombatConfig.knockdownDuration) : CombatConfig.staggerDuration, knockdown);
            MinecraftForge.EVENT_BUS.post(se);
            if (se.isCanceled()) {
                posture = prev;
                return 0f;
            }
            elb.stopUsingItem();
            if (se.isKnockdown()) {
                //ugly fix. Posture is consumed before damage so the final hit that knocks down a mob will not deal damage.
                //this delays the processing until damage
                //solution: when posture is 0 mark guard as broken and disable block after that hit is over.
                //basically allow player stunning, but player stunning is just guard break and doesn't recover any posture
                tickProc("knockdown", se.getLength());
                tickProc("cannot_block", se.getLength());
            } else {
                if (player) {
                    //cancels blocking and returns successful on that specific hit
                    MobilityUtils.knockBack(elb, assailant, 0.7f, true, true);
                    return 0;
                } else {
                    //stun sets the posture to max so you can deplete it again
                    posture = getMaxPosture();
                    stun(assailant, se.getLength());
                    elb.removeEffect(FootworkEffects.COUNTERSTRIKE.get());
                    if (assailant != null) {
//                        CombatData.getCap(assailant).addRally((float) (CombatData.getCap(assailant).getMaxPosture() * assailant.getAttributeValue(FootworkAttributes.BREACH_RALLY.get())));
                    }
                }
            }
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
        if (posture < 0) {
            ret = Math.abs(posture);
            posture = 0;
        }
        if (amount > 0) {
            addRally((amount - ret) * rallyPerc);
            //System.out.println("rally: "+getRally());
        }
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
        if (player) {
            rally = Mth.clamp(v, 0, getMaxPosture() - getPosture());//(float) Math.min(v, dude.get().getAttributeValue(FootworkAttributes.MAX_RALLY.get()));
            if (rally < 0) rally = 0;
            dirty = true;
        }
    }

    @Override
    public void rally(float amount) {
        //if (alreadyProc("rally")) return;
//        RallyPostureEvent rpe = new RallyPostureEvent(dude.get(), amount);
//        MinecraftForge.EVENT_BUS.post(rpe);
//        if (rpe.isCanceled()) return;
        //amount = rpe.getQuantity();//Math.min(rpe.getQuantity(), rally);
//        amount = Math.min(amount, rally);
//        rally -= amount;
        amount = rally;
        //rallyCD = RALLY_CD;
        //tickProc("rally");
        setPosture(posture + amount);//todo this is quite harsh
        rally = 0;
        dirty = true;
    }

    @Override
    public void retconDamage(float amount) {
        if (alreadyProc("rally")) return;
        recordedDamage = Math.min(dude.get().getMaxHealth(), recordedDamage);
        DamageRetconEvent rpe = new DamageRetconEvent(dude.get(), amount);
        MinecraftForge.EVENT_BUS.post(rpe);
        if (rpe.isCanceled()) return;
        amount = Math.min(rpe.getAmount(), recordedDamage);
        tickProc("rally");
        recordedDamage -= amount;
        if (recordedDamage < 0) recordedDamage = 0;
        dirty = true;
    }

    @Override
    public void tickProc(String key, double ticks) {
        procs.put(key, ticks);
        dirty = true;
    }

    @Override
    public double getProc(String key) {
        return procs.getOrDefault(key, 0d);
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
            pin(0);
            setHandBind(InteractionHand.MAIN_HAND, 0);
            setHandBind(InteractionHand.OFF_HAND, 0);
            SkillUtils.removeAttribute(e, Attributes.ARMOR, STOPMOVING);
        }//entering stagger
        else if (e != null && time > 0 && staggerTime == 0) {
            pin(time);
            setHandBind(InteractionHand.MAIN_HAND, time);
            setHandBind(InteractionHand.OFF_HAND, time);
            SkillUtils.addAttribute(e, Attributes.ARMOR, STOPMOVING);
        }
        maxStaggerTime = Math.max(maxStaggerTime, time);
        staggerTime = time;
        dirty = true;
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
        initializePostureIfNew(elb);

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

        //tick down everything
        //hand bind
        for (InteractionHand h : InteractionHand.values()) {
            if (getHandBind(h) > 0)
                CombatUtils.setHandCooldown(elb, h, 0, true);
        }
        mBind -= ticks;
        int prevOBind = oBind;
        oBind -= ticks;
        if (!CombatUtils.suppressChangeFunctions && (oBind > 0 || prevOBind > 0) && (oBind <= 0 || prevOBind <= 0))
            TwoHandingHandler.updateTwoHanding(elb, elb.getMainHandItem(), elb.getMainHandItem());

        //offhand cooldown
        offhandCD += ticks;

        //dodge/block/parry/iframe resolution
        setDodgeTime(dodgeFrame - ticks);
        if (elb.isShiftKeyDown()) {
            if (guardFrame < 0 && canParry())
                setParryTime(CombatConfig.parryTime);
            guardFrame = 2;
        }
        parryFrame -= ticks;
        iFrame -= ticks;
        guardFrame-=ticks;

        //stun
        if (isStunned())
            if (staggerTime - ticks > 0)
                staggerTime -= ticks;
            else {
                //do not reset posture when not stunned
                resetPosture();
                //player specific get up bonus
                if (dude.get() instanceof Player defender) {
                    setIframe(40);
                    for (Entity t : defender.level().getEntities(defender, defender.getBoundingBox().inflate(5), (a -> !TargetingUtils.isAlly(a, defender)))) {
                        float strength = 0.7f;
                        MobilityUtils.knockBack(t, defender, strength, true, false);

                    }
                }
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
        if (isKnockdown() && getPosture() < getMaxPosture()) {
            setPosture(getPosture() + getMaxPosture() / getMaxStunTime());
        } else {
            handlePostureRegen(ticks);
            handleSpiritRegen(ticks);
            //if (elb.isBlocking()) addPosture(0.01f);
        }
        if (getPosture() > getMaxPosture())
            setPosture(getMaxPosture());
        if (prev == null || !ItemStack.matches(elb.getOffhandItem(), prev)) {
            prev = elb.getOffhandItem();
            setOffhandCooldown(0);
        }

        //decrement or clear turn procs
        procs.replaceAll((k, v) -> v - 1);
        procs.entrySet().removeIf(entry -> entry.getValue() <= 0);

        lastUpdate = elb.level().getGameTime();
        first = false;
        sync();
    }

    private void initializePostureIfNew(LivingEntity elb) {
        final boolean uninitializedPosture = elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).getBaseValue() == 0d;
        if (uninitializedPosture) {
            final float mPos = getMPos(elb);
            elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).setBaseValue(mPos);
            if (!player) {//ew
                MobSpecs.MobInfo specs = MobSpecs.getMobInfo(elb);
                if (specs == null) specs = MobSpecs.DEFAULT;
                mobPosRegenSpd = specs.getPostureRegenerationSpeed();
                maxMobPosCD = specs.getPostureRegenerationCooldown();
                double permScale = specs.getMaxPostureScaling();
                if (permScale != 1)
                    elb.getAttribute(FootworkAttributes.MAX_POSTURE.get()).addPermanentModifier(new AttributeModifier(CombatUtils.main, "json bonus", permScale, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if (mpos == 0) {
            mpos = (float) elb.getAttributeValue(FootworkAttributes.MAX_POSTURE.get());
            posture = mpos;
        }
    }

    private void sync() {
        if (dirty) {//todo
            LivingEntity elb = dude.get();
            if (elb == null || elb.level().isClientSide) return;
            CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientResourcePacket(elb.getId(), write()));
            if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayer sp)
                CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientResourcePacket(elb.getId(), write()));
            dirty = false;
        }
    }

    @Override
    public void clientTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        final int ticks = 1;
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
        oBind -= ticks;
        LivingEntity e = dude.get();
        offhandCD += ticks;
        setDodgeTime(dodgeFrame - ticks);

        //stagger
        if (isStunned())
            if (staggerTime - ticks > 0)
                staggerTime -= ticks;
            else {
                maxStaggerTime = staggerTime = 0;
            }

        //regenerate posture
        if (isStunned() && getPosture() < getMaxPosture()) {
            setPosture(getPosture() + getMaxPosture() / getMaxStunTime());
        } else {
            handlePostureRegen(ticks);
            handleSpiritRegen(ticks);
            //if (elb.isBlocking()) addPosture(0.01f);
        }
        if (getPosture() > getMaxPosture())
            setPosture(getMaxPosture());
        if (prev == null || !ItemStack.matches(elb.getOffhandItem(), prev)) {
            prev = elb.getOffhandItem();
            setOffhandCooldown(0);
        }
        first = false;
    }

    @Override
    public int getOffhandCooldown() {
        return offhandCD;
    }

    @Override
    public void setOffhandCooldown(int i) {
        offhandCD = i;
        dirty = true;
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
//        if(dodgeFrame>0&&i<=0&&dude.get() instanceof Player p)
//            p.setForcedPose(Pose.SWIMMING);
        if (i <= 0 && dodgeFrame > 0 && dude.get() instanceof Player p)
            //cancel slide pose change
            p.setForcedPose(null);
        dodgeFrame = i;
        dirty = true;
    }

    @Override
    public boolean isParrying() {
        return parryFrame > 0 && !alreadyProc("cannot_parry");
    }

    @Override
    public int getParryCooldown() {
        return Math.max(parryFrame + CombatConfig.parryCD, 0);
    }

    @Override
    public float getParryCooldownPerc() {
        if (parryFrame > 0) return (float) -parryFrame / CombatConfig.parryTime;
        return (float) getParryCooldown() / CombatConfig.parryCD;
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
        dirty = true;
    }

    @Override
    public boolean isBlocking() {
        final LivingEntity e = dude.get();
        if (e == null) return false;
        //need to have at least one valid hand
        if (getHandBind(InteractionHand.MAIN_HAND) > 0 && getHandBind(InteractionHand.OFF_HAND) > 0) return false;
        return guardFrame > 0 && posture > 0 && !alreadyProc("cannot_block");
    }

    @Override
    public boolean canBlock() {
        return !alreadyProc("cannot_block") && posture > 0;
    }

    @Override
    public int getGuardTime() {
        return guardFrame;
    }

    @Override
    public void setGuardTime(int i) {
        guardFrame = i;
        dirty = true;
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
        dirty = true;
    }

    @Override
    public float getRecordedDamage() {
        return recordedDamage;
    }

    @Override
    public void recordDamage(float v) {
        if (v >= 0) healthyCooldown = 600;
        if (recordedDamage < 0) recordedDamage = 0;
        recordedDamage += v;
        dirty = true;
    }

    @Override
    public void stopRecording(DamageSource damageSource) {
        if (dude.get() != null && damageSource != null) {
            dude.get().hurt(damageSource, recordedDamage);
        }
        recordedDamage = 0;
        dirty = true;
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
        if (!CombatUtils.suppressChangeFunctions) {
            if (isStunned()) return 1;
            LivingEntity bro = dude.get();
            if (bro != null&&!bro.level().isClientSide) {
                if (h == InteractionHand.OFF_HAND && (WeaponStats.isTwoHanded(bro.getOffhandItem(), bro, InteractionHand.OFF_HAND) || (WeaponStats.isTwoHanded(bro.getMainHandItem(), bro, InteractionHand.MAIN_HAND) && WeaponStats.isWeapon(bro,bro.getOffhandItem()))))
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
                if (!CombatUtils.suppressChangeFunctions && (oBind == 0 || amount == 0) && oBind != amount && e != null)
                    TwoHandingHandler.updateTwoHanding(e, e.getMainHandItem(), e.getMainHandItem());
                oBind = amount;
            }
        }
    }

    @Override
    public CompoundTag write() {
        CompoundTag c = new CompoundTag();
        c.putFloat("spirit", spirit);
        c.putInt("mspi", mspi);
        c.putFloat("posture", posture);
        c.putFloat("mpos", mpos);
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
        c.putInt("spiCD", spiritCD);
        c.putInt("maxMobPosCD", maxMobPosCD);
        c.putInt("healthyCooldown", healthyCooldown);
        c.putDouble("mobPosRegenSpd", mobPosRegenSpd);
        c.putBoolean("player", player);
        CompoundTag proc = new CompoundTag();
        for (Map.Entry<String, Double> e : procs.entrySet()) {
            proc.putDouble(e.getKey(), e.getValue());
        }
        c.put("procs", proc);
        c.putFloat("recorded", recordedDamage);
        c.putInt("pin", pinTime);
        return c;
    }

    @Override
    public void read(CompoundTag t) {
        mspi = t.getInt("mspi");
        mpos = t.getFloat("mpos");
        setSpirit(t.getFloat("spirit"));
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
        spiritCD = t.getInt("spiCD");
        mobPosRegenSpd = t.getDouble("mobPosRegenSpd");
        healthyCooldown = t.getInt("healthyCooldown");
        if (t.contains("procs")) {
            procs.clear();
            CompoundTag c = (CompoundTag) t.get("procs");
            if (c != null) for (String id : c.getAllKeys()) {
                procs.put(id, c.getDouble(id));
            }
        }
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

    private void handlePostureRegen(int ticks) {
        mobPosCD -= ticks;
        if (mobPosRegenSpd == 0) mobPosRegenSpd = 0.4;
        float mult = 1;
        LivingEntity elb = dude.get();
        if (mobPosCD > 0) return;
        if (elb != null) {
            float healthperc = 0.3f + (elb.getHealth() / elb.getMaxHealth()) * 0.7f;
            if (player) {
                //players actually regenerate faster near death.
                //for the TENSION!
                healthperc = 0.65f + (1 - healthperc);
                healthperc *= Math.max(StylishData.getCap(elb).getCombo(), 0) / 40; //style
            }
            mult *= healthperc;
            //mult *= Math.min(CombatUtils.getCooledAttackStrength(elb, InteractionHand.MAIN_HAND, 0.5f), CombatUtils.getCooledAttackStrength(elb, InteractionHand.MAIN_HAND, 0.5f));
            int exp = elb.hasEffect(MobEffects.POISON) ? (elb.getEffect(MobEffects.POISON).getAmplifier() + 1) : 0;
            float poison = 1;
            for (int j = 0; j < exp; j++) {
                poison *= GeneralConfig.poison;
            }
            mult *= poison;
            double wetDebuff=0.8;
            if(elb.isInWaterRainOrBubble())
                mult*=wetDebuff;
        }
        if (mobPosCD < 0) {
            int overflow = -mobPosCD;
            mobPosCD = 0;
            if (player) addRally(overflow * mult);
            else setPosture((float) (getPosture() + overflow * mobPosRegenSpd * mult));
            healthyCooldown--;
            if (healthyCooldown < 0)
                recordDamage((float) Math.min(-0.01f, -getRecordedDamage() / 1000));
        }
    }

    private void handleSpiritRegen(int ticks) {
//        spiritCD -= ticks;
//        if (spiritCD <= 0) {
//            addSpirit((float) spiritCD / ReworkConstants.SPIRIT_QI);
//            spiritCD = 0;
//        }
    }
}
