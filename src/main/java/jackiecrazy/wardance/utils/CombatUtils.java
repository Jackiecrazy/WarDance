package jackiecrazy.wardance.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.weaponry.CombatManipulator;
import jackiecrazy.footwork.event.AttackMightEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateAttackPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CombatUtils {
    public static final UUID off = UUID.fromString("8c8028c8-da69-49a2-99cd-f92d7ad22534");
    public static final TagKey<Item> TWO_HANDED = ItemTags.create(new ResourceLocation(WarDance.MODID, "two_handed"));
    public static final TagKey<Item> UNARMED = ItemTags.create(new ResourceLocation(WarDance.MODID, "unarmed"));
    public static final TagKey<Item> PIERCE_PARRY = ItemTags.create(new ResourceLocation(WarDance.MODID, "pierce_parry"));
    public static final TagKey<Item> PIERCE_SHIELD = ItemTags.create(new ResourceLocation(WarDance.MODID, "pierce_shield"));
    public static final TagKey<Item> CANNOT_PARRY = ItemTags.create(new ResourceLocation(WarDance.MODID, "cannot_parry"));
    private static final UUID main = UUID.fromString("8c8028c8-da67-49a2-99cd-f92d7ad22534");
    public static HashMap<ResourceLocation, Float> customPosture = new HashMap<>();
    public static HashMap<ResourceLocation, MobInfo> parryMap = new HashMap<>();
    public static HashMap<Item, AttributeModifier[]> armorStats = new HashMap<>();
    public static HashMap<Item, AttributeModifier[]> shieldStat = new HashMap<>();
    public static boolean isSweeping = false;
    public static boolean suppress = false;
    private static MeleeInfo DEFAULTMELEE = new MeleeInfo(1, 1);
    private static ProjectileInfo DEFAULTRANGED = new ProjectileInfo(0.1, 1, false, false);
    private static HashMap<Item, MeleeInfo> combatList = new HashMap<>();
    private static HashMap<EntityType, ProjectileInfo> projectileMap = new HashMap<>();
    private static ArrayList<Item> unarmed = new ArrayList<>();

    public static void updateItems(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        DEFAULTMELEE = new MeleeInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend);
        combatList = new HashMap<>();
        armorStats = new HashMap<>();
        shieldStat = new HashMap<>();
        unarmed = new ArrayList<>();

        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            file.entrySet().forEach(entry -> {
                final String name = entry.getKey();
                ResourceLocation i = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(i);
                if (item == null||item== Items.AIR) {
                    if (GeneralConfig.debug)
                        WarDance.LOGGER.debug(name + " is not a registered item!");
                    return;
                }
                try {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    MeleeInfo put = new MeleeInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend);
                    if (obj.has("attack")) put.attackPostureMultiplier = obj.get("attack").getAsDouble();
                    if (obj.has("defend")) put.defensePostureMultiplier = obj.get("defend").getAsDouble();
                    if (obj.has("shield")) put.isShield = obj.get("shield").getAsBoolean();
                    combatList.put(item, put);
                } catch (Exception x) {
                    WarDance.LOGGER.error("malformed json under " + name + "!");
                    x.printStackTrace();
                }
            });
        });
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
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
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

    public static boolean isShield(LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        return combatList.containsKey(stack.getItem()) && (combatList.getOrDefault(stack.getItem(), DEFAULTMELEE).isShield);//stack.isShield(e);
    }

    public static boolean isShield(LivingEntity e, InteractionHand hand) {
        return isShield(e, e.getItemInHand(hand));
    }

    public static boolean isHoldingShield(LivingEntity e){
        return isShield(e, InteractionHand.MAIN_HAND) || isShield(e, InteractionHand.OFF_HAND);
    }

    public static boolean isWeapon(@Nullable LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        return combatList.containsKey(stack.getItem()) && !combatList.getOrDefault(stack.getItem(), DEFAULTMELEE).isShield;//stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    public static boolean isUnarmed(ItemStack is, LivingEntity e) {
        return is.isEmpty() || is.is(UNARMED);
    }

    public static boolean isUnarmed(LivingEntity e, InteractionHand hand) {
        return isUnarmed(e.getItemInHand(hand), e);
    }

    public static boolean isFullyUnarmed(LivingEntity e) {
        return isUnarmed(e, InteractionHand.MAIN_HAND) && isUnarmed(e, InteractionHand.OFF_HAND);
    }

    public static boolean isTwoHanded(ItemStack is, LivingEntity e) {
        return !is.isEmpty() && is.is(TWO_HANDED);
    }

    public static boolean canPierceParry(ItemStack is, LivingEntity e) {
        return is.is(PIERCE_PARRY);
    }

    public static boolean canPierceShield(ItemStack is, LivingEntity e) {
        return is.is(PIERCE_SHIELD);
    }

    public static boolean canParry(LivingEntity defender, Entity attacker, @Nonnull ItemStack i, float postureDamage) {
        return canParry(defender, attacker, i, null, postureDamage);
    }

    public static boolean canParry(LivingEntity defender, Entity attacker, @Nonnull ItemStack defend, @Nullable ItemStack attack, float postureDamage) {
        InteractionHand h = defender.getOffhandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (postureDamage < 0) return false;
        if (attacker instanceof LivingEntity && getPostureDef((LivingEntity) attacker, defender, defend, postureDamage) < 0)
            return false;
        if (defend.is(CANNOT_PARRY))
            return false;
        if (attack != null) {
            if (attack.is(PIERCE_PARRY) && isWeapon(defender, defend))
                return false;
            if (attack.is(PIERCE_SHIELD) && isShield(defender, defend))
                return false;
        }
        if (defender instanceof Player && ((Player) defender).getCooldowns().isOnCooldown(defend.getItem()))
            return false;
        if (CombatData.getCap(defender).getHandBind(h) > 0)
            return false;
        float rand = WarDance.rand.nextFloat();
        boolean recharge = true;//getCooledAttackStrength(defender, h, 0.5f) > 0.9f && CombatData.getCap(defender).getHandBind(h) == 0;
        recharge &= (!(defender instanceof Player) || ((Player) defender).getCooldowns().getCooldownPercent(defender.getItemInHand(h).getItem(), 0) == 0);
        if (defend.getCapability(CombatManipulator.CAP).isPresent() && attacker instanceof LivingEntity) {
            return defend.getCapability(CombatManipulator.CAP).resolve().get().canBlock(defender, attacker, defend, recharge, postureDamage);
        }
        if (isShield(defender, defend)) {
            boolean canShield = (defender instanceof Player || rand < CombatConfig.mobParryChanceShield);
            boolean canParry = true;//CombatData.getCap(defender).getBarrierCooldown() == 0 || CombatData.getCap(defender).getBarrier() > 0;
            return recharge & canParry & canShield;
        } else if (isWeapon(defender, defend)) {
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

    public static float getPostureAtk(@Nullable LivingEntity attacker, @Nullable LivingEntity defender, @Nullable InteractionHand h, float amount, ItemStack stack) {
        float base = amount * (float) DEFAULTMELEE.attackPostureMultiplier;
        //Spartan Shields compat, doesn't seem to work.
        if (attacker != null && attacker.isBlocking()) {
            h = attacker.getUsedItemHand();
            stack = attacker.getItemInHand(h);
        }
        float scaler = CombatConfig.mobScaler;
        if (stack != null && !stack.isEmpty()) {
            scaler = 1;
            if (stack.getCapability(CombatManipulator.CAP).isPresent()) {
                base = stack.getCapability(CombatManipulator.CAP).resolve().get().postureDealtBase(attacker, defender, stack, amount);
            } else if (combatList.containsKey(stack.getItem()))
                base = (float) combatList.get(stack.getItem()).attackPostureMultiplier;

        } else {
            if (attacker != null && !(attacker instanceof Player))
                base = CombatData.getCap(attacker).getMaxPosture() * CombatConfig.defaultMultiplierPostureMob;
        }
        if (attacker == null || h == null) return base;
        final float fin = attacker instanceof Player ? Math.max(CombatData.getCap(attacker).getCachedCooldown(), ((Player) attacker).getAttackStrengthScale(0.5f)) : scaler;
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
        //TODO does this break anything?
        return s.getEntity() != null && s.getEntity() == s.getDirectEntity() && !s.isExplosion() && !s.isProjectile();//!s.isFire() && !s.isMagic() &&
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

    public static boolean isPhysicalAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.getDamageTyping() == CombatDamageSource.TYPE.PHYSICAL;
        }
        return !s.isExplosion() && !s.isFire() && !s.isMagic() && !s.isBypassArmor();
    }

    public static boolean isTrueDamage(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.getDamageTyping() == CombatDamageSource.TYPE.TRUE;
        }
        return s.isBypassInvul() || (s.isBypassMagic() && s.isBypassArmor());
    }

    /**
     * knocks the target back, with regards to the attacker's relative angle to the target, and adding y knockback
     */
    public static void knockBack(Entity to, Entity from, float strength, boolean considerRelativeAngle, boolean bypassAllChecks) {
        Vec3 distVec = to.position().add(0, to.getBbHeight() / 2, 0).vectorTo(from.position().add(0, from.getBbHeight() / 2, 0)).multiply(1, 0.5, 1).normalize();
        if (to instanceof LivingEntity && !bypassAllChecks) {
            if (considerRelativeAngle)
                knockBack((LivingEntity) to, from, strength, distVec.x, distVec.y, distVec.z, false);
            else
                knockBack(((LivingEntity) to), from, (float) strength * 0.5F, (double) Mth.sin(from.getYRot() * 0.017453292F), 0, (double) (-Mth.cos(from.getYRot() * 0.017453292F)), false);
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
            Vec3 vec = to.getDeltaMovement();
            double motionX = vec.x, motionY = vec.y, motionZ = vec.z;
            to.hasImpulse = true;
            double pythagora = Math.sqrt(xRatio * xRatio + zRatio * zRatio);
            if (to.isOnGround()) {
                motionY /= 2.0D;
                motionY += strength;

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
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e), new UpdateAttackPacket(e.getId(), real));
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
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e), new UpdateAttackPacket(e.getId(), amount));
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
        suppress = false;
//        attributes.addAll(main.getAttributeModifiers(EquipmentSlotType.MAINHAND).keys());
//        attributes.addAll(main.getAttributeModifiers(EquipmentSlotType.OFFHAND).keys());
//        attributes.addAll(off.getAttributeModifiers(EquipmentSlotType.MAINHAND).keys());
//        attributes.addAll(off.getAttributeModifiers(EquipmentSlotType.OFFHAND).keys());
//        attributes.forEach((att)->{Optional.ofNullable(e.getAttribute(att)).ifPresent(ModifiableAttributeInstance::compute);});
        main.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.removeModifier(mod);});
        });
        off.getAttributeModifiers(EquipmentSlot.OFFHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.removeModifier(mod);});
        });
        main.getAttributeModifiers(EquipmentSlot.OFFHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.addTransientModifier(mod);});
        });
        off.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((att, mod) -> {
            Optional.ofNullable(e.getAttribute(att)).ifPresent((mai) -> {mai.addTransientModifier(mod);});
        });
        e.attackStrengthTicker = cap.getOffhandCooldown();
        cap.setOffhandCooldown(tssl);
    }

    public static void sweep(LivingEntity e, Entity ignore, InteractionHand h, double reach) {
        if (!GeneralConfig.betterSweep) return;
        if (!CombatData.getCap(e).isCombatMode()) return;
        if (CombatData.getCap(e).getForcedSweep() == 0) {
            CombatData.getCap(e).setForcedSweep(-1);
            return;
        }
        if (h == InteractionHand.OFF_HAND) {
            swapHeldItems(e);
            CombatData.getCap(e).setOffhandAttack(true);
        }
        int angle = CombatData.getCap(e).getForcedSweep() > 0 ? CombatData.getCap(e).getForcedSweep() : EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, e) * GeneralConfig.sweepAngle;
        if (e.getMainHandItem().getCapability(CombatManipulator.CAP).isPresent())
            angle = e.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().sweepArea(e, e.getMainHandItem());
        float charge = Math.max(CombatUtils.getCooledAttackStrength(e, InteractionHand.MAIN_HAND, 0.5f), CombatData.getCap(e).getCachedCooldown());
        boolean hit = false;
        isSweeping = ignore != null;
        double modRange = Math.min(GeneralConfig.maxRange, GeneralConfig.baseRange + (reach - 3) * GeneralConfig.rangeMult);
        for (Entity target : e.level.getEntities(e, e.getBoundingBox().inflate(modRange + 3))) {
            if (target == ignore) {
                if (angle > 0)
                    hit = true;
                continue;
            }
            if (!GeneralUtils.isFacingEntity(e, target, angle)) continue;
            if (!e.hasLineOfSight(target)) continue;
            if (GeneralUtils.getDistSqCompensated(e, target) > modRange * modRange) continue;
            CombatUtils.setHandCooldown(e, InteractionHand.MAIN_HAND, charge, false);
            hit = true;
            if (e instanceof Player)
                ((Player) e).attack(target);
            else e.doHurtTarget(target);
            isSweeping = true;
        }
        if (e instanceof Player && hit) {
            ((Player) e).sweepAttack();
            e.level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, e.getSoundSource(), 1.0F, 1.0F);
        }
        isSweeping = false;
        if (h == InteractionHand.OFF_HAND) {
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
        ppe.setReturnVec(pi.destroy ? null : ppe.getProjectile().getDeltaMovement().normalize().scale(-0.1));
        ppe.setPostureConsumption((float) pi.posture * mult);
        ppe.setTrigger(pi.trigger);
    }

    private static class MeleeInfo {
        private double attackPostureMultiplier, defensePostureMultiplier;
        private boolean isShield, ignoreParry, ignoreShield, canParry;

        private MeleeInfo(double attack, double defend) {
            attackPostureMultiplier = attack;
            defensePostureMultiplier = defend;
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
