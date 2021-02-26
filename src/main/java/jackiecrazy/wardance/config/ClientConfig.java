package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {
    public static final ClientConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.BooleanValue _displayEnemyPosture;
    private final ForgeConfigSpec.IntValue _qiX;
    private final ForgeConfigSpec.IntValue _qiY;
    private final ForgeConfigSpec.IntValue _comboX;
    private final ForgeConfigSpec.IntValue _comboY;
    private final ForgeConfigSpec.IntValue _yourPostureY;
    private final ForgeConfigSpec.IntValue _yourPostureX;
    private final ForgeConfigSpec.IntValue _theirPostureY;
    private final ForgeConfigSpec.IntValue _theirPostureX;
    public static boolean displayEnemyPosture;
    public static int qiX;
    public static int qiY;
    public static int comboX;
    public static int comboY;
    public static int yourPostureX;
    public static int yourPostureY;
    public static int theirPostureX;
    public static int theirPostureY;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        _displayEnemyPosture = b.translation("wardance.config.displayPosture").comment("whether to display the posture of the entity looked at").define("displayEnemyPosture", true);
        b.push("qi");
        _qiX = b.translation("wardance.config.qiX").comment("qi HUD x position").defineInRange("qiX", 0, 0, Integer.MAX_VALUE);
        _qiY = b.translation("wardance.config.qiY").comment("qi HUD y position").defineInRange("qiY", 0, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("combo");
        _comboX = b.translation("wardance.config.comboX").comment("combo HUD x position from the left of the screen").defineInRange("combo X", -5, -Integer.MAX_VALUE, 0);
        _comboY = b.translation("wardance.config.comboY").comment("combo HUD y position from the center of the screen").defineInRange("combo Y", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        b.pop();
        b.push("yours");
        _yourPostureX = b.translation("wardance.config.uPosX").comment("your posture bar X, defined as deviation from the center").defineInRange("yourPostureX", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        _yourPostureY = b.translation("wardance.config.uPosY").comment("your posture bar Y, defined from the bottom of the screen").defineInRange("yourPostureY", 57, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("theirs");
        _theirPostureX = b.translation("wardance.config.tPosX").comment("enemy posture bar X, defined as deviation from the center").defineInRange("theirPostureX", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        _theirPostureY = b.translation("wardance.config.tPosY").comment("enemy posture bar Y, defined from the top of the screen").defineInRange("theirPostureY", 20, 0, Integer.MAX_VALUE);
        b.pop();
    }

    public static void bake() {
        displayEnemyPosture = CONFIG._displayEnemyPosture.get();
        qiX = CONFIG._qiX.get();
        qiY = CONFIG._qiY.get();
        comboX = CONFIG._comboX.get();
        comboY = CONFIG._comboY.get();
        yourPostureX = CONFIG._yourPostureX.get();
        yourPostureY = CONFIG._yourPostureY.get();
        theirPostureX = CONFIG._theirPostureX.get();
        theirPostureY = CONFIG._theirPostureY.get();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            WarDance.LOGGER.debug("loading client config!");
            bake();
        }
    }
}
