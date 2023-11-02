package jackiecrazy.wardance.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncItemDataPacket;
import jackiecrazy.wardance.networking.SyncTagDataPacket;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringUtil;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeaponStats extends SimpleJsonResourceReloadListener {
    public static final TagKey<Item> TWO_HANDED = ItemTags.create(new ResourceLocation(WarDance.MODID, "two_handed"));
    public static final TagKey<Item> PARRY_PROJECTILE = ItemTags.create(new ResourceLocation(WarDance.MODID, "parry_projectiles"));
    public static final TagKey<Item> CAN_BE_DISABLED = ItemTags.create(new ResourceLocation(WarDance.MODID, "can_be_disabled"));
    public static final TagKey<Item> AXE_LIKE = ItemTags.create(new ResourceLocation(WarDance.MODID, "disable_shield"));
    public static final TagKey<Item> UNARMED = ItemTags.create(new ResourceLocation(WarDance.MODID, "unarmed"));
    public static final TagKey<Item> PIERCE_PARRY = ItemTags.create(new ResourceLocation(WarDance.MODID, "pierce_parry"));
    public static final TagKey<Item> PIERCE_SHIELD = ItemTags.create(new ResourceLocation(WarDance.MODID, "pierce_shield"));
    public static final TagKey<Item> CANNOT_PARRY = ItemTags.create(new ResourceLocation(WarDance.MODID, "cannot_parry"));
    private static final SweepInfo DEFAULT_FAN = new SweepInfo(SWEEPTYPE.CONE, 30, 30);
    private static final SweepInfo DEFAULT_CLEAVE = new SweepInfo(SWEEPTYPE.CLEAVE, 30, 30);
    private static final SweepInfo DEFAULT_IMPACT = new SweepInfo(SWEEPTYPE.IMPACT, 1, 1.5);
    private static final SweepInfo DEFAULT_LINE = new SweepInfo(SWEEPTYPE.LINE, 1, 1.5);
    private static final SweepInfo DEFAULT_CIRCLE = new SweepInfo(SWEEPTYPE.CIRCLE, 1, 1.5);
    private static final SweepInfo DEFAULT_NONE = new SweepInfo(SWEEPTYPE.NONE, 0, 0);
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(SweepInfo.class, new SweepAdapter()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();
    public static MeleeInfo DEFAULTMELEE = new MeleeInfo(1, 1);
    public static HashMap<Item, MeleeInfo> combatList = new HashMap<>();
    private static HashMap<TagKey<Item>, MeleeInfo> archetypes = new HashMap<>();

    public WeaponStats() {
        super(GSON, "war_stats");
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

    public static void updateItems(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        DEFAULTMELEE = new MeleeInfo(CombatConfig.defaultMultiplierPostureAttack, CombatConfig.defaultMultiplierPostureDefend);
        combatList = new HashMap<>();
        archetypes = new HashMap<>();

        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            if (GeneralConfig.debug)
                WarDance.LOGGER.debug("loading " + key);
            file.entrySet().forEach(entry -> {
                final String name = entry.getKey();
                if (name.startsWith("#")) {//register tags separately
                    try {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        MeleeInfo put = parseMeleeInfo(obj);
                        archetypes.put(ItemTags.create(new ResourceLocation(WarDance.MODID, name.substring(1))), put);
                    } catch (Exception x) {
                        WarDance.LOGGER.error("malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                    return;
                }
                ResourceLocation i = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(i);
                if (item == null || item == Items.AIR) {
                    if (GeneralConfig.debug)
                        WarDance.LOGGER.debug(name + " is not a registered item!");
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
        SweepInfo defaultSweep = GSON.fromJson(obj, SweepInfo.class);
        put.sweeps[0] = defaultSweep;
        for (SWEEPSTATE s : SWEEPSTATE.values()) {
            int ord = s.ordinal();
            JsonElement gottem = obj.get(s.name().toLowerCase(Locale.ROOT));
            if (gottem == null || !gottem.isJsonObject()) {
                if (GeneralConfig.debug)
                    WarDance.LOGGER.debug("did not find " + s + ", generating defaults");
                //"smartly" infer what kind of falling attack is wanted:
                //cone->cleave, impact->impact, line->line, the others->none
                if (s == SWEEPSTATE.FALLING)
                    switch (defaultSweep.sweep) {
                        case CONE -> {
                            SweepInfo fresh = new SweepInfo(SWEEPTYPE.CLEAVE, defaultSweep.sweep_base, defaultSweep.sweep_scale);
                            fresh.crit = true;
                            put.sweeps[ord] = fresh;
                        }
                        case IMPACT, LINE -> put.sweeps[ord] = defaultSweep;
                        default -> put.sweeps[ord] = DEFAULT_NONE;
                    }
                else put.sweeps[ord] = put.sweeps[0];
                continue;
            }
            JsonObject sub = gottem.getAsJsonObject();
            SweepInfo sweep = GSON.fromJson(sub, SweepInfo.class);
            put.sweeps[ord] = sweep;
        }
        return put;
    }

    @Nullable
    public static MeleeInfo lookupStats(ItemStack is) {
        if (is == null) return null;
        if (combatList.containsKey(is.getItem())) return combatList.get(is.getItem());
        for (TagKey<Item> tag : archetypes.keySet()) {
            if (is.is(tag))
                return archetypes.get(tag);
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
        if (h == InteractionHand.MAIN_HAND && handing >= 1d)
            return false;
        if (h == InteractionHand.OFF_HAND && handing >= 3d)
            return false;
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
        if (h == InteractionHand.OFF_HAND && twohanding < 4)
            return false;
        return h == InteractionHand.MAIN_HAND && offhandFree;
    }

    public static boolean canPierceParry(ItemStack is, LivingEntity e) {
        return is.is(PIERCE_PARRY);
    }

    public static boolean canPierceShield(ItemStack is, LivingEntity e) {
        return is.is(PIERCE_SHIELD);
    }

    public static SweepInfo getSweepInfo(ItemStack i, SWEEPSTATE s) {
        final MeleeInfo info = lookupStats(i);
        return info == null ? DEFAULT_NONE : info.sweeps[s.ordinal()];
    }

    public static SWEEPTYPE getSweepType(LivingEntity e, ItemStack i, SWEEPSTATE s) {
        return getSweepInfo(i, s).sweep;
    }

    public static double getSweepBase(ItemStack i, SWEEPSTATE s) {
        return getSweepInfo(i, s).sweep_base;
    }

    public static double getSweepScale(ItemStack i, SWEEPSTATE s) {
        return getSweepInfo(i, s).sweep_scale;
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object, @Nonnull ResourceManager rm, @Nonnull ProfilerFiller profiler) {
        updateItems(object, rm, profiler);
    }

    public enum SWEEPTYPE {
        NONE,
        CONE,//horizontal fan area in front of the entity up to max range, base and scale add angle
        CLEAVE,//cone but vertical
        LINE,//1 block wide line up to max range, base and scale add to thickness
        IMPACT,//splash at point of impact or furthest distance if no mob aimed, base and scale add radius
        CIRCLE//splash with entity as center, ignores range, base and scale add radius
    }

    public enum SWEEPSTATE {
        STANDING,
        //RISING, //may implement some day
        FALLING,
        SNEAKING,
        SPRINTING,//also while swimming
        RIDING //no speed requirement
    }

    public static class MeleeInfo {
        private double attackPostureMultiplier, defensePostureMultiplier;
        private boolean isShield, ignoreParry, ignoreShield, canParry;
        //standing, falling, sneaking, sprinting, riding
        private SweepInfo[] sweeps = {DEFAULT_FAN.clone(),
                DEFAULT_CLEAVE.clone(),
                DEFAULT_IMPACT.clone(),
                DEFAULT_CIRCLE.clone(),
                DEFAULT_LINE.clone()};

        private MeleeInfo(double attack, double defend) {
            attackPostureMultiplier = attack;
            defensePostureMultiplier = defend;
        }

        public static MeleeInfo read(FriendlyByteBuf f) {
            MeleeInfo ret = new MeleeInfo(0, 0);
            ret.attackPostureMultiplier = f.readDouble();
            ret.defensePostureMultiplier = f.readDouble();
            ret.isShield = f.readBoolean();
            for (SweepInfo ss : ret.sweeps) {
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

            for (SweepInfo ss : sweeps) {
                ss.write(f);
            }
        }
    }

    public static class SweepInfo {
        private static final SweepInfo REFERENCE = new SweepInfo(SWEEPTYPE.NONE, 0, 0);
        //general effects:
        // knockback scaling (negative supported),
        // (posture) damage scaling,
        // force crit,
        // crit damage
        private double knockback = 1;
        private double damage_scale = 1;
        private double posture_scale = 1;
        private boolean crit = false;
        private double crit_damage = 1.5;
        private double sweep_base = 0;
        private double sweep_scale = 0;
        private SWEEPTYPE sweep = SWEEPTYPE.NONE;
        private String hit_self_command = "", hit_other_command = "", damage_self_command = "", damage_other_command = "";

        private SweepInfo(SWEEPTYPE t, double b, double s) {
            sweep = t;
            sweep_base = b;
            sweep_scale = s;
        }

        private static ChatFormatting getColorFromValue(double a) {
            if (a > 1)
                return ChatFormatting.GREEN;
            else if (a < 0)
                return ChatFormatting.YELLOW;
            else return ChatFormatting.RED;
        }

        public Component getToolTip(ItemStack e, boolean advanced) {
            String advance = "";
            double finalized = sweep_base + (sweep_scale * e.getEnchantmentLevel(Enchantments.SWEEPING_EDGE));
            MutableComponent sweepTip = Component.translatable("wardance.tooltip.sweep." + sweep, Component.literal(String.valueOf(finalized)).withStyle(ChatFormatting.AQUA));
            //grab different tooltips if and only if they are different
            double damage = damage_scale;
            double posture = posture_scale;
            if (knockback != REFERENCE.knockback) {
                MutableComponent cp = Component.literal(RenderUtils.formatter.format(knockback) + "x");
                if (!advanced) {
                    if (knockback > 1)
                        cp = Component.translatable("wardance.tooltip.more");
                    else if (knockback < 0)
                        cp = Component.translatable("wardance.tooltip.negative");
                    else
                        cp = Component.translatable("wardance.tooltip.less");
                }
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.knockback", cp.withStyle(getColorFromValue(knockback))));
            }
            if (crit) {
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.crit").withStyle(ChatFormatting.GOLD));
                damage *= crit_damage;
                posture *= crit_damage;
            }
            if (damage != 1)
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.damage" + advance, Component.literal(RenderUtils.formatter.format(damage * 100) + "%").withStyle(getColorFromValue(damage))));
            if (posture != 1)
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.posture" + advance, Component.literal(RenderUtils.formatter.format(posture * 100) + "%").withStyle(getColorFromValue(posture))));
            if (!hit_self_command.isEmpty() || !hit_other_command.isEmpty() || !damage_other_command.isEmpty() || !damage_self_command.isEmpty()) {
                if (advanced) {
                    if (!hit_self_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.self_hit", Component.literal(hit_self_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    if (!hit_other_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.other_hit", Component.literal(hit_other_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    if (!damage_self_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.self_damage", Component.literal(damage_self_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    if (!damage_other_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.other_damage", Component.literal(damage_other_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                } else
                    sweepTip.append(Component.translatable("wardance.tooltip.sweep.command").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            sweepTip = sweepTip.withStyle(ChatFormatting.WHITE);
            return sweepTip;
        }

        public double getKnockback() {
            return knockback;
        }

        public double getDamageScale() {
            return damage_scale;
        }

        public double getPostureScale() {
            return posture_scale;
        }

        public boolean isCrit() {
            return crit;
        }

        public double getCritDamage() {
            return crit_damage;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SweepInfo other
                    && other.sweep == sweep
                    && other.sweep_scale == sweep_scale
                    && other.damage_scale == damage_scale
                    && other.posture_scale == posture_scale
                    && other.crit_damage == crit_damage
                    && other.crit == crit
                    && other.knockback == knockback
                    && other.hit_other_command.equals(hit_other_command)
                    && other.hit_self_command.equals(hit_self_command)
                    && other.damage_other_command.equals(damage_other_command)
                    && other.damage_self_command.equals(damage_self_command)
                    && other.sweep_base == sweep_base;
        }

        public SweepInfo clone() {
            SweepInfo ret = new SweepInfo(sweep, sweep_base, sweep_scale);
            ret.crit = crit;
            //ret.crit_conversion = crit_conversion;
            ret.crit_damage = crit_damage;
            ret.damage_scale = damage_scale;
            ret.knockback = knockback;
            ret.posture_scale = posture_scale;
            return ret;
        }

        public double getBase() {
            return sweep_base;
        }

        public double getScaling() {
            return sweep_scale;
        }

        public SWEEPTYPE getType() {
            return sweep;
        }

        public void write(FriendlyByteBuf f) {
            f.writeInt(sweep.ordinal());
            f.writeDouble(sweep_base);
            f.writeDouble(sweep_scale);
            f.writeDouble(knockback);
            f.writeDouble(damage_scale);
            f.writeDouble(posture_scale);
            f.writeBoolean(crit);
            f.writeDouble(crit_damage);
            f.writeUtf(hit_self_command);
            f.writeUtf(hit_other_command);
            f.writeUtf(damage_self_command);
            f.writeUtf(damage_other_command);
            //f.writeDouble(crit_conversion);
        }

        public void read(FriendlyByteBuf f) {
            sweep = SWEEPTYPE.values()[f.readInt()];
            sweep_base = f.readDouble();
            sweep_scale = f.readDouble();
            knockback = f.readDouble();
            damage_scale = f.readDouble();
            posture_scale = f.readDouble();
            crit = f.readBoolean();
            crit_damage = f.readDouble();
            hit_self_command = f.readUtf();
            hit_other_command = f.readUtf();
            damage_self_command = f.readUtf();
            damage_other_command = f.readUtf();
            //crit_conversion = f.readDouble();
        }

        public boolean performCommand(LivingEntity by, boolean self, boolean damage) {
            String command = self ? (damage ? damage_self_command : hit_self_command) : (damage ? damage_other_command : hit_other_command);
            Level level = by.level();
            if (!level.isClientSide) {
                MinecraftServer minecraftserver = level.getServer();
                if (!StringUtil.isNullOrEmpty(command)) {
                    try {
                        CommandSourceStack commandsourcestack = new CommandSourceStack(by, by.position(), by.getRotationVector(), level instanceof ServerLevel s ? s : null, 3, by.getName().getString(), by.getDisplayName(), level.getServer(), by).withSuppressedOutput();
                        minecraftserver.getCommands().performPrefixedCommand(commandsourcestack, command);
                    } catch (Throwable ignored) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public static class SweepAdapter implements JsonDeserializer<SweepInfo> {
        @Override
        public SweepInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) return null;
            JsonObject sub = json.getAsJsonObject();
            SweepInfo sweep = new SweepInfo(SWEEPTYPE.NONE, 0, 0);
            if (sub.has("sweep"))
                sweep.sweep = SWEEPTYPE.valueOf(sub.get("sweep").getAsString().toUpperCase(Locale.ROOT));
            if (!sub.has("sweep_base"))
                if (sweep.sweep == SWEEPTYPE.CONE) {
                    sweep.sweep_base = 30;
                } else {
                    sweep.sweep_base = 1;
                }
            else sweep.sweep_base = sub.get("sweep_base").getAsDouble();
            if (!sub.has("sweep_scale"))
                if (sweep.sweep == SWEEPTYPE.CONE) {
                    sweep.sweep_scale = 30;
                } else {
                    sweep.sweep_scale = 1.5;
                }
            else sweep.sweep_scale = sub.get("sweep_scale").getAsDouble();
            if (sub.has("damage_scale"))
                sweep.damage_scale = sub.get("damage_scale").getAsDouble();
            if (sub.has("posture_scale"))
                sweep.posture_scale = sub.get("posture_scale").getAsDouble();
            if (sub.has("knockback"))
                sweep.knockback = sub.get("knockback").getAsDouble();
            if (sub.has("crit"))
                sweep.crit = sub.get("crit").getAsBoolean();
            if (sub.has("crit_damage"))
                sweep.crit_damage = sub.get("crit_damage").getAsDouble();
            if (sub.has("hit_self_command"))
                sweep.hit_self_command = sub.get("hit_self_command").getAsString();
            if (sub.has("hit_other_command"))
                sweep.hit_other_command = sub.get("hit_other_command").getAsString();
            if (sub.has("damage_self_command"))
                sweep.damage_self_command = sub.get("damage_self_command").getAsString();
            if (sub.has("damage_other_command"))
                sweep.damage_other_command = sub.get("damage_other_command").getAsString();
            return sweep;
        }
    }
}
