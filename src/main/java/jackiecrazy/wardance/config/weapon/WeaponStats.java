package jackiecrazy.wardance.config.weapon;

import com.google.common.collect.Maps;
import com.google.gson.*;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.weapon.interactions.*;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncItemDataPacket;
import jackiecrazy.wardance.networking.sync.SyncTagDataPacket;
import jackiecrazy.wardance.utils.CombatUtils;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector4d;

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
    private static final ResourceLocation air = new ResourceLocation("air");
    public static List<Item> DESPERATION = new ArrayList<>();
    public static WeaponInfo DEFAULTMELEE = new WeaponInfo(1, 1);
    public static HashMap<Item, WeaponInfo> combatList = new HashMap<>();
    public static HashMap<Item, WeaponInfo> clientItems = new HashMap<>();
    public static HitInfo info_override = null;
    private static HashMap<TagKey<Item>, WeaponInfo> archetypes = new HashMap<>();
    private static HashMap<TagKey<Item>, WeaponInfo> clientArchetypes = new HashMap<>();

    public WeaponStats() {
        super(WeaponInteractions.GSON, "war_stats");
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

    public static void clientWeaponOverride(Map<Item, WeaponInfo> server) {
        clientItems.putAll(server);//the client doesn't need *that* much info, so we keep its list separate and save packets
    }

    public static void clientTagOverride(Map<TagKey<Item>, WeaponInfo> server) {
        clientArchetypes = new HashMap<>(server);
    }

    public static void updateItems(Map<ResourceLocation, JsonElement> object,
                                   ResourceManager rm,
                                   ProfilerFiller profiler) {
        DEFAULTMELEE = new WeaponInfo();
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
                        WeaponInfo put = parseMeleeInfo(name, obj);
                        archetypes.put(ItemTags.create(new ResourceLocation(name)), put);
                    } catch (Exception x) {
                        WarDance.LOGGER.error("malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                    return;
                }
                ResourceLocation i = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(i);
                if (item == null || (!i.equals(air) && item == Items.AIR)) {
                    return;
                }
                try {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    WeaponInfo put = parseMeleeInfo(name, obj);
                    if (GeneralConfig.debug)
                        WarDance.LOGGER.debug(name + " has been registered with sweep types: " + put.sweeps[0].getInteractionType() + " " + put.sweeps[1].getInteractionType() + " " + put.sweeps[2].getInteractionType() + " " + put.sweeps[3].getInteractionType() + " " + put.sweeps[4].getInteractionType() + " ");
                    combatList.put(item, put);
                } catch (Exception x) {
                    WarDance.LOGGER.error("malformed json under " + name + "!");
                    x.printStackTrace();
                }
            });
        });
    }

    @Nonnull
    private static WeaponInfo parseMeleeInfo(String root, JsonObject obj) {
        WeaponInfo put = WeaponInteractions.GSON.fromJson(obj, WeaponInfo.class);
        WeaponInteractions.WeaponInteraction defaultSweep = WeaponInteractions.GSON.fromJson(obj, WeaponInteractions.WeaponInteraction.class);
        put.sweeps[0] = defaultSweep;
        for (AttackType s : AttackType.values()) {
            int ord = s.ordinal();
            JsonElement gottem = obj.get(s.name().toLowerCase(Locale.ROOT));
            if (gottem != null) {
                JsonObject sub = gottem.getAsJsonObject();
                WeaponInteractions.WeaponInteraction sweep = WeaponInteractions.GSON.fromJson(sub, WeaponInteractions.WeaponInteraction.class);
                put.sweeps[ord] = sweep;
            }
            WeaponInteractions.WeaponInteraction sweep=put.sweeps[ord];
            if (sweep.description() == null)
                sweep.setDescription("wardance.tooltip.attacks." + root + "." + s.toString().toLowerCase(Locale.ROOT));
            else sweep.setDescription(sweep.description());//initialize the component
        }
        return put;
    }

    @Nullable
    public static WeaponInfo lookupStats(ItemStack is) {
        if (is == null) return null;
        if (combatList.containsKey(is.getItem())) return combatList.get(is.getItem());
        for (TagKey<Item> tag : archetypes.keySet()) {
            if (is.is(tag)) {
                //cache lookup
                combatList.put(is.getItem(), archetypes.get(tag));
                return archetypes.get(tag);
            }
        }
        if (clientItems.containsKey(is.getItem())) return clientItems.get(is.getItem());
        for (TagKey<Item> tag : clientArchetypes.keySet()) {
            if (is.is(tag)) {
                //cache lookup
                clientItems.put(is.getItem(), clientArchetypes.get(tag));
                return clientArchetypes.get(tag);
            }
        }
        return null;
    }

    public static boolean isShield(LivingEntity e, ItemStack stack) {
        if (stack == null) return false;
        WeaponInfo rt = lookupStats(stack);//stack.isShield(e);
        return rt != null && rt.shield;
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
        WeaponInfo rt = lookupStats(stack);
        return rt != null && !rt.shield;
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

    public static WeaponInteractions.WeaponInteraction getSweepInfo(ItemStack i, LivingEntity wielder, AttackType s) {
        final WeaponInfo info = lookupStats(i);
        if (info == null) {
            return SweepAttack.DEFAULT_NONE;
        } else {
            final WeaponInteractions.WeaponInteraction intl = info.sweeps[s.ordinal()];
            if (!intl.getOverrides().isEmpty()) {
                ArgumentContext ctx = new ArgumentContext(wielder, wielder);//todo allow target in the future?
                for (WeaponInteractions.InteractionOverride io : intl.getOverrides()) {
                    if (Boolean.TRUE.equals(io.condition().resolve(ctx))) {
                        return io.override();
                    }
                }
            }
            return intl;
        }
    }

    public static HitInfo getHitInfo(ItemStack i, LivingEntity wielder, AttackType s) {
        if (info_override != null) return info_override;
//        final WeaponInfo info = lookupStats(i);
//        if (info == null) return SweepAttack.DEFAULT_NONE.getHitInfo();
        return getSweepInfo(i, wielder, s).getHitInfo();
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
        AERIAL, //juggle, style

        //special actions//
        GUARD_COUNTER,//breach and posture damage
        THROW,//breach and style points
        PICKUP_FLOURISH,//posture
        DRAW_ATTACK
    }

    public static class WeaponInfo {
        private double attack, defend;
        private boolean shield;
        private MotionManager idle_frame = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 1, 0), Vec3.ZERO, 0).setEffects(new FrameEffects().setEffects()), 5);
        private MotionManager guard_frame = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(0, 1, 0, 90)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)), CombatConfig.parryTime / 2);
        private MotionManager aim_frame = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), 0).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)), 2);
        private MotionManager swap_frame = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), Vec3.ZERO, 0).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.AFTERIMAGE)), 2);
        //standing, falling, sneaking, sprinting, riding
        private WeaponInteractions.WeaponInteraction[] sweeps = new WeaponInteractions.WeaponInteraction[AttackType.values().length];

        private WeaponInfo() {
            this(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend);
        }

        private WeaponInfo(double attack, double defend) {
            this.attack = attack;
            this.defend = defend;
            for (int i = 0; i < sweeps.length; i++) {
                sweeps[i] = SweepAttack.DEFAULT_FAN.clone();
            }
            sweeps[AttackType.GUARD_COUNTER.ordinal()] = Animation.FLURRY;
            sweeps[AttackType.THROW.ordinal()] = Throw.DEFAULT;
            sweeps[AttackType.PICKUP_FLOURISH.ordinal()] = Animation.CIRCLE;
        }

        public static WeaponInfo read(FriendlyByteBuf f) {
            WeaponInfo ret = new WeaponInfo();
            ret.attack = f.readDouble();
            ret.defend = f.readDouble();
            ret.shield = f.readBoolean();
            for (int x = 0; x < ret.sweeps.length; x++) {
                ret.sweeps[x] = WeaponInteractions.WeaponInteraction.readFromByte(f);
            }
            return ret;
        }

        public MotionManager swap_frame() {
            return swap_frame;
        }

        public MotionManager aim_frame() {
            return aim_frame;
        }

        public MotionManager guard_frame() {
            return guard_frame;
        }

        public MotionManager idle_frame() {
            return idle_frame;
        }

        public double getAttackPostureMultiplier() {
            return attack;
        }

        public double getDefensePostureMultiplier() {
            return defend;
        }

        public void write(FriendlyByteBuf f) {
            f.writeDouble(attack);
            f.writeDouble(defend);
            f.writeBoolean(shield);

            for (WeaponInteractions.WeaponInteraction ss : sweeps) {
                ss.write(f);
            }
        }
    }


}
