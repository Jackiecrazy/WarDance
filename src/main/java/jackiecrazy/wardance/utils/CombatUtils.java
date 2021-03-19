package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.api.ICombatManipulator;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.client.ClientEvents;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateAttackPacket;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CombatUtils {
    private static class CombatInfo {
        private final double attackPostureMultiplier, defensePostureMultiplier;
        private final double distractDamageBonus, unawareDamageBonus;
        private final int parryTime, parryCount;
        private final boolean isShield;

        private CombatInfo(double attack, double defend, boolean shield, int pTime, int pCount, double distract, double unaware) {
            attackPostureMultiplier = attack;
            defensePostureMultiplier = defend;
            isShield = shield;
            parryCount = pCount;
            parryTime = pTime;
            distractDamageBonus = distract;
            unawareDamageBonus = unaware;
        }
    }

    private static CombatInfo DEFAULT = new CombatInfo(1, 1, false, 0, 0, 1, 1);
    private static HashMap<Item, CombatInfo> combatList = new HashMap<>();
    public static HashMap<String, Float> customPosture = new HashMap<>();
    public static HashMap<Item, AttributeModifier[]> armorStats = new HashMap<>();

    public static void updateLists(List<? extends String> interpretC, List<? extends String> interpretP, List<? extends String> interpretA) {
        DEFAULT = new CombatInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend, false, CombatConfig.shieldThreshold, CombatConfig.shieldCount, CombatConfig.distract, CombatConfig.unaware);
        combatList = new HashMap<>();
        customPosture = new HashMap<>();
        armorStats = new HashMap<>();
        for (String s : interpretC) {
            String[] val = s.split(",");
            String name = val[0];
            double attack = CombatConfig.defaultMultiplierPostureAttack;
            double defend = CombatConfig.defaultMultiplierPostureDefend;
            boolean shield = false;
            if (val.length > 1)
                try {
                    attack = Float.parseFloat(val[1].trim());
                } catch (NumberFormatException ignored) {
                    WarDance.LOGGER.warn("attack data for config entry " + s + " is not properly formatted, replacing with default values.");
                }
            if (val.length > 2)
                try {
                    defend = Float.parseFloat(val[2].trim());
                } catch (NumberFormatException ignored) {
                    WarDance.LOGGER.warn("defense data for config entry " + s + " is not properly formatted, replacing with default values.");
                }
            if (val.length > 3)
                shield = Boolean.parseBoolean(val[3].trim());
            int pTime = CombatConfig.shieldThreshold, pCount = CombatConfig.shieldCount;
            double distract = CombatConfig.distract, unaware = CombatConfig.unaware;
            if (shield) {
                try {
                    pTime = Integer.parseInt(val[4].trim());
                    pCount = Integer.parseInt(val[5].trim());
                } catch (Exception e) {
                    WarDance.LOGGER.warn("additional data for shield config entry " + s + " is not properly formatted, replacing with default values.");
                }
            } else {
                try {
                    distract = Double.parseDouble(val[4].trim());
                    unaware = Double.parseDouble(val[5].trim());
                } catch (Exception e) {
                    WarDance.LOGGER.warn("additional data for weapon config entry " + s + " is not properly formatted, replacing with default values.");
                }
            }
            ResourceLocation key = null;
            try {
                key = new ResourceLocation(name);
            } catch (Exception e) {
                WarDance.LOGGER.warn(name + " is not a proper item name, it will not be registered.");
            }
            if (ForgeRegistries.ITEMS.containsKey(key))
                combatList.put(ForgeRegistries.ITEMS.getValue(key), new CombatInfo(attack, defend, shield, pTime, pCount, distract, unaware));
            //System.out.print("\"" + name + ", " + formatter.format(attack + 1.5) + ", " + formatter.format(defend) + ", " + shield + (shield ? ", " + pTime + ", " + pCount : "") + "\", ");
        }
        for (String s : interpretA) {
            String[] val = s.split(",");
            String name = val[0];
            double absorption = 0, deflection = 0, shatter = 0;
            try {
                absorption = Double.parseDouble(val[1]);
                deflection = Double.parseDouble(val[2]);
                shatter = Double.parseDouble(val[3]);
            } catch (Exception ignored) {
                WarDance.LOGGER.warn("armor data for config entry " + s + " is not properly formatted, filling in zeros.");
            }
            ResourceLocation key = null;
            try {
                key = new ResourceLocation(name);
            } catch (Exception e) {
                WarDance.LOGGER.warn(name + " is not a proper item name, it will not be registered.");
            }
            if (ForgeRegistries.ITEMS.containsKey(key) && (ForgeRegistries.ITEMS.getValue(key)) instanceof ArmorItem) {
                UUID touse = WarAttributes.MODIFIERS[((ArmorItem) (ForgeRegistries.ITEMS.getValue(key))).getEquipmentSlot().getIndex()];
                armorStats.put(ForgeRegistries.ITEMS.getValue(key), new AttributeModifier[]{
                        new AttributeModifier(touse, "war dance modifier", absorption, AttributeModifier.Operation.ADDITION),
                        new AttributeModifier(touse, "war dance modifier", deflection, AttributeModifier.Operation.ADDITION),
                        new AttributeModifier(touse, "war dance modifier", shatter, AttributeModifier.Operation.ADDITION)
                });
            }
        }
        for (String s : interpretP) {
            try {
                String[] val = s.split(",");
                CombatUtils.customPosture.put(val[0], Float.parseFloat(val[1]));

            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted custom posture definition " + s + "!");
            }
        }
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> {
            return new DistExecutor.SafeRunnable() {
                @Override
                public void run() {
                    ClientEvents.updateList(interpretP);
                }
            };
        });
    }

    public static float getCooledAttackStrength(LivingEntity e, Hand h, float adjustTicks) {
        //if (h == Hand.OFF_HAND && adjustTicks == 1) System.out.println(getCooldownPeriod(e, h));
        return MathHelper.clamp(((float) (h == Hand.MAIN_HAND ? e.ticksSinceLastSwing : CombatData.getCap(e).getOffhandCooldown()) + adjustTicks) / getCooldownPeriod(e, h), 0.0F, 1.0F);
    }

    public static float getCooldownPeriod(LivingEntity e, Hand h) {
        return (float) (1.0D / GeneralUtils.getAttributeValueHandSensitive(e, Attributes.ATTACK_SPEED, h) * 20.0D);
    }

    public static boolean isShield(LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        return combatList.containsKey(stack.getItem()) && combatList.getOrDefault(stack.getItem(), DEFAULT).isShield;//stack.isShield(e);
    }

    public static boolean isWeapon(@Nullable LivingEntity e, ItemStack stack) {
        return combatList.containsKey(stack.getItem()) && !combatList.getOrDefault(stack.getItem(), DEFAULT).isShield;//stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    @Nullable
    public static ItemStack getDefendingItemStack(LivingEntity e, boolean shieldsOnly) {
        ItemStack ret = null;
        float posMod = 1337;
        float rand = WarDance.rand.nextFloat();
        boolean canShield = (e instanceof PlayerEntity || rand < CombatConfig.mobParryChanceShield);
        boolean canWeapon = (e instanceof PlayerEntity || rand < CombatConfig.mobParryChanceWeapon);
        boolean mainRec = getCooledAttackStrength(e, Hand.MAIN_HAND, 0.5f) > 0.9f && CombatData.getCap(e).getHandBind(Hand.MAIN_HAND) == 0;
        boolean offRec = getCooledAttackStrength(e, Hand.OFF_HAND, 0.5f) > 0.9f && CombatData.getCap(e).getHandBind(Hand.OFF_HAND) == 0;
        if (offRec && canShield && isShield(e, e.getHeldItemOffhand()) && (!(e instanceof PlayerEntity) || ((PlayerEntity) e).getCooldownTracker().getCooldown(e.getHeldItemOffhand().getItem(), 0) == 0)) {
            ret = e.getHeldItemOffhand();
            posMod = getPostureDef(e, e.getHeldItemOffhand());
        }
        if (mainRec && canShield && isShield(e, e.getHeldItemMainhand()) && (!(e instanceof PlayerEntity) || ((PlayerEntity) e).getCooldownTracker().getCooldown(e.getHeldItemMainhand().getItem(), 0) == 0)) {
            if (ret == null || getPostureDef(e, e.getHeldItemMainhand()) < posMod) {
                posMod = getPostureDef(e, e.getHeldItemMainhand());
                ret = e.getHeldItemMainhand();
            }
        }
        if (shieldsOnly) return ret;
        if (offRec && canWeapon && isWeapon(e, e.getHeldItemOffhand())) {
            if (ret == null || getPostureDef(e, e.getHeldItemMainhand()) < posMod) {
                posMod = getPostureDef(e, e.getHeldItemOffhand());
                ret = e.getHeldItemMainhand();
            }
        }
        if (mainRec && canWeapon && isWeapon(e, e.getHeldItemMainhand())) {
            if (ret == null || getPostureDef(e, e.getHeldItemMainhand()) < posMod) {
                ret = e.getHeldItemMainhand();
            }
        }
        return ret;
    }

    public static boolean canParry(LivingEntity e, @Nonnull ItemStack i) {
        Hand h = e.getHeldItemOffhand() == i ? Hand.OFF_HAND : Hand.MAIN_HAND;
        float rand = WarDance.rand.nextFloat();
        boolean recharge = getCooledAttackStrength(e, h, 0.5f) > 0.9f && CombatData.getCap(e).getHandBind(h) == 0;
        recharge &= (!(e instanceof PlayerEntity) || ((PlayerEntity) e).getCooldownTracker().getCooldown(e.getHeldItemOffhand().getItem(), 0) == 0);
        if (isShield(e, i)) {
            boolean canShield = (e instanceof PlayerEntity || rand < CombatConfig.mobParryChanceShield);
            boolean canParry = CombatData.getCap(e).getShieldTime() == 0 || CombatData.getCap(e).getShieldCount() != 0;
            return recharge & canParry & canShield;
        } else if (isWeapon(e, i)) {
            boolean canWeapon = (e instanceof PlayerEntity || rand < CombatConfig.mobParryChanceWeapon);
            return recharge & canWeapon;
        } else return false;
    }

    @Nullable
    public static ItemStack getAttackingItemStack(DamageSource ds) {
        if (ds instanceof CombatDamageSource)
            return ((CombatDamageSource) ds).getDamageDealer();
        else if (ds.getTrueSource() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) ds.getTrueSource();
            return e.getHeldItemMainhand();//CombatData.getCap(e).isOffhandAttack() ? e.getHeldItemOffhand() : e.getHeldItemMainhand();
        }
        return null;
    }

    /**
     * first is threshold, second is count
     */
    public static Tuple<Integer, Integer> getShieldStats(ItemStack stack) {
        if (stack != null && combatList.containsKey(stack.getItem())) {
            return new Tuple<>(combatList.get(stack.getItem()).parryTime, combatList.get(stack.getItem()).parryCount);
        }
        return new Tuple<>(CombatConfig.shieldThreshold, CombatConfig.shieldCount);
    }

    public static float getPostureAtk(@Nullable LivingEntity e, @Nullable Hand h, float amount, ItemStack stack) {
        float base = amount * (float) DEFAULT.attackPostureMultiplier;
        if (stack != null && combatList.containsKey(stack.getItem())) {
            base = (float) combatList.get(stack.getItem()).attackPostureMultiplier;
        } else if (stack == null || stack.isEmpty()) {
            base *= CombatConfig.kenshiroScaler;
        }
        if (e == null || h == null) return base;
        return base * (e instanceof PlayerEntity ? CombatData.getCap(e).getCachedCooldown() : CombatConfig.mobScaler);
    }

    public static float getPostureDef(@Nullable LivingEntity e, ItemStack stack) {
        if (stack == null) return (float) DEFAULT.defensePostureMultiplier;
        if (e != null && isShield(e, stack) && CombatData.getCap(e).getShieldTime() > 0 && CombatData.getCap(e).getShieldCount() > 0) {
            return 0;
        }
        if (combatList.containsKey(stack.getItem())) {
            return (float) combatList.get(stack.getItem()).defensePostureMultiplier;
        }
        return (float) DEFAULT.defensePostureMultiplier;
    }

    public static boolean isMeleeAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.canProcAutoEffects();
        }
        return s.getTrueSource() == s.getImmediateSource() && !s.isExplosion() && !s.isFireDamage() && !s.isMagicDamage() && !s.isUnblockable() && !s.isProjectile();
    }

    public static boolean isPhysicalAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.getDamageTyping() == CombatDamageSource.TYPE.PHYSICAL;
        }
        return !s.isExplosion() && !s.isFireDamage() && !s.isMagicDamage() && !s.isUnblockable();
    }

    /**
     * knocks the target back, with regards to the attacker's relative angle to the target, and adding y knockback
     */
    public static void knockBack(Entity to, Entity from, float strength, boolean considerRelativeAngle, boolean bypassAllChecks) {
        Vector3d distVec = to.getPositionVec().add(0, to.getHeight() / 2, 0).subtractReverse(from.getPositionVec().add(0, from.getHeight() / 2, 0)).mul(1, 0.5, 1).normalize();
        if (to instanceof LivingEntity && !bypassAllChecks) {
            if (considerRelativeAngle)
                knockBack((LivingEntity) to, from, strength, distVec.x, distVec.y, distVec.z, false);
            else
                knockBack(((LivingEntity) to), from, (float) strength * 0.5F, (double) MathHelper.sin(from.rotationYaw * 0.017453292F), 0, (double) (-MathHelper.cos(from.rotationYaw * 0.017453292F)), false);
        } else {
            //eh
            if (considerRelativeAngle) {
                to.setVelocity(distVec.x * -strength, to.collidedVertically ? 0.1 : distVec.y * -strength, distVec.z * -strength);
            } else {
                to.addVelocity(-MathHelper.sin(-from.rotationYaw * 0.017453292F - (float) Math.PI) * 0.5, 0.1, -MathHelper.cos(-from.rotationYaw * 0.017453292F - (float) Math.PI) * 0.5);
            }
            to.velocityChanged = true;
        }
    }

    /**
     * knockback in LivingEntity except it makes sense and the resist is factored into the event
     */
    public static void knockBack(LivingEntity to, Entity from, float strength, double xRatio, double yRatio, double zRatio, boolean bypassEventCheck) {
        if (!bypassEventCheck) {
            net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(to, strength, xRatio, zRatio);
            if (event.isCanceled()) return;
            strength = event.getStrength();
            xRatio = event.getRatioX();
            zRatio = event.getRatioZ();
        } else {
            strength *= (float) (1 - GeneralUtils.getAttributeValueSafe(to, Attributes.KNOCKBACK_RESISTANCE));
        }
        if (strength != 0f) {
            Vector3d vec = to.getMotion();
            double motionX = vec.x, motionY = vec.y, motionZ = vec.z;
            to.isAirBorne = true;
            float pythagora = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);
            if (to.isOnGround()) {
                motionY /= 2.0D;
                motionY += strength;

                if (motionY > 0.4000000059604645D) {
                    motionY = 0.4000000059604645D;
                }
            } else if (yRatio != 0) {
                pythagora = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio + yRatio * yRatio);
                motionY /= 2.0D;
                motionY -= yRatio / (double) pythagora * (double) strength;
            }
            motionX /= 2.0D;
            motionZ /= 2.0D;
            motionX -= xRatio / (double) pythagora * (double) strength;
            motionZ -= zRatio / (double) pythagora * (double) strength;
            to.setMotion(motionX, motionY, motionZ);
            to.velocityChanged = true;
        }
    }

    public static void setHandCooldown(LivingEntity e, Hand h, float percent, boolean sync) {
        //special case for quick maths
        int real = percent == 0 ? 0 : (int) (percent * getCooldownPeriod(e, h));
        switch (h) {
            case MAIN_HAND:
                e.ticksSinceLastSwing = real;
                if (!(e instanceof FakePlayer) && e instanceof ServerPlayerEntity && sync)
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e), new UpdateAttackPacket(e.getEntityId(), real));
                break;
            case OFF_HAND:
                CombatData.getCap(e).setOffhandCooldown(real);
                break;
        }
    }

    public static double getDamageMultiplier(AWARENESS a, ItemStack is) {
        if (!CombatConfig.stealthSystem || is == null) return 1;
        CombatInfo ci = combatList.getOrDefault(is.getItem(), DEFAULT);
        switch (a) {
            case DISTRACTED:
                return ci.distractDamageBonus;
            case UNAWARE:
                return ci.unawareDamageBonus;
            default:
                return 1;

        }
    }

    private static ArrayList<Attribute> attributes = new ArrayList<>();

    public static void swapHeldItems(LivingEntity e) {
        //attributes = new ArrayList<>();
        ItemStack main = e.getHeldItemMainhand(), off = e.getHeldItemOffhand();
        int tssl = e.ticksSinceLastSwing;
        boolean silent = e.isSilent();
        e.setSilent(true);
        ICombatCapability cap = CombatData.getCap(e);
        e.setHeldItem(Hand.MAIN_HAND, e.getHeldItemOffhand());
        e.setHeldItem(Hand.OFF_HAND, main);
//        attributes.addAll(main.getAttributeModifiers(EquipmentSlotType.MAINHAND).keys());
//        attributes.addAll(main.getAttributeModifiers(EquipmentSlotType.OFFHAND).keys());
//        attributes.addAll(off.getAttributeModifiers(EquipmentSlotType.MAINHAND).keys());
//        attributes.addAll(off.getAttributeModifiers(EquipmentSlotType.OFFHAND).keys());
//        attributes.forEach((att)->{Optional.ofNullable(e.getAttribute(att)).ifPresent(ModifiableAttributeInstance::compute);});
        main.getAttributeModifiers(EquipmentSlotType.MAINHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.removeModifier(mod);});
        });
        main.getAttributeModifiers(EquipmentSlotType.OFFHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.applyNonPersistentModifier(mod);});
        });
        off.getAttributeModifiers(EquipmentSlotType.MAINHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.applyNonPersistentModifier(mod);});
        });
        off.getAttributeModifiers(EquipmentSlotType.OFFHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.removeModifier(mod);});
        });
        e.ticksSinceLastSwing = cap.getOffhandCooldown();
        cap.setOffhandCooldown(tssl);
        e.setSilent(silent);
    }

    public static boolean isSweeping = false;

    public static void sweep(LivingEntity e, Entity ignore, Hand h, double reach) {
        if (!CombatConfig.betterSweep) return;
        if (h == Hand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(true);
        }
        int angle = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.SWEEPING, e) * CombatConfig.sweepAngle;
        if (e.getHeldItemMainhand().getItem() instanceof ICombatManipulator)
            angle = ((ICombatManipulator) e.getHeldItemMainhand().getItem()).sweepArea(e, e.getHeldItemMainhand());
        float charge = Math.max(CombatUtils.getCooledAttackStrength(e, Hand.MAIN_HAND, 0.5f), CombatData.getCap(e).getCachedCooldown());
        boolean hit = false;
        isSweeping = ignore != null;
        for (Entity target : e.world.getEntitiesWithinAABBExcludingEntity(e, e.getBoundingBox().grow(reach))) {
            if (target == ignore) {
                hit = true;
                continue;
            }
            if (!GeneralUtils.isFacingEntity(e, target, angle)) continue;
            if (!e.canEntityBeSeen(target)) continue;
            CombatUtils.setHandCooldown(e, Hand.MAIN_HAND, charge, false);
            hit = true;
            if (e instanceof PlayerEntity)
                ((PlayerEntity) e).attackTargetEntityWithCurrentItem(target);
            else e.attackEntityAsMob(target);
            isSweeping = true;
        }
        if (e instanceof PlayerEntity && hit) {
            ((PlayerEntity) e).spawnSweepParticles();
            e.world.playSound(null, e.getPosX(), e.getPosY(), e.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, e.getSoundCategory(), 1.0F, 1.0F);
        }
        isSweeping = false;
        if (h == Hand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(false);
        }
    }

    public enum AWARENESS {
        UNAWARE,//deals 1.5x (posture) damage, cannot be parried, absorbed, shattered, or deflected
        DISTRACTED,//deals 1.5x (posture) damage
        ALERT//normal damage and reduction
    }

    public static AWARENESS getAwareness(LivingEntity attacker, LivingEntity target) {
        if (!CombatConfig.stealthSystem || target instanceof PlayerEntity) return AWARENESS.ALERT;
        if (target.getRevengeTarget() == null && (!(target instanceof MobEntity) || ((MobEntity) target).getAttackTarget() == null))
            return AWARENESS.UNAWARE;
        else if (target.getRevengeTarget() != attacker && (!(target instanceof MobEntity) || ((MobEntity) target).getAttackTarget() != attacker))
            return AWARENESS.DISTRACTED;
        else return AWARENESS.ALERT;
    }
}
