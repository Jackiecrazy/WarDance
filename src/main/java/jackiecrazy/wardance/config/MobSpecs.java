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
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class MobSpecs extends SimpleJsonResourceReloadListener {

    public static final MobInfo DEFAULT = new MobInfo();
    public static final TagKey<EntityType<?>> CANNOT_PARRY =TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "cannot_parry"));
    public static final TagKey<EntityType<?>> DESTROY_ON_PARRY =TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "destroy_on_parry"));
    public static final TagKey<EntityType<?>> TRIGGER_ON_PARRY =TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WarDance.MODID, "trigger_on_parry"));
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();
    public static HashMap<EntityType<?>, MobInfo> mobMap = new HashMap<>();

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new MobSpecs());
    }

    public MobSpecs() {
        super(GSON, "war_mob_stats");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager p_10794_, ProfilerFiller p_10795_) {
        mobMap.clear();
        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            if (GeneralConfig.debug)
                WarDance.LOGGER.debug("loading " + key);
            file.entrySet().forEach(entry -> {
                final String name = entry.getKey();
                ResourceLocation i = new ResourceLocation(name);
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(i);
                if (type == null) {
                    if (GeneralConfig.debug)
                        WarDance.LOGGER.debug(name + " is not a registered entity!");
                    return;
                }
                try {
                    mobMap.put(type, GSON.fromJson(entry.getValue(), MobInfo.class));
                } catch (Exception x) {
                    WarDance.LOGGER.error("malformed json under " + name + "!");
                    x.printStackTrace();
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
        private double auto_parry_multiplier = 1;
        private double parry_chance = 0;
        private boolean parry_omnidirectional = false;
        private boolean natural_shield = false;

        public double getBaseAttackPosture() {
            return base_attack_posture;
        }

        public double getItemPostureScaling() {
            return item_attack_posture_scaling;
        }

        public double getMaxPosture() {
            return base_max_posture;
        }

        public double getAutoParryMultiplier() {
            return auto_parry_multiplier;
        }

        public double getParryChance() {
            return parry_chance;
        }

        public boolean isOmnidirectional() {
            return parry_omnidirectional;
        }

        public boolean isShield() {
            return natural_shield;
        }

        public double getMaxPostureScaling() {
            return max_posture_scaling;
        }
    }
}
