package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.ClientEvents;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

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
    private final ForgeConfigSpec.IntValue _spiritX;
    private final ForgeConfigSpec.IntValue _spiritY;
    private final ForgeConfigSpec.IntValue _yourPostureY;
    private final ForgeConfigSpec.IntValue _yourPostureX;
    private final ForgeConfigSpec.IntValue _theirPostureY;
    private final ForgeConfigSpec.IntValue _theirPostureX;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;
    public static boolean displayEnemyPosture;
    public static int mightX;
    public static int mightY;
    public static int spiritX;
    public static int spiritY;
    public static int comboX;
    public static int comboY;
    public static int yourPostureX;
    public static int yourPostureY;
    public static int theirPostureX;
    public static int theirPostureY;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        _displayEnemyPosture = b.translation("wardance.config.displayPosture").comment("whether to display the posture of the entity looked at").define("displayEnemyPosture", true);
        b.push("might");
        _qiX = b.translation("wardance.config.mightX").comment("might HUD x position from the left quartile of the screen").defineInRange("might X", -32, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        _qiY = b.translation("wardance.config.mightY").comment("might HUD y position from the bottom of the screen").defineInRange("might Y", 5, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("spirit");
        _spiritX = b.translation("wardance.config.spiritX").comment("spirit HUD x position from the right quartile of the screen").defineInRange("spirit X", 16, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        _spiritY = b.translation("wardance.config.spiritY").comment("spirit HUD y position from the bottom of the screen").defineInRange("spirit Y", 5, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("combo");
        _comboX = b.translation("wardance.config.comboX").comment("x position of the combo HUD's center from the left of the screen").defineInRange("combo X", -40, -Integer.MAX_VALUE, 0);
        _comboY = b.translation("wardance.config.comboY").comment("x position of the combo HUD's center the center of the screen").defineInRange("combo Y", -32, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        b.pop();
        b.push("yours");
        _yourPostureX = b.translation("wardance.config.uPosX").comment("your posture bar X, defined as deviation from the center").defineInRange("yourPostureX", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        _yourPostureY = b.translation("wardance.config.uPosY").comment("your posture bar Y, defined from the middle of the screen").defineInRange("yourPostureY", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        b.pop();
        b.push("theirs");
        _theirPostureX = b.translation("wardance.config.tPosX").comment("enemy posture bar X, defined as deviation from the center").defineInRange("theirPostureX", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        _theirPostureY = b.translation("wardance.config.tPosY").comment("enemy posture bar Y, defined from the top of the screen").defineInRange("theirPostureY", 20, 0, Integer.MAX_VALUE);
        b.pop();
        _customPosture = b.translation("wardance.config.postureMobs").comment("whether a mob is rotated when it is staggered.").defineList("mob stagger rotation", Lists.newArrayList("example:dragon, false", "example:ghast, true"), String.class::isInstance);

    }

    public static void bake() {
        displayEnemyPosture = CONFIG._displayEnemyPosture.get();
        mightX = CONFIG._qiX.get();
        mightY = CONFIG._qiY.get();
        spiritX = CONFIG._spiritX.get();
        spiritY = CONFIG._spiritY.get();
        comboX = CONFIG._comboX.get();
        comboY = CONFIG._comboY.get();
        yourPostureX = CONFIG._yourPostureX.get();
        yourPostureY = CONFIG._yourPostureY.get();
        theirPostureX = CONFIG._theirPostureX.get();
        theirPostureY = CONFIG._theirPostureY.get();
        ClientEvents.updateList(CONFIG._customPosture.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            WarDance.LOGGER.debug("loading client config!");
            bake();
        }
    }
}
