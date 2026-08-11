package jackiecrazy.wardance.config.weapon;

import com.google.gson.*;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BCBackupProvider extends SimpleJsonResourceReloadListener {
    public BCBackupProvider() {
        super(WeaponInteractions.GSON, "weapon_attributes");
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new BCBackupProvider());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        object.forEach((key, value) -> {
            @NotNull Optional<Holder<Item>> i = ForgeRegistries.ITEMS.getHolder(key);
            i.ifPresent(a->{
                if(a instanceof Holder.Reference<Item> b){
                    try {
                        JsonElement type = value.getAsJsonObject().get("parent");
                        if(type!=null&& type.isJsonPrimitive()){
                            String t =type.getAsString();
                            final List<TagKey<Item>> prevTags = new ArrayList<>(b.getTagKeys().toList());
                            WarDance.LOGGER.debug("given tag {} to BC-defined item {}", t, key);
                            prevTags.add(ItemTags.create(new ResourceLocation(t)));
                            b.bindTags(prevTags);
                        }
                    } catch (Exception ignored) {
                        WarDance.LOGGER.warn("attempted to adapt invalid tags from "+key.toString());
                    }
                }
            });
        });
    }
}