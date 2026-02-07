package jackiecrazy.wardance.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncItemDataPacket;
import jackiecrazy.wardance.networking.sync.SyncTagDataPacket;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SweepActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class WeaponStats extends SimpleJsonResourceReloadListener {
    public static final TagKey<Item> TWO_HANDED = ItemTags.create(new ResourceLocation(WarDance.MODID, "two_handed"));
    public static final TagKey<Item> PARRY_PROJECTILE = ItemTags.create(new ResourceLocation(WarDance.MODID, "parry_projectiles"));
    public static final TagKey<Item> CAN_BE_DISABLED = ItemTags.create(new ResourceLocation(WarDance.MODID, "can_be_disabled"));
    public static final TagKey<Item> DISABLE_SHIELD = ItemTags.create(new ResourceLocation(WarDance.MODID, "disable_shield"));
    public static final TagKey<Item> UNARMED = ItemTags.create(new ResourceLocation(WarDance.MODID, "unarmed"));
    public static final TagKey<Item> PIERCE_SHIELD = ItemTags.create(new ResourceLocation(WarDance.MODID, "pierce_shield"));
    public static final TagKey<Item> CANNOT_BLOCK = ItemTags.create(new ResourceLocation(WarDance.MODID, "cannot_parry"));
    public static final TagKey<Item> DEMON_HUNTER_CHARGE_RANGED = ItemTags.create(new ResourceLocation(WarDance.MODID, "demon_hunter_ranged"));
    public static final TagKey<Item> DESPERATE_THROW = ItemTags.create(new ResourceLocation(WarDance.MODID, "desperate_throw"));
    public static List<Item> DESPERATION = new ArrayList<>();
    public static MeleeInfo DEFAULTMELEE = new MeleeInfo(1, 1);
    public static HashMap<Item, MeleeInfo> combatList = new HashMap<>();
    public static SweepActions.HitInfo info_override = null;
    private static HashMap<TagKey<Item>, MeleeInfo> archetypes = new HashMap<>();

    public WeaponStats() {
        super(SweepActions.GSON, "war_stats");
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new WeaponStats());
    }

    public static void sendItemData(ServerPlayer p) {
        //duplicated removed automatically
        Set<String> paths = combatList.keySet().stream().map(a -> ForgeRegistries.ITEMS.getKey(a).getNamespace()).collect(Collectors.toSet());
        for (String namespace : paths)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncItemDataPacket(Maps.filterEntries(combatList, a -> ForgeRegistries.ITEMS.getKey(a.getKey()).getNamespace().equals(namespace))));
        //CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncItemDataPacket(new HashMap<>(combatList)));
        CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncTagDataPacket(archetypes));
    }

    public static void clientWeaponOverride(Map<Item, MeleeInfo> server) {
        combatList.putAll(server);
    }

    public static void clientTagOverride(Map<TagKey<Item>, MeleeInfo> server) {
        archetypes = new HashMap<>(server);
    }

    public static void updateItems(Map<ResourceLocation, JsonElement> object,
                                   ResourceManager rm,
                                   ProfilerFiller profiler) {
        DEFAULTMELEE = new MeleeInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend);
        combatList = new HashMap<>();
        archetypes = new HashMap<>();

        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            if (GeneralConfig.debug) WarDance.LOGGER.debug("loading " + key);
            file.entrySet().forEach(entry -> {
                String name = entry.getKey();
                if (name.startsWith("#")) {//register tags separately
                    try {
                        name = name.substring(1);
                        if (!name.contains(":")) name = "wardance:" + name;
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        MeleeInfo put = parseMeleeInfo(obj);
                        archetypes.put(ItemTags.create(new ResourceLocation(name)), put);
                    } catch (Exception x) {
                        WarDance.LOGGER.error("malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                    return;
                }
                ResourceLocation i = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(i);
                if (item == null || item == Items.AIR) {
                    if (GeneralConfig.debug) WarDance.LOGGER.debug(name + " is not a registered item!");
                    return;
                }
                try {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    MeleeInfo put = parseMeleeInfo(obj);
                    if (GeneralConfig.debug)
                        WarDance.LOGGER.debug(name + " has been registered with sweep types: " + put.sweeps[0].getType() + " " + put.sweeps[1].getType() + " " + put.sweeps[2].getType() + " " + put.sweeps[3].getType() + " " + put.sweeps[4].getType() + " ");
                    combatList.put(item, put);
                } catch (Exception x) {
                    WarDance.LOGGER.error("malformed json under " + name + "!");
                    x.printStackTrace();
                }
            });
        });
    }

    @Nonnull
    private static MeleeInfo parseMeleeInfo(JsonObject obj) {
        MeleeInfo put = new MeleeInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend);
        if (obj.has("attack")) put.attackPostureMultiplier = obj.get("attack").getAsDouble();
        if (obj.has("defend")) put.defensePostureMultiplier = obj.get("defend").getAsDouble();
        if (obj.has("shield")) put.isShield = obj.get("shield").getAsBoolean();
        SweepActions.SweepInfo defaultSweep = SweepActions.GSON.fromJson(obj, SweepActions.SweepInfo.class);
        put.sweeps[0] = defaultSweep;
        for (AttackType s : AttackType.values()) {
            int ord = s.ordinal();
            JsonElement gottem = obj.get(s.name().toLowerCase(Locale.ROOT));
            JsonObject sub = gottem.getAsJsonObject();
            SweepActions.SweepInfo sweep = SweepActions.GSON.fromJson(sub, SweepActions.SweepInfo.class);
            put.sweeps[ord] = sweep;
        }
        return put;
    }

    @Nullable
    public static MeleeInfo lookupStats(ItemStack is) {
        if (is == null) return null;
        if (combatList.containsKey(is.getItem())) return combatList.get(is.getItem());
        for (TagKey<Item> tag : archetypes.keySet()) {
            if (is.is(tag)) {
                //cache lookup
                combatList.put(is.getItem(), archetypes.get(tag));
                return archetypes.get(tag);
            }
        }
        return null;
    }

    public static boolean isShield(LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        MeleeInfo rt = lookupStats(stack);//stack.isShield(e);
        return rt != null && rt.isShield;
    }

    public static boolean canParryProjectile(LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        return stack.is(PARRY_PROJECTILE) || isShield(e, stack);
    }

    public static boolean canBeDisabled(LivingEntity e, LivingEntity attacker, ItemStack stack) {
        if (stack == null) return false;
        return stack.is(CAN_BE_DISABLED) || isShield(e, stack);
    }

    public static boolean isCombatItem(LivingEntity e, ItemStack stack) {
        return lookupStats(stack) != null;
    }

    public static boolean isCombatItem(LivingEntity e, InteractionHand hand) {
        if (e == null) return false;
        return isCombatItem(e, e.getItemInHand(hand));
    }

    public static boolean isShield(LivingEntity e, InteractionHand hand) {
        if (e == null) return false;
        return isShield(e, e.getItemInHand(hand));
    }

    public static boolean isWeapon(@Nullable LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        MeleeInfo rt = lookupStats(stack);
        return rt != null && !rt.isShield;
    }

    public static boolean isUnarmed(ItemStack is, LivingEntity e) {
        return is.isEmpty() || is.is(UNARMED);
    }

    public static boolean isTwoHanded(ItemStack is, LivingEntity e, InteractionHand h) {
        final double handing = e == null ? 0 : e.getAttributeValue(FootworkAttributes.TWO_HANDING.get());
        if (h == InteractionHand.MAIN_HAND && handing >= 1d) return false;
        if (h == InteractionHand.OFF_HAND && handing >= 3d) return false;
        //the hand is instantly swapped on offhand attack, which means a main hand twohander will now be on the offhand ._.
        //this only happens at twohander values 1 and 2
        return !is.isEmpty() && is.is(TWO_HANDED);
    }

    public static boolean twoHandBonus(LivingEntity e, InteractionHand h) {
        // -1 disallows receiving bonuses from two-handing.
        //At 0, you wield two-handed weapons normally.
        // 1 allows you to wield a two-handed weapon with a one-handed weapon in the offhand.
        // 2 allows you to do so while maintaining the two-handed bonus.
        // 3 allows dual wielding two-handers with no two-hander bonus, and
        // 4 allows you to maintain the two-handing bonus of both.
        final double twohanding = e.getAttributeValue(FootworkAttributes.TWO_HANDING.get());
        if (twohanding < 0) return false;
        boolean offhandFree = CombatData.getCap(e).getHandBind(InteractionHand.OFF_HAND) > 0 || CombatUtils.isHoldingNonWeapon(e, InteractionHand.OFF_HAND);
        boolean offhandTwo = isTwoHanded(e.getOffhandItem(), e, InteractionHand.OFF_HAND);
        if (h == InteractionHand.MAIN_HAND && (twohanding >= 4 || !offhandTwo && twohanding >= 2 || (offhandFree && twohanding >= 0)))
            return true;
        if (h == InteractionHand.OFF_HAND && twohanding < 4) return false;
        return h == InteractionHand.MAIN_HAND && offhandFree;
    }

    public static boolean canPierceShield(ItemStack is, Entity e) {
        return is.is(PIERCE_SHIELD);
    }

    public static SweepActions.SweepInfo getSweepInfo(ItemStack i, AttackType s) {
        final MeleeInfo info = lookupStats(i);
        return info == null ? SweepActions.DEFAULT_NONE : info.sweeps[s.ordinal()];
    }
    public static SweepActions.HitInfo getHitInfo(ItemStack i, AttackType s) {
        if (info_override != null) return info_override;
        final MeleeInfo info = lookupStats(i);
        return info == null ? SweepActions.DEFAULT_NONE.getHitInfo() : info.sweeps[s.ordinal()].getHitInfo();
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object,
                         @Nonnull ResourceManager rm,
                         @Nonnull ProfilerFiller profiler) {
        updateItems(object, rm, profiler);
    }

    public enum AttackType {
        UNDEFINED,//becomes internal damage

        //normal actions//
        STANDING, //normal intent, posture
        FALLING, //vertical motion, posture
        SPRINTING,//forward motion, posture

        //special actions//
        GRAPPLE_FLYING,//minor posture and knockback
        GRAPPLE_ATTACK,//breach and internal damage
        GUARD_COUNTER,//breach and posture damage
        THROW,//breach and style points
        PICKUP_FLOURISH//posture
    }

    public static class MeleeInfo {
        private double attackPostureMultiplier, defensePostureMultiplier;
        private boolean isShield, ignoreParry, ignoreShield, canParry;
        //standing, falling, sneaking, sprinting, riding
        private SweepActions.SweepInfo[] sweeps = {
                SweepActions.DEFAULT_FAN.clone(), SweepActions.DEFAULT_CLEAVE.clone(), SweepActions.DEFAULT_FAN.clone(), SweepActions.DEFAULT_FAN.clone(),
                SweepActions.DEFAULT_FAN.clone(), SweepActions.DEFAULT_FAN.clone(), SweepActions.DEFAULT_FAN.clone(), SweepActions.DEFAULT_FAN.clone(), SweepActions.DEFAULT_FAN.clone()
        };

        private MeleeInfo(double attack, double defend) {
            attackPostureMultiplier = attack;
            defensePostureMultiplier = defend;
        }

        public static MeleeInfo read(FriendlyByteBuf f) {
            MeleeInfo ret = new MeleeInfo(0, 0);
            ret.attackPostureMultiplier = f.readDouble();
            ret.defensePostureMultiplier = f.readDouble();
            ret.isShield = f.readBoolean();
            for (SweepActions.SweepInfo ss : ret.sweeps) {
                ss.read(f);
            }
            return ret;
        }

        public double getAttackPostureMultiplier() {
            return attackPostureMultiplier;
        }

        public double getDefensePostureMultiplier() {
            return defensePostureMultiplier;
        }

        public void write(FriendlyByteBuf f) {
            f.writeDouble(attackPostureMultiplier);
            f.writeDouble(defensePostureMultiplier);
            f.writeBoolean(isShield);

            for (SweepActions.SweepInfo ss : sweeps) {
                ss.write(f);
            }
        }
    }


}
