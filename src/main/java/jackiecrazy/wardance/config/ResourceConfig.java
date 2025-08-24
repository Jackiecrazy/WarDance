package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourceConfig {
    public static final ResourceConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static double postureRegen;
    public static int postureCD;
    public static ThirdOption sleepingHealsDecay;

    static {
        final Pair<ResourceConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ResourceConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    //private final ForgeConfigSpec.IntValue _postureCap;
    private final ForgeConfigSpec.DoubleValue _postureRegen;
    private final ForgeConfigSpec.IntValue _postureCD;

    public ResourceConfig(ForgeConfigSpec.Builder b) {
        //master, resources, compat, stealth, items, misc
        //_postureCap = b.translation("wardance.config.qiG").comment("numeric hard cap on the amount of posture a mob can regenerate per second, for fairness.").defineInRange("posture regeneration cap", 100, 1, Integer.MAX_VALUE);
        _postureRegen = b.translation("wardance.config.spiritC").comment("Default percentage of max posture a mob can heal per second. This is capped.").defineInRange("posture regen percentage", 0.4, 0, Double.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Default number of ticks before a mob begins to regenerate posture again. Does nothing for players.").defineInRange("posture cooldown", 50, 0, Integer.MAX_VALUE);
    }

    private static void bake() {
        //qiGrace = CONFIG._postureCap.get();
        postureRegen = CONFIG._postureRegen.get();
        postureCD = CONFIG._postureCD.get();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            if(GeneralConfig.debug)
                WarDance.LOGGER.debug("loading combat config!");
            bake();
        }
    }

    public enum ThirdOption {
        TRUE,
        FALSE,
        FORCED
    }
}
