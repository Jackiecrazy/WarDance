//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package jackiecrazy.wardance.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(
        modid = "wardance",
        bus = Bus.MOD
)
public class StealthConfig {
    public static final StealthConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static boolean ignore;
    private final ForgeConfigSpec.BooleanValue _ignore;

    public StealthConfig(ForgeConfigSpec.Builder b) {
        this._ignore = b.translation("wardance.config.ignore").comment("if you have Cloak and Dagger installed, whether unaware stabs ignore parry and evasion.").define("unaware stab defense ignore", true);
    }

    private static void bake() {
        ignore = CONFIG._ignore.get();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }

    }

    static {
        Pair<StealthConfig, ForgeConfigSpec> specPair = (new ForgeConfigSpec.Builder()).configure(StealthConfig::new);
        CONFIG = (StealthConfig) specPair.getLeft();
        CONFIG_SPEC = (ForgeConfigSpec)specPair.getRight();
    }
}
