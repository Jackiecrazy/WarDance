package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.weaponry.CombatManipulator;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.client.particle.ScalingParticleType;
import jackiecrazy.footwork.event.AttackMightEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.event.SweepEvent;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.UpdateAttackCooldownPacket;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CombatUtils {
    public static final UUID off = UUID.fromString("8c8028c8-da69-49a2-99cd-f92d7ad22534");
    public static final UUID main = UUID.fromString("8c8028c8-da67-49a2-99cd-f92d7ad22534");
    public static boolean isSweeping = false;
    public static boolean suppress = false;
    private static ProjectileInfo DEFAULTRANGED = new ProjectileInfo(0.6, 1, false, false);
    private static HashMap<EntityType, ProjectileInfo> projectileMap = new HashMap<>();

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
            if (from instanceof Player) ((Player) from).attack(to);
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
        return (int) (1.0D / GeneralUtils.getAttributeValueHandSensitive(e, Attributes.ATTACK_SPEED, h) * 20.0D);
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

    public static boolean canParry(LivingEntity defender, Entity attacker, @Nonnull ItemStack i, float postureDamage) {
        return canParry(defender, attacker, i, null, postureDamage);
    }

    public static boolean canParry(LivingEntity defender, Entity attacker, @Nonnull ItemStack defend, @Nullable ItemStack attack, float postureDamage) {
        InteractionHand h = defender.getOffhandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (postureDamage < 0) return false;
        if (attacker instanceof LivingEntity && getPostureDef((LivingEntity) attacker, defender, defend, postureDamage) < 0)
            return false;
        //can't parry lah
        if (defender.getType().is(MobSpecs.CANNOT_PARRY))
            return false;
        if (defend.is(WeaponStats.CANNOT_PARRY))
            return false;
        //attack pierces parry/shield
        if (attack != null) {
            if (attack.is(WeaponStats.PIERCE_PARRY) && WeaponStats.isWeapon(defender, defend))
                return false;
            if (attack.is(WeaponStats.PIERCE_SHIELD) && WeaponStats.isShield(defender, defend))
                return false;
        }
        //item cooldown
        if (defender instanceof Player p && defend.is(WeaponStats.CAN_BE_DISABLED) && p.getCooldowns().isOnCooldown(defend.getItem()))
            return false;
        //hand bound
        if (CombatData.getCap(defender).getHandBind(h) > 0)
            return false;
        float rand = WarDance.rand.nextFloat();
        boolean recharge = !WeaponStats.isShield(defender, defend) || getCooledAttackStrength(defender, h, 0.5f) > 0.9f && CombatData.getCap(defender).getHandBind(h) == 0;
        recharge &= (!(defender instanceof Player) || ((Player) defender).getCooldowns().getCooldownPercent(defender.getItemInHand(h).getItem(), 0) == 0);
        if (defend.getCapability(CombatManipulator.CAP).isPresent() && attacker instanceof LivingEntity) {
            return defend.getCapability(CombatManipulator.CAP).resolve().get().canBlock(defender, attacker, defend, recharge, postureDamage);
        }
        if (WeaponStats.isShield(defender, defend)) {
            boolean canShield = (defender instanceof Player || rand < CombatConfig.mobParryChanceShield);
            boolean canParry = true;//CombatData.getCap(defender).getBarrierCooldown() == 0 || CombatData.getCap(defender).getBarrier() > 0;
            return recharge & canParry & canShield;
        } else if (WeaponStats.isWeapon(defender, defend)) {
            boolean canWeapon = (defender instanceof Player || rand < CombatConfig.mobParryChanceWeapon);
            return recharge & canWeapon;
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

    public static float getPostureAtk(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, @Nullable InteractionHand h, @Nullable DamageSource ds, double amount, ItemStack stack) {
        double base = amount * (float) WeaponStats.DEFAULTMELEE.getAttackPostureMultiplier();
        //Spartan Shields compat, doesn't seem to work?
        if (attacker != null && attacker.isBlocking()) {
            h = attacker.getUsedItemHand();
            stack = attacker.getItemInHand(h);
        }
        if (ds instanceof CombatDamageSource cds && cds.getPostureDamage() >= 0) return cds.getPostureDamage();
        float scaler = CombatConfig.mobScaler;
        if (stack != null && !stack.isEmpty()) {//weapon
            scaler = 1;
            if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
                base = stack.getCapability(CombatManipulator.CAP).resolve().get().postureDealtBase(attacker, defender, stack, amount);
            } else {
                final WeaponStats.MeleeInfo meleeInfo = WeaponStats.lookupStats(stack);
                if (meleeInfo != null) {
                    base = (float) meleeInfo.getAttackPostureMultiplier();
                    if (attacker != null) {
                        final WeaponStats.SweepInfo info = WeaponStats.getSweepInfo(attacker.getMainHandItem(), CombatUtils.getSweepState(attacker));
                        base *= info.getPostureScale();
                    }
                }
            }
            //scale by mob and sweep
            if (attacker != null) {
                base *= MobSpecs.mobMap.getOrDefault(attacker.getType(), MobSpecs.DEFAULT).getItemPostureScaling();
            }

        } else {//unarmed
            if (attacker != null && !(attacker instanceof Player)) {
                base = MobSpecs.mobMap.getOrDefault(attacker.getType(), MobSpecs.DEFAULT).getBaseAttackPosture();
                if (base == -1)
                    base = CombatData.getCap(attacker).getMaxPosture() * CombatConfig.defaultMultiplierPostureMob;
            }
        }
        if (attacker == null || h == null) return (float) base;
        double finalScale = scaler;
        if (attacker instanceof Player) {
            finalScale = (Math.max(CombatData.getCap(attacker).getCachedCooldown(), ((Player) attacker).getAttackStrengthScale(0.5f)) - 0.20) / 0.80;
        }
        return (float) (base * finalScale);
    }

    public static float getPostureDef(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, ItemStack stack, float amount) {
        if (stack == null) return (float) WeaponStats.DEFAULTMELEE.getDefensePostureMultiplier();
//        if (defender != null && isShield(defender, stack) && CombatData.getCap(defender).getBarrierCooldown() > 0 && CombatData.getCap(defender).getBarrier() > 0) {
//            return 0;
//        }
        if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
            return stack.getCapability(CombatManipulator.CAP).resolve().get().postureMultiplierDefend(attacker, defender, stack, amount);
        }
        final WeaponStats.MeleeInfo meleeInfo = WeaponStats.lookupStats(stack);
        if (meleeInfo != null) {
            return (float) meleeInfo.getDefensePostureMultiplier();
        }
        return (float) WeaponStats.DEFAULTMELEE.getDefensePostureMultiplier();
    }

    public static float getAttackMight(LivingEntity seme, LivingEntity uke) {
        ICombatCapability semeCap = CombatData.getCap(seme);
        final float magicScale = 1.722f;
        final float magicNumber = 1562.5f;//magic numbers scale the modified formula to 0.1 per sword hit
        final float cooldownSq = semeCap.getCachedCooldown() * semeCap.getCachedCooldown();
        final double period = 20.0D / (seme.getAttribute(Attributes.ATTACK_SPEED).getValue() + 0.5d);//+0.5 makes sure heavies don't scale forever, light ones are still puny
        float might = cooldownSq * cooldownSq * magicScale * (float) period * (float) period / magicNumber;
        might *= (1f + (semeCap.getRank() / 20f));//combo bonus
        float weakness = 1;
        if (seme.hasEffect(MobEffects.WEAKNESS))
            for (int foo = 0; foo < seme.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1; foo++) {
                weakness *= GeneralConfig.weakness;
            }
        might *= weakness;//weakness malus
        AttackMightEvent ame = new AttackMightEvent(seme, uke, might);
        MinecraftForge.EVENT_BUS.post(ame);
        return ame.getQuantity();
    }

    /**
     * knocks the target back, with regards to the attacker's relative angle to the target, and adding y knockback
     */
    public static void knockBack(Entity to, Entity from, float strength, boolean considerRelativeAngle, boolean bypassAllChecks) {
        Vec3 distVec = to.position().add(0, to.getBbHeight() / 2, 0).vectorTo(from.position().add(0, from.getBbHeight() / 2, 0)).multiply(1, 0.5, 1).normalize();
        if (to instanceof LivingEntity && !bypassAllChecks) {
            if (considerRelativeAngle)
                knockBack((LivingEntity) to, strength, distVec.x, distVec.y, distVec.z, false);
            else
                knockBack(((LivingEntity) to), (float) strength * 0.5F, (double) Mth.sin(from.getYRot() * 0.017453292F), 0, (double) (-Mth.cos(from.getYRot() * 0.017453292F)), false);
        } else {
            //eh
            if (considerRelativeAngle) {
                to.lerpMotion(distVec.x * -strength, to.verticalCollision ? 0.1 : distVec.y * -strength, distVec.z * -strength);
            } else {
                to.push(-Mth.sin(-from.getYRot() * 0.017453292F - (float) Math.PI) * 0.5, 0.1, -Mth.cos(-from.getYRot() * 0.017453292F - (float) Math.PI) * 0.5);
            }
            to.hurtMarked = true;
        }
    }

    /**
     * knockback in LivingEntity except it makes sense and the resist is factored into the event
     */
    public static void knockBack(LivingEntity to, float strength, double xRatio, double yRatio, double zRatio, boolean bypassEventCheck) {
        if (!bypassEventCheck) {
            net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(to, strength, xRatio, zRatio);
            if (event.isCanceled()) return;
            strength = event.getStrength();
            xRatio = event.getRatioX();
            zRatio = event.getRatioZ();
        }
        strength *= (float) Math.max(0, 1 - GeneralUtils.getAttributeValueSafe(to, Attributes.KNOCKBACK_RESISTANCE));
        if (strength != 0f) {
            Vec3 vec = to.getDeltaMovement();
            double motionX = vec.x, motionY = vec.y, motionZ = vec.z;
            to.hasImpulse = true;
            double pythagora = Math.sqrt(xRatio * xRatio + zRatio * zRatio);
            if (to.onGround()) {
                motionY /= 2.0D;
                motionY += Math.abs(strength);

                if (motionY > 0.4000000059604645D) {
                    motionY = 0.4000000059604645D;
                }
            } else if (yRatio != 0) {
                pythagora = Math.sqrt(xRatio * xRatio + zRatio * zRatio + yRatio * yRatio);
                motionY /= 2.0D;
                motionY -= yRatio / (double) pythagora * (double) strength;
            }
            motionX /= 2.0D;
            motionZ /= 2.0D;
            motionX -= xRatio / (double) pythagora * (double) strength;
            motionZ -= zRatio / (double) pythagora * (double) strength;
            to.setDeltaMovement(motionX, motionY, motionZ);
            to.hurtMarked = true;
        }
    }

    public static void setHandCooldown(LivingEntity e, InteractionHand h, float percent, boolean sync) {
        //special case for quick maths
        int real = percent == 0 ? 0 : (int) (percent * getCooldownPeriod(e, h));
        switch (h) {
            case MAIN_HAND:
                if (!(e instanceof Player)) return;
                e.attackStrengthTicker = real;
                if (!(e instanceof FakePlayer) && e instanceof ServerPlayer && sync)
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e), new UpdateAttackCooldownPacket(e.getId(), real));
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
                if (!(e instanceof FakePlayer) && e instanceof ServerPlayer && sync)
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e), new UpdateAttackCooldownPacket(e.getId(), amount));
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
        suppress = true;
        ICombatCapability cap = CombatData.getCap(e);
        e.setItemInHand(InteractionHand.MAIN_HAND, e.getOffhandItem());
        e.setItemInHand(InteractionHand.OFF_HAND, main);
        int mbind = cap.getHandBind(InteractionHand.MAIN_HAND);
        cap.setHandBind(InteractionHand.MAIN_HAND, cap.getHandBind(InteractionHand.OFF_HAND));
        cap.setHandBind(InteractionHand.OFF_HAND, mbind);
        //tried really hard to make this work, but it just causes more problems.
        suppress = false;
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

    public static void sweep(LivingEntity e, Entity ignore, InteractionHand h, double reach) {
        ItemStack stack = e.getItemInHand(h);
        WeaponStats.SWEEPSTATE s = getSweepState(e);
        WeaponStats.SweepInfo info = WeaponStats.getSweepInfo(stack, s);
        //apply instantaneous damage multiplier
        SkillUtils.modifyAttribute(e, Attributes.ATTACK_DAMAGE, main, info.getDamageScale() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        sweep(e, ignore, h, info.getType(), reach, info.getBase(), info.getScaling());
        SkillUtils.removeAttribute(e, Attributes.ATTACK_DAMAGE, main);
    }

    public static void sweep(LivingEntity e, Entity ignore, InteractionHand h, WeaponStats.SWEEPTYPE type, double reach, double base, double scaling) {
        //no go cases
        if (!GeneralConfig.betterSweep) return;//a shame, but alas
        if (!CombatData.getCap(e).isCombatMode()) return;
        if (CombatData.getCap(e).getHandBind(h) > 0) return;//don't even try dude
        if (h == InteractionHand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(true);
        }
        if (!PermissionData.getCap(e).canSweep()) type = WeaponStats.SWEEPTYPE.NONE;
        double radius;

        SweepEvent sre = new SweepEvent(e, h, e.getMainHandItem(), type, base, scaling);
        MinecraftForge.EVENT_BUS.post(sre);
        base = sre.getBase();
        scaling = sre.getScaling();
        radius = sre.getFinalizedWidth();
        type = sre.getType();
        if (sre.isCanceled() || type == WeaponStats.SWEEPTYPE.NONE || radius == 0) {
            //no go, swap items back and stop
            if (h == InteractionHand.OFF_HAND) {
                swapHeldItems(e);
                CombatData.getCap(e).setOffhandAttack(false);
            }
            return;
        }
        if (e.getMainHandItem().getCapability(CombatManipulator.CAP).isPresent())
            radius = e.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().sweepArea(e, e.getMainHandItem());
        float charge = Math.max(CombatUtils.getCooledAttackStrength(e, InteractionHand.MAIN_HAND, 0.5f), CombatData.getCap(e).getCachedCooldown());
        boolean hit = false;
        isSweeping = ignore != null;
        Vec3 starting = ignore == null ? GeneralUtils.raytraceAnything(e.level(), e, reach).getLocation() : ignore.position();
        //grab everyone in "range"
        for (Entity target : e.level().getEntities(e, e.getBoundingBox().inflate(reach * 2))) {
            if (target == e) continue;
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
                    if (!target.getBoundingBox().inflate(radius).intersects(start, end)) continue;
                }
            }

            CombatUtils.setHandCooldown(e, InteractionHand.MAIN_HAND, charge, false);
            hit = true;
            if (e instanceof Player)
                ((Player) e).attack(target);
            else e.doHurtTarget(target);
            isSweeping = true;
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
            }
        }
        ParticleUtils.playSweepParticle(particle, e, starting, 0, radius, sre.getColor(), offset);
        e.level().playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, e.getSoundSource(), 1.0F, 1.0F);
        //}
        isSweeping = false;
        if (h == InteractionHand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(false);
        }
    }

    public static void initializePPE(ProjectileParryEvent ppe, float mult) {
        final EntityType<?> type = ppe.getProjectile().getType();
        ProjectileInfo pi = projectileMap.getOrDefault(type, DEFAULTRANGED);
        ppe.setReturnVec(pi.destroy | type.is(MobSpecs.DESTROY_ON_PARRY) ? null : ppe.getProjectile().getDeltaMovement().normalize().scale(-0.1));
        ppe.setPostureConsumption((float) pi.posture * mult);
        ppe.setTrigger(pi.trigger | type.is(MobSpecs.TRIGGER_ON_PARRY));
    }

    public static WeaponStats.SWEEPSTATE getSweepState(LivingEntity entity) {
        if (entity.isPassenger()) return WeaponStats.SWEEPSTATE.RIDING;
        if (entity.isCrouching()) return WeaponStats.SWEEPSTATE.SNEAKING;
        if (entity.isSwimming() || entity.isSprinting() || entity.isFallFlying() || MovementUtils.hasInvFrames(entity))
            return WeaponStats.SWEEPSTATE.SPRINTING;
        if ((!(entity instanceof Player p) || !p.getAbilities().flying) && !entity.onGround() && entity.fallDistance > 0 && !entity.onClimbable() && !entity.isInWater())
            return WeaponStats.SWEEPSTATE.FALLING;
        return WeaponStats.SWEEPSTATE.STANDING;
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
