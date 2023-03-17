package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourceConfig {
    public static final ResourceConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static int qiGrace;
    public static int spiritCD;
    public static int postureCD;
    public static ThirdOption sleepingHealsDecay;

    static {
        final Pair<ResourceConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ResourceConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.IntValue _qiGrace;
    private final ForgeConfigSpec.IntValue _spiritCD;
    private final ForgeConfigSpec.IntValue _postureCD;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;

    public ResourceConfig(ForgeConfigSpec.Builder b) {
        //master, resources, compat, stealth, items, misc
        _qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining might during which it will not decrease").defineInRange("might grace period", 100, 1, Integer.MAX_VALUE);
        _spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spirit cooldown", 80, 1, Integer.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("posture cooldown", 20, 1, Integer.MAX_VALUE);
        _customPosture = b.translation("wardance.config.postureMobs").comment("Here you can define custom max posture for mobs. Armor adds to this independently.").defineList("custom mob posture", Lists.newArrayList("example:dragon, 100", "example:ghast, 8"), String.class::isInstance);
    }

    private static void bake() {
        qiGrace = CONFIG._qiGrace.get();
        spiritCD = CONFIG._spiritCD.get();
        postureCD = CONFIG._postureCD.get();
        CombatUtils.updateMobPosture(CONFIG._customPosture.get());
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
