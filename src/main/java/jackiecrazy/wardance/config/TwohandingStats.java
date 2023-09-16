package jackiecrazy.wardance.config;

import com.google.gson.*;
import jackiecrazy.wardance.WarDance;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class TwohandingStats extends SimpleJsonResourceReloadListener {
    public static final UUID uuid = UUID.fromString("a516026a-bee2-4014-bcb6-b6a5776663de");
    public static final UUID offhanduuid = UUID.fromString("a516026a-bee2-5125-bcb6-b6a5776663de");
    public static final Map<Item, Tuple<Map<Attribute, List<AttributeModifier>>, Map<Attribute, List<AttributeModifier>>>> MAP = new HashMap<>();
    public static final Map<TagKey<Item>, Tuple<Map<Attribute, List<AttributeModifier>>, Map<Attribute, List<AttributeModifier>>>> ARCHETYPES = new HashMap<>();
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public TwohandingStats() {
        super(GSON, "war_twohanding");
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
                }
                ResourceLocation i = new ResourceLocation(name);
                item = ForgeRegistries.ITEMS.getValue(i);
                if (item == null || item == Items.AIR) {
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
                            ARCHETYPES.putIfAbsent(tag, new Tuple<>(new HashMap<>(), new HashMap<>()));
                            Tuple<Map<Attribute, List<AttributeModifier>>, Map<Attribute, List<AttributeModifier>>> sub = ARCHETYPES.get(tag);
                            sub.getA().putIfAbsent(a, new ArrayList<>());
                            sub.getA().get(a).add(main);
                            sub.getB().putIfAbsent(a, new ArrayList<>());
                            sub.getB().get(a).add(off);
                            ARCHETYPES.put(tag, sub);
                        } else {
                            MAP.putIfAbsent(item, new Tuple<>(new HashMap<>(), new HashMap<>()));
                            Tuple<Map<Attribute, List<AttributeModifier>>, Map<Attribute, List<AttributeModifier>>> sub = MAP.get(item);
                            sub.getA().putIfAbsent(a, new ArrayList<>());
                            sub.getA().get(a).add(main);
                            sub.getB().putIfAbsent(a, new ArrayList<>());
                            sub.getB().get(a).add(off);
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