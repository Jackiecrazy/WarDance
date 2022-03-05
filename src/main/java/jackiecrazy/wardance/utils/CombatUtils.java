package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.weaponry.CombatManipulator;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateAttackPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CombatUtils {
    public static final UUID off = UUID.fromString("8c8028c8-da69-49a2-99cd-f92d7ad22534");
    private static final UUID main = UUID.fromString("8c8028c8-da67-49a2-99cd-f92d7ad22534");
    public static HashMap<ResourceLocation, Float> customPosture = new HashMap<>();
    public static HashMap<ResourceLocation, MobInfo> parryMap = new HashMap<>();
    public static HashMap<Item, AttributeModifier[]> armorStats = new HashMap<>();
    public static HashMap<Item, AttributeModifier[]> shieldStat = new HashMap<>();
    public static boolean isSweeping = false;
    public static boolean suppress = false;
    private static MeleeInfo DEFAULTMELEE = new MeleeInfo(1, 1, false, 0, 0, 1, 1);
    private static ProjectileInfo DEFAULTRANGED = new ProjectileInfo(0.1, 1, false, false);
    private static HashMap<Item, MeleeInfo> combatList = new HashMap<>();
    private static HashMap<EntityType, ProjectileInfo> projectileMap = new HashMap<>();
    private static ArrayList<Item> unarmed = new ArrayList<>();

    public static void updateItems(List<? extends String> interpretC, List<? extends String> interpretA, List<? extends String> interpretU) {
        DEFAULTMELEE = new MeleeInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend, false, CombatConfig.shieldCooldown, CombatConfig.barrierSize, StealthConfig.distract, StealthConfig.unaware);
        combatList = new HashMap<>();
        armorStats = new HashMap<>();
        shieldStat = new HashMap<>();
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
            int pTime = CombatConfig.shieldCooldown;
            float pCount = CombatConfig.barrierSize;
            double distract = StealthConfig.distract, unaware = StealthConfig.unaware;
            if (shield) {
                try {
                    pTime = Integer.parseInt(val[4].trim());
                    pCount = Float.parseFloat(val[5].trim());
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
            //System.out.print("\""+key+"\",\n");
            if (ForgeRegistries.ITEMS.containsKey(key)) {
                final Item item = ForgeRegistries.ITEMS.getValue(key);
                combatList.put(item, new MeleeInfo(attack, defend, shield, pTime, pCount, distract, unaware));
                if (shield) {
                    final AttributeModifier downtimeM = new AttributeModifier(main, "extra downtime", pTime, AttributeModifier.Operation.ADDITION);
                    final AttributeModifier barrierM = new AttributeModifier(main, "barrier bonus", pCount, AttributeModifier.Operation.ADDITION);
                    final AttributeModifier downtimeO = new AttributeModifier(off, "extra downtime", pTime, AttributeModifier.Operation.ADDITION);
                    final AttributeModifier barrierO = new AttributeModifier(off, "barrier bonus", pCount, AttributeModifier.Operation.ADDITION);
                    shieldStat.put(item, new AttributeModifier[]{downtimeM, barrierM, downtimeO, barrierO});
                }
            }
            //System.out.print("\"" + name+ "\", ");
        }
        for (String s : interpretA) {
            String[] val = s.split(",");
            String name = val[0];
            double absorption = 0, deflection = 0, shatter = 0, stealth = 0;
            try {
                absorption = Double.parseDouble(val[1]);
                deflection = Double.parseDouble(val[2]);
                shatter = Double.parseDouble(val[3]);
                stealth = Double.parseDouble(val[4]);
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
                final Item armor = ForgeRegistries.ITEMS.getValue(key);
                UUID touse = WarAttributes.MODIFIERS[((ArmorItem) armor).getEquipmentSlot().getIndex()];
                armorStats.put(armor, new AttributeModifier[]{
                        new AttributeModifier(touse, "war dance modifier", absorption, AttributeModifier.Operation.ADDITION),
                        new AttributeModifier(touse, "war dance modifier", deflection, AttributeModifier.Operation.ADDITION),
                        new AttributeModifier(touse, "war dance modifier", shatter, AttributeModifier.Operation.ADDITION),
                        new AttributeModifier(touse, "war dance modifier", stealth, AttributeModifier.Operation.ADDITION)
                });
            }
            //System.out.print("\"" + name+ "\", ");
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
    }

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
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(key);
                if (type != null)
                    projectileMap.put(type, new ProjectileInfo(posture, count, destroy, trigger));
            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted projectile parry definition " + s + "!");
            }
    }

    public static void updateMobParrying(List<? extends String> interpretM) {
        parryMap.clear();
        for (String s : interpretM) {
            try {
                String[] val = s.split(",");
                if (val.length < 4)
                    CombatUtils.parryMap.put(new ResourceLocation(val[0]), new MobInfo(Float.parseFloat(val[1]), Float.parseFloat(val[2]), false, false));
                else
                    CombatUtils.parryMap.put(new ResourceLocation(val[0]), new MobInfo(Float.parseFloat(val[1]), Float.parseFloat(val[2]), val[3].contains("o"), val[3].contains("s")));

            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted mob parrying definition " + s + "!");
            }
        }
    }

    public static void updateMobPosture(List<? extends String> interpretP) {
        customPosture.clear();
        for (String s : interpretP) {
            try {
                String[] val = s.split(",");
                CombatUtils.customPosture.put(new ResourceLocation(val[0]), Float.parseFloat(val[1]));

            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted custom posture definition " + s + "!");
            }
        }
    }

    public static void attack(LivingEntity from, Entity to, boolean offhand) {
        if (offhand) {
            swapHeldItems(from);
            CombatData.getCap(from).setOffhandAttack(true);
        }
        if (from.ticksSinceLastSwing > 0) {
            int temp = from.ticksSinceLastSwing;
            if (from instanceof PlayerEntity) ((PlayerEntity) from).attackTargetEntityWithCurrentItem(to);
            else from.attackEntityAsMob(to);
            from.ticksSinceLastSwing = temp;
        }
        if (offhand) {
            CombatUtils.swapHeldItems(from);
            CombatData.getCap(from).setOffhandAttack(false);
        }
    }

    public static float getCooledAttackStrength(LivingEntity e, Hand h, float adjustTicks) {
        if (!(e instanceof PlayerEntity) && h == Hand.MAIN_HAND) return 1;
        //if (h == Hand.OFF_HAND && adjustTicks == 1) System.out.println(getCooldownPeriod(e, h));
        return MathHelper.clamp(((float) (h == Hand.MAIN_HAND ? e.ticksSinceLastSwing : CombatData.getCap(e).getOffhandCooldown()) + adjustTicks) / getCooldownPeriod(e, h), 0.0F, 1.0F);
    }

    public static int getCooldownPeriod(LivingEntity e, Hand h) {
        return (int) (1.0D / GeneralUtils.getAttributeValueHandSensitive(e, Attributes.ATTACK_SPEED, h) * 20.0D);
    }

    public static boolean isShield(LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        return combatList.containsKey(stack.getItem()) && (combatList.getOrDefault(stack.getItem(), DEFAULTMELEE).isShield);//stack.isShield(e);
    }

    public static boolean isWeapon(@Nullable LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        if (e != null && CasterData.getCap(e).getCategoryState(SkillCategories.shield_bash) == Skill.STATE.HOLSTERED)
            return combatList.containsKey(stack.getItem());
        return combatList.containsKey(stack.getItem()) && !combatList.getOrDefault(stack.getItem(), DEFAULTMELEE).isShield;//stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    public static boolean isUnarmed(ItemStack is, LivingEntity e) {
        return is.isEmpty() || unarmed.contains(is.getItem());
    }

    public static boolean canParry(LivingEntity defender, Entity attacker, @Nonnull ItemStack i, float postureDamage) {
        Hand h = defender.getHeldItemOffhand() == i ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (postureDamage < 0) return false;
        if (attacker instanceof LivingEntity && getPostureDef((LivingEntity) attacker, defender, i, postureDamage) < 0)
            return false;
        if (defender instanceof PlayerEntity && ((PlayerEntity) defender).getCooldownTracker().hasCooldown(i.getItem()))
            return false;
        if (CombatData.getCap(defender).getHandBind(h) > 0)
            return false;
        float rand = WarDance.rand.nextFloat();
        boolean recharge = getCooledAttackStrength(defender, h, 0.5f) > 0.9f && CombatData.getCap(defender).getHandBind(h) == 0;
        recharge &= (!(defender instanceof PlayerEntity) || ((PlayerEntity) defender).getCooldownTracker().getCooldown(defender.getHeldItem(h).getItem(), 0) == 0);
        if (i.getCapability(CombatManipulator.CAP).isPresent() && attacker instanceof LivingEntity) {
            return i.getCapability(CombatManipulator.CAP).resolve().get().canBlock(defender, attacker, i, recharge, postureDamage);
        }
        if (isShield(defender, i)) {
            boolean canShield = (defender instanceof PlayerEntity || rand < CombatConfig.mobParryChanceShield + CombatData.getCap(defender).getHandReel(h));
            boolean canParry = true;//CombatData.getCap(defender).getBarrierCooldown() == 0 || CombatData.getCap(defender).getBarrier() > 0;
            return recharge & canParry & canShield;
        } else if (isWeapon(defender, i)) {
            boolean canWeapon = (defender instanceof PlayerEntity || rand < CombatConfig.mobParryChanceWeapon + CombatData.getCap(defender).getHandReel(h));
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
     *
     * @return
     */
    public static Tuple<Integer, Float> getShieldStats(ItemStack stack) {
        if (stack != null && combatList.containsKey(stack.getItem())) {
            return new Tuple<>(combatList.get(stack.getItem()).parryTime, combatList.get(stack.getItem()).parryCount);
        }
        return new Tuple<>(CombatConfig.shieldCooldown, CombatConfig.barrierSize);
    }

    public static float getPostureAtk(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, @Nullable Hand h, float amount, ItemStack stack) {
        float base = amount * (float) DEFAULTMELEE.attackPostureMultiplier;
        //Spartan Shields compat, doesn't seem to work.
        if (attacker != null && attacker.isActiveItemStackBlocking()) {
            h = attacker.getActiveHand();
            stack = attacker.getHeldItem(h);
        }
        float scaler = CombatConfig.mobScaler;
        if (stack != null && !stack.isEmpty()) {
            scaler = 1;
            if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
                base = stack.getCapability(CombatManipulator.CAP).resolve().get().postureDealtBase(attacker, defender, stack, amount);
            } else if (combatList.containsKey(stack.getItem()))
                base = (float) combatList.get(stack.getItem()).attackPostureMultiplier;

        } else {
            if (!(attacker instanceof PlayerEntity))
                base = CombatData.getCap(attacker).getMaxPosture() * CombatConfig.defaultMultiplierPostureMob;
        }
        if (attacker == null || h == null) return base;
        final float fin = attacker instanceof PlayerEntity ? Math.max(CombatData.getCap(attacker).getCachedCooldown(), ((PlayerEntity) attacker).getCooledAttackStrength(0.5f)) : scaler;
        return base * fin;
    }

    public static float getPostureDef(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, ItemStack stack, float amount) {
        if (stack == null) return (float) DEFAULTMELEE.defensePostureMultiplier;
//        if (defender != null && isShield(defender, stack) && CombatData.getCap(defender).getBarrierCooldown() > 0 && CombatData.getCap(defender).getBarrier() > 0) {
//            return 0;
//        }
        if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
            return stack.getCapability(CombatManipulator.CAP).resolve().get().postureMultiplierDefend(attacker, defender, stack, amount);
        }
        if (combatList.containsKey(stack.getItem())) {
            return (float) combatList.get(stack.getItem()).defensePostureMultiplier;
        }
        return (float) DEFAULTMELEE.defensePostureMultiplier;
    }

    public static boolean isMeleeAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            return ((CombatDamageSource) s).canProcAutoEffects();
        }
        return s.getTrueSource() == s.getImmediateSource() && !s.isExplosion() && !s.isFireDamage() && !s.isMagicDamage() && !s.isUnblockable() && !s.isProjectile();
    }

    public static float getAttackMight(LivingEntity seme, LivingEntity uke) {
        ICombatCapability semeCap = CombatData.getCap(seme);
        final float magicScale = 1.722f;
        final float magicNumber = 781.25f;//magic numbers scale the modified formula to 0.2 per sword hit
        final float cooldownSq = semeCap.getCachedCooldown() * semeCap.getCachedCooldown();
        final double period = 20.0D / (seme.getAttribute(Attributes.ATTACK_SPEED).getValue() + 0.5d);//+0.5 makes sure heavies don't scale forever, light ones are still puny
        float might = cooldownSq * cooldownSq * magicScale * (float) period * (float) period / magicNumber;
        might *= (1f + (semeCap.getRank() / 20f));//combo bonus
        float weakness = 1;
        if (seme.isPotionActive(Effects.WEAKNESS))
            for (int foo = 0; foo < seme.getActivePotionEffect(Effects.WEAKNESS).getAmplifier() + 1; foo++) {
                weakness *= GeneralConfig.weakness;
            }
        might *= weakness;//weakness malus
        AttackMightEvent ame = new AttackMightEvent(seme, uke, might);
        MinecraftForge.EVENT_BUS.post(ame);
        return ame.getQuantity();
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
        }
        strength *= (float) Math.max(0, 1 - GeneralUtils.getAttributeValueSafe(to, Attributes.KNOCKBACK_RESISTANCE));
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

    public static void setHandCooldownDirect(LivingEntity e, Hand h, int amount, boolean sync) {
        switch (h) {
            case MAIN_HAND:
                if (!(e instanceof PlayerEntity)) return;
                e.ticksSinceLastSwing = amount;
                if (!(e instanceof FakePlayer) && e instanceof ServerPlayerEntity && sync)
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e), new UpdateAttackPacket(e.getEntityId(), amount));
                break;
            case OFF_HAND:
                CombatData.getCap(e).setOffhandCooldown(amount);
                break;
        }
    }

    public static double getDamageMultiplier(StealthUtils.Awareness a, ItemStack is) {
        if (!StealthConfig.stealthSystem || is == null) return 1;
        MeleeInfo ci = combatList.getOrDefault(is.getItem(), DEFAULTMELEE);
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
        suppress = true;
        ICombatCapability cap = CombatData.getCap(e);
        e.setHeldItem(Hand.MAIN_HAND, e.getHeldItemOffhand());
        e.setHeldItem(Hand.OFF_HAND, main);
        suppress = false;
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
    }

    public static void sweep(LivingEntity e, Entity ignore, Hand h, double reach) {
        if (!GeneralConfig.betterSweep) return;
        if (CombatData.getCap(e).getForcedSweep() == 0) {
            CombatData.getCap(e).setForcedSweep(-1);
            return;
        }
        if (h == Hand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(true);
        }
        int angle = CombatData.getCap(e).getForcedSweep() > 0 ? CombatData.getCap(e).getForcedSweep() : EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.SWEEPING, e) * GeneralConfig.sweepAngle;
        if (e.getHeldItemMainhand().getCapability(CombatManipulator.CAP).isPresent())
            angle = e.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get().sweepArea(e, e.getHeldItemMainhand());
        float charge = Math.max(CombatUtils.getCooledAttackStrength(e, Hand.MAIN_HAND, 0.5f), CombatData.getCap(e).getCachedCooldown());
        boolean hit = false;
        isSweeping = ignore != null;
        double modRange = Math.min(GeneralConfig.maxRange, GeneralConfig.baseRange + (reach - 3) * GeneralConfig.rangeMult);
        for (Entity target : e.world.getEntitiesWithinAABBExcludingEntity(e, e.getBoundingBox().grow(modRange + 3))) {
            if (target == ignore) {
                if (angle > 0)
                    hit = true;
                continue;
            }
            if (!GeneralUtils.isFacingEntity(e, target, angle)) continue;
            if (!e.canEntityBeSeen(target)) continue;
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
        CombatData.getCap(e).setForcedSweep(-1);
    }

    public static boolean isCrit(CriticalHitEvent e) {
        return e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical());
    }

    public static void initializePPE(ProjectileParryEvent ppe, float mult) {
        ProjectileInfo pi = projectileMap.getOrDefault(ppe.getProjectile().getType(), DEFAULTRANGED);
        ppe.setReturnVec(pi.destroy ? null : ppe.getProjectile().getMotion().normalize().scale(-0.1));
        ppe.setPostureConsumption((float) pi.posture * mult);
        ppe.setTrigger(pi.trigger);
    }

    private static class MeleeInfo {
        private final double attackPostureMultiplier, defensePostureMultiplier;
        private final double distractDamageBonus, unawareDamageBonus;
        private final int parryTime;
        private final float parryCount;
        private final boolean isShield;

        private MeleeInfo(double attack, double defend, boolean shield, int pTime, float pCount, double distract, double unaware) {
            attackPostureMultiplier = attack;
            defensePostureMultiplier = defend;
            isShield = shield;
            parryCount = pCount;
            parryTime = pTime;
            distractDamageBonus = distract;
            unawareDamageBonus = unaware;
        }
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

    public static class MobInfo {
        public final double mult;
        public final double chance;
        public final boolean omnidirectional, shield;

        private MobInfo(double m, double c, boolean o, boolean s) {
            mult = m;
            chance = c;
            omnidirectional = o;
            shield = s;
        }
    }
}
