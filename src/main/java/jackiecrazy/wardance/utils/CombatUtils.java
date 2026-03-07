package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.capability.weaponry.CombatManipulator;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.client.particle.ScalingParticleType;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.*;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import jackiecrazy.wardance.capability.charging.ChargingData;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.flyingweapon.IFlyingWeapon;
import jackiecrazy.wardance.capability.stylish.StylishCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.config.weapon.interactions.*;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.event.ProjectileDefendEvent;
import jackiecrazy.wardance.event.SweepEvent;
import jackiecrazy.wardance.mixin.LivingEntityAccessors;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.UpdateAttackCooldownPacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CombatUtils {

    public static final UUID off = UUID.fromString("8c8028c8-da69-49a2-99cd-f92d7ad22534");
    public static final UUID main = UUID.fromString("8c8028c8-da67-49a2-99cd-f92d7ad22534");
    public static boolean suppressChangeFunctions = false, allowCombatHotbarPickup = false;
    public static Vec3 temp_dest = Vec3.ZERO;
    private static ProjectileInfo DEFAULTRANGED = new ProjectileInfo(0.6, 1, false, false);
    private static HashMap<EntityType, ProjectileInfo> projectileMap = new HashMap<>();
    private static int cacheLeft, cacheRight;//primarily useful in client
    private static int cacheLeftAtk, cacheRightAtk;

    public static void updateProjectiles(List<? extends String> interpretP) {
        projectileMap.clear();
        DEFAULTRANGED = new ProjectileInfo(CombatConfig.posturePerProjectile, 1, false, false);
        for (String s : interpretP)
            try {
                String[] val = s.split(",");
                final ResourceLocation key = new ResourceLocation(val[0].trim());
                double posture = Double.parseDouble(val[1].trim());
                double count = Double.parseDouble(val[2].trim());
                boolean destroy = false, trigger = false;
                if (val.length > 3) {
                    String tags = val[3];
                    destroy = tags.contains("d");
                    trigger = tags.contains("t");
                }
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
                if (type != null)
                    projectileMap.put(type, new ProjectileInfo(posture, count, destroy, trigger));
            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted projectile parry definition " + s + "!");
            }
    }

    public static void attack(LivingEntity from, Entity to, boolean offhand) {
        if (offhand) {
            swapHeldItems(from);
            CombatData.getCap(from).setOffhandAttack(true);
        }
        if (from.attackStrengthTicker > 0) {
            int temp = from.attackStrengthTicker;
            CombatUtils.updateNormalAttackStatus(from);
            if (from instanceof Player p) p.attack(to);
            else from.doHurtTarget(to);
            from.attackStrengthTicker = temp;
        }
        if (offhand) {
            CombatUtils.swapHeldItems(from);
            CombatData.getCap(from).setOffhandAttack(false);
        }
    }

    public static float getCooledAttackStrength(LivingEntity e, InteractionHand h, float adjustTicks) {
        if (!(e instanceof Player) && h == InteractionHand.MAIN_HAND) return 1;
        //if (h == Hand.OFF_HAND && adjustTicks == 1) System.out.println(getCooldownPeriod(e, h));
        return Mth.clamp(((float) (h == InteractionHand.MAIN_HAND ? e.attackStrengthTicker : CombatData.getCap(e).getOffhandCooldown()) + adjustTicks) / getCooldownPeriod(e, h), 0.0F, 1.0F);
    }

    public static int getCooldownPeriod(LivingEntity e, InteractionHand h) {
        if (h == InteractionHand.MAIN_HAND) {
            if (e.tickCount == cacheRight) return cacheRightAtk;
            int ret = (int) (1.0D / GeneralUtils.getAttributeValueHandSensitive(e, Attributes.ATTACK_SPEED, h) * 20.0D);
            cacheRight = e.tickCount;
            cacheRightAtk = ret;
            return ret;
        } else {
            if (e.tickCount == cacheLeft) return cacheLeftAtk;
            int ret = (int) (1.0D / GeneralUtils.getAttributeValueHandSensitive(e, Attributes.ATTACK_SPEED, h) * 20.0D);
            cacheLeft = e.tickCount;
            cacheLeftAtk = ret;
            return ret;
        }
    }

    public static boolean isHoldingShield(LivingEntity e) {
        return WeaponStats.isShield(e, InteractionHand.MAIN_HAND) || WeaponStats.isShield(e, InteractionHand.OFF_HAND);
    }

    public static boolean isHoldingNonWeapon(LivingEntity living, InteractionHand h) {
        return isHoldingNonWeapon(living, living.getItemInHand(h));
    }

    public static boolean isHoldingNonWeapon(LivingEntity living, ItemStack is) {
        return (!WeaponStats.isWeapon(living, is) && !WeaponStats.isShield(living, is));
    }

    public static InteractionHand getShieldHand(LivingEntity e) {
        for (InteractionHand h : InteractionHand.values())
            if (WeaponStats.isShield(e, h)) return h;
        return null;
    }

    public static boolean isUnarmed(LivingEntity e, InteractionHand hand) {
        return WeaponStats.isUnarmed(e.getItemInHand(hand), e);
    }

    public static boolean isFullyUnarmed(LivingEntity e) {
        return isUnarmed(e, InteractionHand.MAIN_HAND) && isUnarmed(e, InteractionHand.OFF_HAND);
    }

    public static boolean canBlock(LivingEntity defender, Entity attacker, @Nonnull ItemStack i, float postureDamage) {
        return canBlock(defender, attacker, i, null, postureDamage);
    }

    public static boolean canBlock(LivingEntity defender,
                                   Entity attacker,
                                   @Nonnull ItemStack defend,
                                   @Nullable ItemStack attack,
                                   float postureDamage) {
        InteractionHand h = defender.getOffhandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        //what
        if (postureDamage < 0) return true;

        //cannot be parried
        if (attacker instanceof LivingEntity && getPostureDef((LivingEntity) attacker, defender, defend, postureDamage) < 0)
            return false;

        //the mob itself cannot block
        if (defender.getType().is(MobSpecs.CANNOT_BLOCK))
            return false;

        //the item cannot block
        if (defend.is(WeaponStats.CANNOT_BLOCK))
            return false;

        //attack pierces blocks,
        if (attack != null) {
            if (WeaponStats.canPierceShield(attack, attacker))
                return false;
        }
        //item cooldown
        if (defender instanceof Player p && defend.is(WeaponStats.CAN_BE_DISABLED) && p.getCooldowns().isOnCooldown(defend.getItem()))
            return false;
        //hand bound
        if (CombatData.getCap(defender).getHandBind(h) > 0)
            return false;
        float rand = WarDance.rand.nextFloat();
        //check the hand is off cooldown
        int bind = CombatData.getCap(defender).getHandBind(h);
        boolean notOnCooldown = bind <= 0;
        //notOnCooldown &= (!(defender instanceof Player p) || p.getCooldowns().getCooldownPercent(defender.getItemInHand(h).getItem(), 0) == 0);
        if (defend.getCapability(CombatManipulator.CAP).isPresent() && attacker instanceof LivingEntity) {
            return defend.getCapability(CombatManipulator.CAP).resolve().get().canBlock(defender, attacker, defend, notOnCooldown, postureDamage);
        }
        if (WeaponStats.isShield(defender, defend)) {
            boolean canShield = (defender instanceof Player || rand < CombatConfig.mobParryChanceShield);
            return notOnCooldown & canShield;
        } else if (WeaponStats.isWeapon(defender, defend)) {
            boolean canWeapon = (defender instanceof Player || rand < CombatConfig.mobParryChanceWeapon);
            return notOnCooldown & canWeapon;
        } else return false;
    }

    @Nullable
    public static ItemStack getAttackingItemStack(DamageSource ds) {
        if (ds instanceof CombatDamageSource)
            return ((CombatDamageSource) ds).getDamageDealer();
        else if (ds.getEntity() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) ds.getEntity();
            return e.getMainHandItem();//CombatData.getCap(e).isOffhandAttack() ? e.getHeldItemOffhand() : e.getHeldItemMainhand();
        }
        return null;
    }

    public static float getPostureAtk(LivingEntity attacker, LivingEntity defender, InteractionHand h) {
        return getPostureAtk(attacker, defender, h, null, GeneralUtils.getAttributeValueHandSensitive(attacker, Attributes.ATTACK_DAMAGE, h), attacker.getItemInHand(h));
    }

    public static float getPostureAtk(@Nullable LivingEntity attacker,
                                      @Nullable LivingEntity defender,
                                      @Nullable InteractionHand h,
                                      @Nullable DamageSource ds,
                                      double amount,
                                      ItemStack stack) {
        double base = amount * (float) WeaponStats.DEFAULTMELEE.getAttackPostureMultiplier();
        if (ds instanceof CombatDamageSource cds && cds.getPostureDamage() >= 0) return cds.getPostureDamage();
        float scaler = CombatConfig.mobScaler;
        if (stack != null && !stack.isEmpty()) {//weapon
            scaler = 1;
            if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
                base = stack.getCapability(CombatManipulator.CAP).resolve().get().postureDealtBase(attacker, defender, stack, amount);
            } else {
                final WeaponStats.WeaponInfo weaponInfo = WeaponStats.lookupStats(stack);
                if (weaponInfo != null) {
                    base = (float) weaponInfo.getAttackPostureMultiplier();
                    if (attacker != null) {
                        base *= WeaponStats.getHitInfo(attacker.getMainHandItem(), attacker, CombatUtils.getAttackState(attacker)).getPostureScale();
                        final HitInfo info = WeaponStats.getHitInfo(attacker.getMainHandItem(), attacker, CombatUtils.getAttackState(attacker));
                        base *= info.getPostureScale();
                    }
                }
            }
            //scale by mob and sweep
            if (attacker != null) {
                base *= MobSpecs.getOrDefault(attacker).getItemPostureScaling();
            }
            base *= ReworkConstants.POSTURE_QI;//temporary

        } else {//unarmed
            if (attacker != null && !(attacker instanceof Player)) {
                base = MobSpecs.getOrDefault(attacker).getBaseAttackPosture();
                if (base == -1)
                    base = CombatData.getCap(attacker).getMaxPosture() * CombatConfig.defaultMultiplierPostureMob;
            } else return 4;//magic number
        }
        if (attacker == null || h == null) return (float) base;
        double finalScale = scaler;
        if (attacker instanceof Player) {
            finalScale = (Math.max(CombatData.getCap(attacker).getProc("swing"), ((Player) attacker).getAttackStrengthScale(0.5f)) - 0.20) / 0.80;
        } else {
            //mob exhaustion penalty
            if (CombatData.getCap(attacker).getPosture() <= 0) {
                finalScale *= 0.3;
            }
        }
        return (float) (base * finalScale);
    }

    public static float getPostureDef(@Nullable LivingEntity attacker,
                                      @Nullable LivingEntity defender,
                                      ItemStack stack,
                                      float amount) {
        if (stack == null) return (float) WeaponStats.DEFAULTMELEE.getDefensePostureMultiplier();
//        if (defender != null && isShield(defender, stack) && CombatData.getCap(defender).getBarrierCooldown() > 0 && CombatData.getCap(defender).getBarrier() > 0) {
//            return 0;
//        }
        if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
            return stack.getCapability(CombatManipulator.CAP).resolve().get().postureMultiplierDefend(attacker, defender, stack, amount);
        }
        final WeaponStats.WeaponInfo weaponInfo = WeaponStats.lookupStats(stack);
        if (weaponInfo != null) {
            return (float) weaponInfo.getDefensePostureMultiplier();
        }
        return (float) WeaponStats.DEFAULTMELEE.getDefensePostureMultiplier();
    }

    public static void setHandCooldown(LivingEntity e, InteractionHand h, float percent, boolean sync) {
        //special case for quick maths
        int real = percent == 0 ? 0 : (int) (percent * getCooldownPeriod(e, h));
        switch (h) {
            case MAIN_HAND:
                if (!(e instanceof Player)) return;
                e.attackStrengthTicker = real;
                if (!(e instanceof FakePlayer) && e instanceof ServerPlayer sp && sync)
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateAttackCooldownPacket(e.getId(), real));
                break;
            case OFF_HAND:
                CombatData.getCap(e).setOffhandCooldown(real);
                break;
        }
    }

    public static void setHandCooldownDirect(LivingEntity e, InteractionHand h, int amount, boolean sync) {
        switch (h) {
            case MAIN_HAND:
                if (!(e instanceof Player)) return;
                e.attackStrengthTicker = amount;
                if (!(e instanceof FakePlayer) && e instanceof ServerPlayer sp && sync)
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateAttackCooldownPacket(e.getId(), amount));
                break;
            case OFF_HAND:
                CombatData.getCap(e).setOffhandCooldown(amount);
                break;
        }
    }

    public static void swapHeldItems(LivingEntity e) {
        //attributes = new ArrayList<>();
        ItemStack main = e.getMainHandItem(), off = e.getOffhandItem();
        int tssl = e.attackStrengthTicker;
        suppressChangeFunctions = true;
        ICombatCapability cap = CombatData.getCap(e);
        e.setItemInHand(InteractionHand.MAIN_HAND, e.getOffhandItem());
        e.setItemInHand(InteractionHand.OFF_HAND, main);
        int mbind = cap.getHandBind(InteractionHand.MAIN_HAND);
        cap.setHandBind(InteractionHand.MAIN_HAND, cap.getHandBind(InteractionHand.OFF_HAND));
        cap.setHandBind(InteractionHand.OFF_HAND, mbind);
        //tried really hard to make this work, but it just causes more problems.
        suppressChangeFunctions = false;
        main.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((att, mod) -> Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> mai.removeModifier(mod)));
        off.getAttributeModifiers(EquipmentSlot.OFFHAND).forEach((att, mod) -> Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> mai.removeModifier(mod)));
        main.getAttributeModifiers(EquipmentSlot.OFFHAND).forEach((att, mod) -> Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {
            if (!mai.hasModifier(mod))
                mai.addTransientModifier(mod);
        }));
        off.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((att, mod) -> Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {
            if (!mai.hasModifier(mod))
                mai.addTransientModifier(mod);
        }));
        e.attackStrengthTicker = cap.getOffhandCooldown();
        cap.setOffhandCooldown(tssl);
    }

    public static void quickSwap(LivingEntity e, ItemStack stack) {
        ItemStack main = e.getMainHandItem();
        suppressChangeFunctions = true;
        e.setItemInHand(InteractionHand.MAIN_HAND, stack);
        suppressChangeFunctions = false;

        main.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((att, mod) -> Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> mai.removeModifier(mod)));
        stack.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((att, mod) -> Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {
            if (!mai.hasModifier(mod)) mai.addTransientModifier(mod);
        }));
    }

    public static void applyFrames(LivingEntity swinger, HitInfo info) {
        if (info.guard_frames() > 0) CombatData.getCap(swinger).setGuardTime(info.guard_frames());
        if (info.parry_frames() > 0) CombatData.getCap(swinger).setParryTime(info.parry_frames());
        if (info.dodge_frames() > 0) CombatData.getCap(swinger).setDodgeTime(info.dodge_frames());
        if (info.invulnerable_frames() > 0) CombatData.getCap(swinger).setIframe(info.invulnerable_frames());
    }

    public static boolean processWeaponInteraction(LivingEntity e, Entity ignore, InteractionHand h, double reach) {
        ItemStack stack = e.getItemInHand(h);
        WeaponStats.AttackType s = getAttackState(e);
        WeaponInteractions.InteractionGroup group = WeaponStats.getSweepInfo(stack, e, s);
        if (CombatUtils.getCooledAttackStrength(e, h, 1f) < group.getMinimumCooldown()) return false;
        MovementUtils.applyVelocity(group.getVelocity(), e, group.isSetVelocity());
        group.on_swing().runEffects(e, e);
        for (WeaponInteractions.WeaponInteraction info : group.getInteractions()) {
            WeaponStats.info_override = info.getHitInfo();
            CombatUtils.applyFrames(e, info.getHitInfo());
            if (info instanceof SweepAttack sweep) {
                //apply instantaneous damage multiplier
                SkillUtils.modifyAttribute(e, Attributes.ATTACK_DAMAGE, main, sweep.getHitInfo().getDamageScale() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
                FlyingWeaponData.getCap(e).getWeapon(h).setUniversalOffset(h == InteractionHand.MAIN_HAND ? group.right_hand_offset() : group.left_hand_offset());
                enhancedSweep(e, ignore, h, sweep.getType(), reach, sweep.getBase(), sweep.getScaling());
                SkillUtils.removeAttribute(e, Attributes.ATTACK_DAMAGE, main);
            }
            if (info instanceof Use use) {
                //stack.releaseUsing(e.level(), e, use.getStartTime());
                if (e instanceof Player p) {
                    ChargingData.getCap(p).alterSpeed(stack, use.getUseSpeed());
                    stack.use(e.level(), p, h);
                    p.startUsingItem(h);
                }
            }
            if (info instanceof Animation anim) {
                FlyingWeaponData.getCap(e).getWeapon(h).setUniversalOffset(h == InteractionHand.MAIN_HAND ? group.right_hand_offset() : group.left_hand_offset());
                for (MotionManager mm : anim.getAnimations())
                    FlyingWeaponData.getCap(e).scheduleAction(h, mm);
            }
            if (info instanceof Throw t) {
                final IFlyingWeapon cap = FlyingWeaponData.getCap(e);
                if (temp_dest == null) temp_dest = e.getEyePosition().add(e.getLookAngle().scale(32));
                ThrownWeaponEntity fwe = cap.yeet(h, temp_dest, t.getThrowSpeed());
                t.transformThrown(fwe);
                if (t.consume() && e instanceof Player player && !player.getAbilities().instabuild) {
                    final ItemStack held = player.getItemInHand(h);
                    held.shrink(1);
                    player.getInventory().setChanged();
                    if (held.getCount() == 0) {
                        player.setItemInHand(h, ItemStack.EMPTY);
                    }
                }
                cap.forceRefreshWeapons();
                temp_dest = null;
            }
            WeaponStats.info_override = null;
        }
        setHandCooldown(e, h, (float) group.getCooldownRefund(), true);
        return true;
    }

    public static void enhancedSweep(LivingEntity e,
                                     Entity ignore,
                                     InteractionHand h,
                                     SweepAttack.SWEEPTYPE type,
                                     double reach,
                                     double base,
                                     double scaling) {


        //no go cases
        if (!GeneralConfig.betterSweep) return;//a shame, but alas
        if (!StylishData.getCap(e).isCombatMode()) return;
        if (CombatData.getCap(e).getHandBind(h) > 0) return;//don't even try dude

        if (h == InteractionHand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(true);
        }


        if (!PermissionData.getCap(e).canSweep()) type = SweepAttack.SWEEPTYPE.NONE;
        double radius;

        SweepEvent sre = new SweepEvent(e, h, e.getMainHandItem(), type, base, scaling);
        MinecraftForge.EVENT_BUS.post(sre);
        if (!CombatData.getCap(e).alreadyProc("sweepState"))
            CombatUtils.updateNormalAttackStatus(e);
        base = sre.getBase();
        scaling = sre.getScaling();
        radius = sre.getFinalizedWidth();
        type = sre.getType();

        //purely visual attack
        int time = CombatUtils.getCooldownPeriod(e, h);
        int animTime = type == SweepAttack.SWEEPTYPE.CIRCLE ? 10 : 5;
        if (type == SweepAttack.SWEEPTYPE.CIRCLE) {
            animTime = 10;//smoother
            reach = radius;
        }
        List<FlyingWeaponEffect> fx = new ArrayList<>();
        fx.add(FlyingWeaponEffect.WEAPON);
        if (StylishData.getCap(e).getFreshness(StylishCapability.getNormalAttackString(e)) > 0) {
            fx.add(FlyingWeaponEffect.TRAIL);
        }
        if (TimeSlowData.getCap(e).getEffectiveSpeed() < 1) {
            fx.add(FlyingWeaponEffect.AFTERIMAGE);
        }
        FlyingWeaponData.getCap(e).scheduleAction(h, TemporaryMoveTranslator.temp_getMMFromType(animTime, type, radius, null, reach), fx.toArray(new FlyingWeaponEffect[fx.size()]));


        if (sre.isCanceled() || type == SweepAttack.SWEEPTYPE.NONE || radius == 0) {
            //no go, swap items back and stop
            if (h == InteractionHand.OFF_HAND) {
                swapHeldItems(e);
                CombatData.getCap(e).setOffhandAttack(false);
            }
            return;
        }
        if (e.getMainHandItem().getCapability(CombatManipulator.CAP).isPresent())
            radius = e.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().sweepArea(e, e.getMainHandItem());


        double charge = Math.max(CombatUtils.getCooledAttackStrength(e, InteractionHand.MAIN_HAND, 0.5f), CombatData.getCap(e).getProc("swing"));
        //any sweep of mine is going to be SOMETHING
        boolean hit = false;
        if (ignore != null)
            CombatData.getCap(e).tickProc("oncePerSweep");
        Vec3 starting = ignore == null ? GeneralUtils.raytraceAnything(e.level(), e, reach).getLocation() : ignore.position();
        //grab everyone in "range"
        for (Entity target : e.level().getEntities(e, e.getBoundingBox().inflate(reach * 2))) {
            if (target == e) continue;
            if (target.getType().is(MobSpecs.IGNORED_BY_SWEEP)) continue;//poor item frames
            if (target.hasPassenger(e) || e.hasPassenger(target)) continue;//poor horse
            if (target == ignore) {
                if (radius > 0)
                    hit = true;
                continue;
            }
            if (!e.hasLineOfSight(target)) continue;
            //type specific sweep checks
            switch (type) {
                case CONE -> {
                    if (!GeneralUtils.isFacingEntity(e, target, (int) radius, 40)) continue;
                    if (GeneralUtils.getDistSqCompensated(e, target) > reach * reach) continue;
                }
                case CLEAVE -> {
                    if (!GeneralUtils.isFacingEntity(e, target, 40, (int) radius)) continue;
                    if (GeneralUtils.getDistSqCompensated(e, target) > reach * reach) continue;
                }
                case IMPACT -> {
                    if (GeneralUtils.getDistSqCompensated(target, starting) > radius * radius) continue;
                }
                case CIRCLE -> {
                    if (GeneralUtils.getDistSqCompensated(target, e) > radius * radius) continue;
                }
                case LINE -> {
                    Vec3 eye = e.getEyePosition(0.5F);
                    Vec3 look = e.getLookAngle();
                    Vec3 start = eye.add(look.scale(radius));
                    Vec3 end = eye.add(look.scale(reach));
                    final AABB inflated = target.getBoundingBox().inflate(radius);
                    if (!inflated.intersects(start, end) && !inflated.contains(start) && !inflated.contains(end))
                        continue;
                }
            }

            CombatUtils.setHandCooldown(e, InteractionHand.MAIN_HAND, (float) charge, false);
            hit = true;
            if (e instanceof Player p)
                p.attack(target);
            else e.doHurtTarget(target);
            CombatData.getCap(e).tickProc("oncePerSweep");
        }
        //if (e instanceof Player && hit) {
        //play sweep particles in different ways
        ParticleType<ScalingParticleType> particle = FootworkParticles.SWEEP.get();
        Vec3 look = e.getLookAngle();
        starting = e.getEyePosition().add(look.scale(reach));
        float offset = 0;//(float) look.scale(reach).y;
        switch (type) {
            case LINE -> {
                particle = FootworkParticles.LINE.get();
                //ParticleUtils.playSweepParticle(particle, e, e.getEyePosition().add(look.normalize()), 0, radius, Color.WHITE, offset);
            }
            case CIRCLE -> {
                starting = e.position().add(look.x, 0, look.z);
                particle = FootworkParticles.CIRCLE.get();
                offset = e.getEyeHeight() / 2;
            }
            case CONE -> {
                radius = Math.tan(GeneralUtils.rad((float) radius / 2)) * reach;
                particle = h == InteractionHand.OFF_HAND ? FootworkParticles.SWEEP_LEFT.get() : FootworkParticles.SWEEP.get();
            }
            case CLEAVE -> {
                particle = FootworkParticles.CLEAVE.get();
                radius = Math.tan(GeneralUtils.rad((float) radius / 2)) * reach;
            }
            case IMPACT -> {
                particle = FootworkParticles.IMPACT.get();
                offset = 0;
                ParticleUtils.playSweepParticle(particle, e, starting, 0, radius, sre.getColor(), offset);
            }
        }
        e.level().playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, e.getSoundSource(), 1.0F, 1.0F);
        //}
        CombatData.getCap(e).tickProc("oncePerSweep", 0);
        if (h == InteractionHand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(false);
        }
    }

    public static void initializePPE(ProjectileDefendEvent ppe, float mult) {
        final EntityType<?> type = ppe.getProjectile().getType();
        ProjectileInfo pi = projectileMap.getOrDefault(type, DEFAULTRANGED);
        ppe.setReturnVec(pi.destroy | type.is(MobSpecs.DESTROY_ON_PARRY) ? null : ppe.getProjectile().getDeltaMovement().normalize().scale(-0.1));
        ppe.setPostureConsumption((float) pi.posture * mult);
        ppe.setTrigger(pi.trigger | type.is(MobSpecs.TRIGGER_ON_PARRY));
    }

    public static void setAttackType(LivingEntity entity, WeaponStats.AttackType set) {
        CombatData.getCap(entity).tickProc("sweepState", set.ordinal());
    }

    public static void updateNormalAttackStatus(LivingEntity entity) {
        WeaponStats.AttackType set = WeaponStats.AttackType.STANDING;
        //if (AerialModeData.getCap(entity).getEffectiveSpeed() < 1) set = WeaponStats.AttackType.AERIAL;
        if (entity.isSwimming() || entity.isSprinting() || entity.isFallFlying() || CombatData.getCap(entity).isDodging())
            set = WeaponStats.AttackType.SPRINTING;
        if ((!(entity instanceof Player p) || !p.getAbilities().flying) && !entity.onGround() && entity.fallDistance > 0 && !entity.onClimbable() && !entity.isInWater())
            set = WeaponStats.AttackType.FALLING;
        if (AerialModeData.getCap(entity).getEffectiveSpeed() < 1) set = WeaponStats.AttackType.AERIAL;
        switch (AerialModeData.getCap(entity).getState()) {
            case CLING, CEILING_CLING -> set = WeaponStats.AttackType.STANDING;
            case WALL_SLIDE -> set = WeaponStats.AttackType.SPRINTING;
            case WALL_JUMP -> set = WeaponStats.AttackType.AERIAL;
        }
        CombatData.getCap(entity).tickProc("sweepState", set.ordinal());
    }

    public static WeaponStats.AttackType getAttackState(LivingEntity entity) {
        if (CombatData.getCap(entity).alreadyProc("sweepState"))
            return WeaponStats.AttackType.values()[(int) CombatData.getCap(entity).getProc("sweepState")];
        return WeaponStats.AttackType.UNDEFINED;
    }

    public static void onSuccessfulBlock(LivingEntity defender,
                                         Entity attacker,
                                         @Nullable InteractionHand hand,
                                         @Nullable ItemStack defend,
                                         float amount) {
        //quickly refill one trigger and trigger block effects
        //add 15% extra posture damage to the mob, removed on next hit but stacks
        //If the attack was guard breaking (entity flag 30) disable block for a while (handled somewhere else)
        defender.level().playSound(null, defender.getX(), defender.getY(), defender.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, WarDance.rand.nextFloat() * 0.3f + Math.min(1f, 1 - CombatData.getCap(defender).getPosturePercentage()), Math.min(0.75f, amount / 7) + WarDance.rand.nextFloat() * 0.5f);
        StylishData.getCap(defender).addTriggerTime(10, true);
        StylishData.getCap(defender).addCombo(0.1f, "block");

        if (attacker instanceof LivingEntity le) {
            //THIS DOESN'T KNOCK BACK ANYONE!
            //so I have to do it here
            float strength = attacker instanceof Player ? 0.3f : 0.5f;
            //prioritize mobs for knockback
            if (le instanceof Player) {
                MobilityUtils.knockBack(defender, le, strength, true, false);
                EffectUtils.attemptAddPot(defender, EffectUtils.stackPot(defender, new MobEffectInstance(FootworkEffects.COUNTERSTRIKE.get(), 100, 0), EffectUtils.StackingMethod.MAXDURATION), true);
            } else {
                ((LivingEntityAccessors) (defender)).callBlockUsingShield(le);
                MobilityUtils.knockBack(le, defender, strength, true, false);
                EffectUtils.attemptAddPot(le, EffectUtils.stackPot(le, new MobEffectInstance(FootworkEffects.COUNTERSTRIKE.get(), 100, 0), EffectUtils.StackingMethod.MAXDURATION), true);
            }
        }

        //hacky. If you can no longer block it must mean your block has been breached, so knock back. FIXME
        if (!CombatData.getCap(defender).canBlock())
            MobilityUtils.knockBack(defender, attacker, 1.2f, false, true);

        //item specific effects
        if (defend != null) {
            //hacky. Instantly trigger block for blocking items
            if (hand != null && defend.getUseAnimation() == UseAnim.BLOCK) {
                defender.startUsingItem(hand);
            }
            ItemStack finalDefend = defend;
            defend.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onBlock(defender, attacker, finalDefend, amount));
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack finalDefend1 = defender.getItemInHand(other);
            finalDefend1.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onOtherHandBlock(defender, attacker, finalDefend1, amount));
        }
    }

    public static void onIdleGuard(LivingEntity defender,
                                   Entity attacker,
                                   @Nullable InteractionHand hand,
                                   @Nullable ItemStack defend,
                                   float amount) {
        //simply knock both sides back
        //knockback based on posture consumed
        //defender kb
        final float kb = Mth.sqrt(amount);
        MobilityUtils.knockBack(defender, attacker, (defender instanceof Player ? 0.25f : 0.5f) * kb, true, false);
        //attacker kb
        MobilityUtils.knockBack(attacker, defender, (attacker instanceof Player ? 0.25f : 0.5f) * kb, true, false);
        defender.level().playSound(null, defender.getX(), defender.getY(), defender.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.25f, (1 - CombatData.getCap(defender).getPosturePercentage()) + WarDance.rand.nextFloat() * 0.5f);
    }

    public static void triggerSteveTime(LivingEntity from, int time) {
        //ZA WAAAAARUDO! TOKI WO TOMARE!
        TimeSlowData.getCap(from).alterSpeed(time, 0.1);
        for (Entity t : from.level().getEntities(from, from.getBoundingBox().inflate(32), (a -> !(a instanceof FlyingItemEntity)))) {
            TimeSlowData.getCap(t).alterSpeed(time, 0.1);
            //jostle everything a tiny amount so you know the time slow is happening
            MobilityUtils.knockBack(t, from, 0.2f, true, false);
        }
        if (from.level() instanceof ServerLevel s)
            for (int i = 0; i < 32; i++) {
                double x = from.getX(), y = from.getY(), z = from.getZ();
                float radians = GeneralUtils.rad(i * 360f / 32f);

                final double sin = Mth.sin(radians) * 32;
                final double cos = Mth.cos(radians) * 32;
                s.sendParticles(new DustParticleOptions(ParticleUtils.gravel, 1), x + cos, y + 0.7, z + sin, 0, 0, 0, 0.0D, 0);
            }
    }

    public static void onSuccessfulDodge(LivingEntity defender, Entity attacker) {
        //Perfect dodging maxes out spirit.
        //slow all mobs in a 32 block range for about 2 seconds and convert remaining dodge frames to iframes to stop repeated procs
        defender.level().playSound(null, defender.getX(), defender.getY(), defender.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        StylishData.getCap(defender).addCombo(0.2f, "dodge");
        ICombatCapability cap = CombatData.getCap(defender);
        int remaining = cap.getDodgeTime();
        if (attacker instanceof LivingEntity e) {
            CombatData.getCap(e).setHandBind(InteractionHand.MAIN_HAND, remaining);//prevent further attacks
        }
        cap.setDodgeTime(CombatConfig.rollTime);
        cap.setIframe(remaining);

        if (defender instanceof Player) {
            triggerSteveTime(defender, 30);
        }
    }

    public static void onSuccessfulParry(LivingEntity defender,
                                         Entity attacker,
                                         @Nullable InteractionHand hand,
                                         @Nullable ItemStack defend,
                                         float amount, float damage) {
        int radius = 5;

        //resolve trigger charges and emit a shockwave that deals ??? posture damage in an area. Cannot breach.
        //grant 2 seconds of iframes, which conveniently stops repeated parrying
        //FakeExplosion.explode(defender.level(), defender, defender.getX(), defender.getY() + defender.getBbHeight() * 1.1f, defender.getZ(), 5);
        defender.level().playSound(null, defender.getX(), defender.getY(), defender.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, Math.min(1, amount / 10) + WarDance.rand.nextFloat() * 0.3f, 0.5f + WarDance.rand.nextFloat() * 0.25f);
        StylishData.getCap(defender).addCombo(0.3f, "parry");
        ICombatCapability cap = CombatData.getCap(defender);
        StylishData.getCap(defender).processAttack(true);
        StylishData.getCap(defender).processAttack(false);
        cap.setParryTime(CombatConfig.parryTime);

        if (defender.level() instanceof ServerLevel s)
            for (int i = 0; i < 32; i++) {
                double x = defender.getX(), y = defender.getY(), z = defender.getZ();
                float radians = GeneralUtils.rad(i * 360f / 32f);

                final double sin = Mth.sin(radians) * radius;
                final double cos = Mth.cos(radians) * radius;
                s.sendParticles(new DustParticleOptions(ParticleUtils.gravel, 1), x + cos, y + 0.7, z + sin, 0, 0, 0, 0.0D, 0);
            }

        if (defender instanceof Player) {
            if (hand == null)
                hand = defender.getOffhandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

            if (attacker instanceof LivingEntity e) {
                CombatData.getCap(e).consumePosture(defender, 4, ICombatCapability.BreachLevel.KNOCKDOWN);
                CombatData.getCap(e).recordDamage((float) damage);
            }

            for (Entity t : defender.level().getEntities(defender, defender.getBoundingBox().inflate(radius), (a -> !TargetingUtils.isAlly(a, defender)))) {
                float strength = 1.3f;
                if (t instanceof LivingEntity e) {
                    CombatData.getCap(e).consumePosture(defender, 4, ICombatCapability.BreachLevel.NO);
                    CombatData.getCap(e).recordDamage((float) damage);
                    strength = Math.min(strength, 0.2f + Mth.clamp(amount * 1 - CombatData.getCap(e).getPosturePercentage(), 0, 1));
                }
                MobilityUtils.knockBack(t, defender, strength, true, false);

            }
        }

        //perform item related procs
        if (defend != null) {
            ItemStack finalDefend = defend;
            defend.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onParry(defender, attacker, finalDefend, amount));
            InteractionHand other = defender.getMainHandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack finalDefend1 = defender.getItemInHand(other);
            finalDefend1.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onOtherHandParry(defender, attacker, finalDefend1, amount));
        }
    }

    public static void kick(LivingEntity kicker, Entity targetEntity, boolean breach) {
        kicker.level().playSound(null, kicker.getX(), kicker.getY(), kicker.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        if (targetEntity instanceof LivingEntity target) {
            CombatData.getCap(kicker).tickProc("qiSpent");
            StylishData.getCap(kicker).addCombo(0.1f, "kick" + breach);
            CombatData.getCap(target).consumePosture(kicker, 12, breach);
            ParticleUtils.playBonkParticle(kicker.level(), kicker.getEyePosition().add(kicker.getLookAngle().scale(Math.sqrt(GeneralUtils.getDistSqCompensated(kicker, target)))), 1, 0, 8, Color.WHITE);
            target.hurt(new CombatDamageSource(kicker).setPostureDamage(0).setDamageTyping(FootworkDamageArchetype.PHYSICAL).flagBreach(breach).setProcAttackEffects(true), 1);
            if (target.getLastHurtByMob() == null)
                target.setLastHurtByMob(kicker);
        }
        MobilityUtils.knockBack(targetEntity, kicker, 0.6f, true, false);
    }

    public static boolean scheduleFinisher(ServerPlayer sender, InteractionHand h, WeaponStats.AttackType s) {
//        if (!StylishData.getCap(sender).canTrigger() && !sender.getAbilities().instabuild) {
//            sender.displayClientMessage(Component.literal("Not enough Finisher Charge! Currently " + StylishData.getCap(sender).getTriggerBar()), true);
//            return false;
//        }
        if (!StylishData.getCap(sender).isCombatMode()) return false;
        if (CombatData.getCap(sender).getHandBind(h) > 0) return false;
        //StylishData.getCap(sender).resetTriggerBar();
        WeaponInteractions.InteractionGroup info = WeaponStats.getSweepInfo(sender.getItemInHand(h), sender, s);
//        if (info instanceof SweepAttack sa)
//            TemporaryMoveTranslator.scheduleFinisher(sender, h, sa);
        StylishData.getCap(sender).addCombo(0.25f, "heavy" + (h == InteractionHand.OFF_HAND) + s.name());
        return true;
    }

    private static class ProjectileInfo {
        private final double posture;
        private final double count;
        private final boolean destroy, trigger;

        private ProjectileInfo(double p, double c, boolean d, boolean t) {
            posture = p;
            count = c;
            destroy = d;
            trigger = t;
        }
    }

}
