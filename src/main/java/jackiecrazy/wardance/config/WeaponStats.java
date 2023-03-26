package jackiecrazy.wardance.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.Map;

public class WeaponStats extends SimpleJsonResourceReloadListener {
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public WeaponStats() {
        super(GSON, "war_stats");
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new WeaponStats());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        CombatUtils.updateItems(object, rm, profiler);
    }
}
