package jackiecrazy.wardance.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jackiecrazy.wardance.WarDance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class MobSpecs extends SimpleJsonResourceReloadListener {

    public static final MobInfo DEFAULT = new MobInfo();
    public static final TagKey<EntityType<?>> CANNOT_BLOCK = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "cannot_parry"));
    public static final TagKey<EntityType<?>> DESTROY_ON_PARRY = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "destroy_on_parry"));
    public static final TagKey<EntityType<?>> TRIGGER_ON_PARRY = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "trigger_on_parry"));
    public static final TagKey<EntityType<?>> IGNORED_BY_SWEEP = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "ignored_by_sweep"));
    public static final TagKey<EntityType<?>> NO_DARKTIDE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "no_darktide"));
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();
    private static HashMap<EntityType<?>, MobInfo> mobMap = new HashMap<>();
    private static HashMap<TagKey<EntityType<?>>, MobInfo> mobTagMap = new HashMap<>();

    public MobSpecs() {
        super(GSON, "war_mob_stats");
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new MobSpecs());
    }

    public static MobInfo getOrDefault(Entity ent){
        MobInfo ret = getMobInfo(ent);
        if(ret==null)return DEFAULT;
        return ret;
    }

    public static MobInfo getMobInfo(Entity ent) {
        if (ent == null) return null;
        EntityType<?> type = ent.getType();
        if (mobMap.containsKey(type)) return mobMap.get(type);
        for (TagKey<EntityType<?>> tag : mobTagMap.keySet()) {
            if (type.is(tag)) {
                mobMap.put(type, mobTagMap.get(tag));
                return mobMap.get(type);
            }
        }
        return null;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager p_10794_, ProfilerFiller p_10795_) {
        mobMap.clear();
        mobTagMap.clear();
        DEFAULT.posture_regeneration_cooldown = ResourceConfig.postureCD;
        DEFAULT.posture_regeneration_speed = ResourceConfig.postureRegen;
        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            if (GeneralConfig.debug)
                WarDance.LOGGER.debug("loading " + key);
            file.entrySet().forEach(entry -> {
                boolean tag = false;
                String name = entry.getKey();
                if (name.startsWith("#")) {
                    name = name.substring(1);
                    tag = true;
                }
                ResourceLocation i = new ResourceLocation(name);
                if (tag) {
                    try {
                        mobTagMap.put(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(name)), GSON.fromJson(entry.getValue(), MobInfo.class));
                    } catch (Exception x) {
                        WarDance.LOGGER.error("malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                } else {
                    try {
                        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(i);
                        if (type == null) {
                            if (GeneralConfig.debug)
                                WarDance.LOGGER.debug(name + " is not a registered entity!");
                            return;
                        }
                        mobMap.put(type, GSON.fromJson(entry.getValue(), MobInfo.class));
                    } catch (Exception x) {
                        WarDance.LOGGER.error("malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                }
            });
        });

    }

    @SuppressWarnings("all")
    public static class MobInfo {
        //here be default values
        private double base_max_posture = -1;
        private double max_posture_scaling = 1;
        private double base_attack_posture = -1;
        private double item_attack_posture_scaling = 1;
        private double posture_regeneration_speed = 0.4;
        private int posture_regeneration_cooldown = 40;
        private double auto_block_multiplier = 1;
        private double block_chance = 0;
        private boolean block_omnidirectional = false;
        private boolean natural_shield = false;

        public int getPostureRegenerationCooldown() {
            return posture_regeneration_cooldown;
        }

        public double getPostureRegenerationSpeed() {
            return posture_regeneration_speed;
        }

        public double getBaseAttackPosture() {
            return base_attack_posture;
        }

        public double getItemPostureScaling() {
            return item_attack_posture_scaling;
        }

        public double getMaxPosture() {
            return base_max_posture;
        }

        public double getBlockMult() {
            return auto_block_multiplier;
        }

        public double getBlockChance() {
            return block_chance;
        }

        public boolean isOmnidirectional() {
            return block_omnidirectional;
        }

        public boolean isShield() {
            return natural_shield;
        }

        public double getMaxPostureScaling() {
            return max_posture_scaling;
        }
    }
}
