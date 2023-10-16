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
public class SkillConfig {
    public static final SkillConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static boolean sifu;
    private final ForgeConfigSpec.BooleanValue _wizmode;

    public SkillConfig(ForgeConfigSpec.Builder b) {
        this._wizmode = b.translation("wardance.config.wizmode").comment("When true, you unlock all skills by default. If toggled halfway through a world, your unlocked skills will become locked and vice versa. Recommended for experienced players only.").define("Grandmaster Mode", false);
    }

    private static void bake() {
        sifu = CONFIG._wizmode.get();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }

    }

    static {
        Pair<SkillConfig, ForgeConfigSpec> specPair = (new ForgeConfigSpec.Builder()).configure(SkillConfig::new);
        CONFIG = (SkillConfig) specPair.getLeft();
        CONFIG_SPEC = (ForgeConfigSpec)specPair.getRight();
    }
}
