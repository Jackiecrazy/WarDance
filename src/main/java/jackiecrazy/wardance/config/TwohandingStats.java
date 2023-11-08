package jackiecrazy.wardance.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.networking.sync.TwoHandItemDataPacket;
import jackiecrazy.wardance.networking.sync.TwoHandTagDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class TwohandingStats extends SimpleJsonResourceReloadListener {
    public static final UUID uuid = UUID.fromString("a516026a-bee2-4014-bcb6-b6a5776663de");
    public static final UUID offhanduuid = UUID.fromString("a516026a-bee2-5125-bcb6-b6a5776663de");
    public static final Map<Item, Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> MAP = new HashMap<>();
    public static final Map<TagKey<Item>,  Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> ARCHETYPES = new HashMap<>();
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public TwohandingStats() {
        super(GSON, "war_twohanding");
    }

    public static void sendItemData(ServerPlayer p) {
        //duplicated removed automatically
        Set<String> paths = MAP.keySet().stream().map(a -> ForgeRegistries.ITEMS.getKey(a).getNamespace()).collect(Collectors.toSet());
        for (String namespace : paths)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new TwoHandItemDataPacket(Maps.filterEntries(MAP, a -> ForgeRegistries.ITEMS.getKey(a.getKey()).getNamespace().equals(namespace))));
        //CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncItemDataPacket(new HashMap<>(combatList)));
        CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new TwoHandTagDataPacket(ARCHETYPES));
    }

    public static void clientWeaponOverride(Map<Item, Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> server) {
        MAP.putAll(server);
    }

    public static void clientTagOverride(Map<TagKey<Item>, Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> server) {
        ARCHETYPES.putAll(server);
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new TwohandingStats());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        MAP.clear();
        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            file.entrySet().forEach(entry -> {
                boolean isTag = false;
                String name = entry.getKey();
                Item item = null;
                if (name.startsWith("#")) {//tag
                    isTag = true;
                    name = name.substring(1);
                    if (!name.contains(":"))
                        name = "wardance:" + name;
                }
                ResourceLocation i = new ResourceLocation(name);
                item = ForgeRegistries.ITEMS.getValue(i);
                if (!isTag && (item == null || item == Items.AIR)) {
                    //Attributizer.LOGGER.debug(name + " is not a registered item!");
                    return;
                }
                JsonArray array = entry.getValue().getAsJsonArray();
                for (JsonElement e : array) {
                    try {
                        JsonObject obj = e.getAsJsonObject();
                        final ResourceLocation attribute = new ResourceLocation(obj.get("attribute").getAsString());
                        Attribute a = ForgeRegistries.ATTRIBUTES.getValue(attribute);
                        if (a == null) {
                            WarDance.LOGGER.debug(attribute + " is not a registered attribute!");
                            continue;
                        }

                        double modify = obj.get("modify").getAsDouble();
                        String type = obj.get("operation").getAsString();
                        UUID uid;
                        try {
                            final String u = obj.get("uuid").getAsString();
                            uid = UUID.fromString(u);
                        } catch (Exception ignored) {
                            //have to grab the uuid haiyaaa
                            uid = uuid;
                        }
                        UUID offuid;
                        try {
                            final String u = obj.get("offuuid").getAsString();
                            offuid = UUID.fromString(u);
                        } catch (Exception ignored) {
                            //have to grab the uuid haiyaaa
                            offuid = offhanduuid;
                        }

                        AttributeModifier main = new AttributeModifier(uid, "two-handing bonus", modify, AttributeModifier.Operation.valueOf(type));
                        AttributeModifier off = new AttributeModifier(offuid, "two-handing bonus", modify, AttributeModifier.Operation.valueOf(type));
                        if (isTag) {
                            final TagKey<Item> tag = ItemTags.create(i);
                            ARCHETYPES.putIfAbsent(tag, new HashMap<>());
                            final Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>> sub = ARCHETYPES.get(tag);
                            sub.putIfAbsent(a, new Tuple<>(new ArrayList<>(), new ArrayList<>()));
                            sub.get(a).getA().add(main);
                            sub.get(a).getB().add(off);
                            ARCHETYPES.put(tag, sub);
                        } else {
                            MAP.putIfAbsent(item, new HashMap<>());
                            final Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>> sub = MAP.get(item);
                            sub.putIfAbsent(a, new Tuple<>(new ArrayList<>(), new ArrayList<>()));
                            sub.get(a).getA().add(main);
                            sub.get(a).getB().add(off);
                            MAP.put(item, sub);
                        }
                    } catch (Exception x) {
                        WarDance.LOGGER.error("incomplete or malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                }
            });
        });
    }
}