package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.weaponry.CombatManipulator;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.ClientEvents;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateAttackPacket;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
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
    public static HashMap<ResourceLocation, Float> customPosture = new HashMap<>();
    public static HashMap<ResourceLocation, Tuple<Float, Float>> parryMap = new HashMap<>();
    public static HashMap<ResourceLocation, StealthData> stealthMap = new HashMap<>();
    public static final StealthData STEALTH=new StealthData(false, false, false, false, false);
    public static HashMap<Item, AttributeModifier[]> armorStats = new HashMap<>();
    public static boolean isSweeping = false;
    private static CombatInfo DEFAULT = new CombatInfo(1, 1, false, 0, 0, 1, 1);
    private static HashMap<Item, CombatInfo> combatList = new HashMap<>();
    private static ArrayList<Item> unarmed = new ArrayList<>();

    public static void updateLists(List<? extends String> interpretC, List<? extends String> interpretP, List<? extends String> interpretA, List<? extends String> interpretM, List<? extends String> interpretU, List<? extends String> interpretS) {
        DEFAULT = new CombatInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend, false, CombatConfig.shieldThreshold, CombatConfig.shieldCount, CombatConfig.distract, CombatConfig.unaware);
        combatList = new HashMap<>();
        customPosture = new HashMap<>();
        armorStats = new HashMap<>();
        parryMap = new HashMap<>();
        stealthMap = new HashMap<>();
        unarmed = new ArrayList<>();
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
            if (ForgeRegistries.ITEMS.containsKey(key)) {
                final Item item = ForgeRegistries.ITEMS.getValue(key);
                combatList.put(item, new CombatInfo(attack, defend, shield, pTime, pCount, distract, unaware));
            }
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
        for (String name : interpretU) {
            ResourceLocation key = null;
            try {
                key = new ResourceLocation(name);
            } catch (Exception e) {
                WarDance.LOGGER.warn(name + " is not a proper item name, it will not be registered.");
            }
            if (ForgeRegistries.ITEMS.containsKey(key)) {
                unarmed.add(ForgeRegistries.ITEMS.getValue(key));
            }
        }
        for (String s : interpretP) {
            try {
                String[] val = s.split(",");
                CombatUtils.customPosture.put(new ResourceLocation(val[0]), Float.parseFloat(val[1]));

            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted custom posture definition " + s + "!");
            }
        }
        for (String s : interpretM) {
            try {
                String[] val = s.split(",");
                CombatUtils.parryMap.put(new ResourceLocation(val[0]), new Tuple<>(Float.parseFloat(val[1]), Float.parseFloat(val[2])));

            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted mob parrying definition " + s + "!");
            }
        }
        for (String s : interpretS) {
            try {
                String[] val = s.split(",");
                CombatUtils.stealthMap.put(new ResourceLocation(val[0]), new StealthData(Boolean.parseBoolean(val[1]), Boolean.parseBoolean(val[2]), Boolean.parseBoolean(val[3]), Boolean.parseBoolean(val[4]), Boolean.parseBoolean(val[5])));

            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted mob stealth definition " + s + "!");
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
        if (!(e instanceof PlayerEntity) && h == Hand.MAIN_HAND) return 1;
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
        if (stack == null) return false;
        if (e != null && CasterData.getCap(e).isSkillActive(WarSkills.SHIELD_BASH.get()))
            return combatList.containsKey(stack.getItem());
        return combatList.containsKey(stack.getItem()) && !combatList.getOrDefault(stack.getItem(), DEFAULT).isShield;//stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    public static boolean isUnarmed(ItemStack is, LivingEntity e) {
        return is.isEmpty() || unarmed.contains(is.getItem());
    }

    public static boolean canParry(LivingEntity defender, Entity attacker, @Nonnull ItemStack i, float damage) {
        Hand h = defender.getHeldItemOffhand() == i ? Hand.OFF_HAND : Hand.MAIN_HAND;
        float rand = WarDance.rand.nextFloat();
        boolean recharge = getCooledAttackStrength(defender, h, 0.5f) > 0.9f && CombatData.getCap(defender).getHandBind(h) == 0;
        recharge &= (!(defender instanceof PlayerEntity) || ((PlayerEntity) defender).getCooldownTracker().getCooldown(defender.getHeldItemOffhand().getItem(), 0) == 0);
        if (defender.getHeldItemMainhand().getCapability(CombatManipulator.CAP).isPresent() && attacker instanceof LivingEntity) {
            return defender.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get().canBlock(defender, attacker, i, recharge, damage);
        }
        if (isShield(defender, i)) {
            boolean canShield = (defender instanceof PlayerEntity || rand < CombatConfig.mobParryChanceShield);
            boolean canParry = CombatData.getCap(defender).getShieldTime() == 0 || CombatData.getCap(defender).getShieldCount() > 0;
            return recharge & canParry & canShield;
        } else if (isWeapon(defender, i)) {
            boolean canWeapon = (defender instanceof PlayerEntity || rand < CombatConfig.mobParryChanceWeapon);
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

    public static float getPostureAtk(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, @Nullable Hand h, float amount, ItemStack stack) {
        float base = amount * (float) DEFAULT.attackPostureMultiplier;
        if (stack != null) {
            if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
                base = stack.getCapability(CombatManipulator.CAP).resolve().get().postureDealtBase(attacker, defender, stack, amount);
            } else if (combatList.containsKey(stack.getItem()))
                base = (float) combatList.get(stack.getItem()).attackPostureMultiplier;

        } else if (stack == null || stack.isEmpty()) {
            base *= CombatConfig.kenshiroScaler;
        }
        if (attacker == null || h == null) return base;
        return base * (attacker instanceof PlayerEntity ? Math.max(CombatData.getCap(attacker).getCachedCooldown(), ((PlayerEntity) attacker).getCooledAttackStrength(0.5f)) : CombatConfig.mobScaler);
    }

    public static float getPostureDef(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, ItemStack stack, float amount) {
        if (stack == null) return (float) DEFAULT.defensePostureMultiplier;
        if (defender != null && isShield(defender, stack) && CombatData.getCap(defender).getShieldTime() > 0 && CombatData.getCap(defender).getShieldCount() > 0) {
            return 0;
        }
        if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
            return stack.getCapability(CombatManipulator.CAP).resolve().get().postureMultiplierDefend(attacker, defender, stack, amount);
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
                //FIXME bht compat?
                if (!(e instanceof PlayerEntity)) return;
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
        off.getAttributeModifiers(EquipmentSlotType.OFFHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.removeModifier(mod);});
        });
        main.getAttributeModifiers(EquipmentSlotType.OFFHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.applyNonPersistentModifier(mod);});
        });
        off.getAttributeModifiers(EquipmentSlotType.MAINHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.applyNonPersistentModifier(mod);});
        });
        e.ticksSinceLastSwing = cap.getOffhandCooldown();
        cap.setOffhandCooldown(tssl);
        e.setSilent(silent);
    }

    public static void sweep(LivingEntity e, Entity ignore, Hand h, double reach) {
        if (!CombatConfig.betterSweep) return;
        if (h == Hand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(true);
        }
        int angle = CombatData.getCap(e).getForcedSweep() > 0 ? CombatData.getCap(e).getForcedSweep() : EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.SWEEPING, e) * CombatConfig.sweepAngle;
        if (e.getHeldItemMainhand().getCapability(CombatManipulator.CAP).isPresent())
            angle = e.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get().sweepArea(e, e.getHeldItemMainhand());
        float charge = Math.max(CombatUtils.getCooledAttackStrength(e, Hand.MAIN_HAND, 0.5f), CombatData.getCap(e).getCachedCooldown());
        boolean hit = false;
        isSweeping = ignore != null;
        for (Entity target : e.world.getEntitiesWithinAABBExcludingEntity(e, e.getBoundingBox().grow(reach + 3))) {//TEST: fixed range 3 attacks
            if (target == ignore) {
                if (angle > 0)
                    hit = true;
                continue;
            }
            if (!GeneralUtils.isFacingEntity(e, target, angle)) continue;
            if (!e.canEntityBeSeen(target)) continue;
            double modRange = reach > 3 ? (reach - 3) / 2d + 3 : reach;
            if (GeneralUtils.getDistSqCompensated(e, target) > modRange * modRange) continue;
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

    public static AWARENESS getAwareness(LivingEntity attacker, LivingEntity target) {
        if (!CombatConfig.stealthSystem || target instanceof PlayerEntity||CombatUtils.stealthMap.getOrDefault(target.getType().getRegistryName(), CombatUtils.STEALTH).isVigilant()) return AWARENESS.ALERT;
        if (target.getRevengeTarget() == null && (!(target instanceof MobEntity) || ((MobEntity) target).getAttackTarget() == null))
            return AWARENESS.UNAWARE;
        else if (target.getRevengeTarget() != attacker && (!(target instanceof MobEntity) || ((MobEntity) target).getAttackTarget() != attacker))
            return AWARENESS.DISTRACTED;
        else if (target.isPotionActive(WarEffects.DISTRACTION.get()))
            return AWARENESS.DISTRACTED;
        else return AWARENESS.ALERT;
    }

    public enum AWARENESS {
        UNAWARE,//cannot be parried, absorbed, shattered, or deflected
        DISTRACTED,//deals extra (posture) damage
        ALERT//normal damage and reduction
    }

    public static class StealthData {
        public boolean isDeaf() {
            return deaf;
        }

        public boolean isNightVision() {
            return nightvision;
        }

        public boolean isAllSeeing() {
            return illuminati;
        }

        public boolean isObservant() {
            return atheist;
        }

        public boolean isVigilant(){
            return vigil;
        }

        private boolean deaf, nightvision, illuminati, atheist, vigil;

        public StealthData(boolean isDeaf, boolean nightVision, boolean allSeeing, boolean observant, boolean vigilant) {
            deaf = isDeaf;
            nightvision = nightVision;
            illuminati = allSeeing;
            atheist = observant;
            vigil=vigilant;
        }
    }

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
}
