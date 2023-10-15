package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * credits to Shadows-of-Fire
 */
@Mod.EventBusSubscriber(
        modid = "wardance",
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class LootConfig {
    public static final LootConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final String[] DEFAULT = new String[]{
            "minecraft:chests.*|0.15",
            ".*chests.*|0.15",
            "twilightforest:structures.*|0.12"
    };
    public static ArrayList<LootPatternMatcher> scrollChances = new ArrayList<>(), styleChances = new ArrayList<>();

    static {
        Pair<LootConfig, ForgeConfigSpec> specPair = (new ForgeConfigSpec.Builder()).configure(LootConfig::new);
        CONFIG = (LootConfig) specPair.getLeft();
        CONFIG_SPEC = (ForgeConfigSpec) specPair.getRight();
    }

    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _scroll;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _style;

    public LootConfig(ForgeConfigSpec.Builder b) {
        _scroll = b.translation("wardance.config.lootTables").comment("Project: War Dance will inject skill scroll drops into these locations. \nFormat is loot table | chance").defineList("projectile parry rules", Arrays.asList(DEFAULT), String.class::isInstance);
        _style = b.translation("wardance.config.lootTables").comment("Project: War Dance will inject style scroll drops into these locations. \nFormat is loot table | chance").defineList("projectile parry rules", Arrays.asList(DEFAULT), String.class::isInstance);
    }

    private static void bake() {
        for (String s : CONFIG._scroll.get()) {
            try {
                scrollChances.add(LootPatternMatcher.parse(s));
            } catch (Exception e) {
                WarDance.LOGGER.error("Loot config: invalid entry " + s + "!");
            }
        }
        for (String s : CONFIG._style.get()) {
            try {
                styleChances.add(LootPatternMatcher.parse(s));
            } catch (Exception e) {
                WarDance.LOGGER.error("Loot config: invalid entry " + s + "!");
            }
        }
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }

    }

    public record LootPatternMatcher(@Nullable String domain, Pattern pathRegex, float chance) {

        public static LootPatternMatcher parse(String s) throws Exception {
            int pipe = s.lastIndexOf('|');
            int colon = s.indexOf(':');
            float chance = Float.parseFloat(s.substring(pipe + 1));
            String domain = colon == -1 ? null : s.substring(0, colon);
            Pattern pattern = Pattern.compile(s.substring(colon + 1, pipe));
            return new LootPatternMatcher(domain, pattern, chance);
        }

        public boolean matches(ResourceLocation id) {
            return (this.domain == null || this.domain.equals(id.getNamespace())) && this.pathRegex.matcher(id.getPath()).matches();
        }
    }
}