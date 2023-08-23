package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.ClientEvents;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    public static final ClientConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static int spiritColor;
    public static int mightColor;
    public static int autoCombat;
    public static boolean hide;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public final DisplayConfigUtils.DisplayData mightBar, mightNumber, spiritBar, spiritNumber, combo, playerAfflict, enemyAfflict, skillCD, stealth;
    public final PostureData playerPosture, enemyPosture;
    public final CircleData mightCircle, spiritCircle;
    private final ForgeConfigSpec.IntValue _autoCombat;
    private final ForgeConfigSpec.BooleanValue _hidexp;
    private final ForgeConfigSpec.ConfigValue<String> _mightColor;
    private final ForgeConfigSpec.ConfigValue<String> _spiritColor;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        b.push("convenience");
        _autoCombat = b.translation("wardance.config.autoCombat").comment("combat mode will be automatically engaged once you attack or get attacked by an entity if it is not already on, for this number of ticks before turning itself off. Set to 0 to disable this feature.").defineInRange("auto combat mode", 0, 0, Integer.MAX_VALUE);
        _hidexp = b.translation("wardance.config.hidexp").comment("hide the exp bar while combat mode is on to make space for the might and spirit bars.").define("combat mode hides exp", true);
        b.pop();
        b.push("might");
        mightCircle = new CircleData(b, "might circle", DisplayConfigUtils.AnchorPoint.BOTTOMLEFT, 32, -16);
        mightBar = new DisplayConfigUtils.DisplayData(b, "might bar", DisplayConfigUtils.AnchorPoint.BOTTOMCENTER, -91, -29);
        mightNumber = new CircleData(b, "might number", DisplayConfigUtils.AnchorPoint.BOTTOMLEFT, 32, -38);
        _mightColor = b.translation("wardance.config.mightC").comment("might color in hexadecimal").define("might color", "ccac00");
        b.pop();
        b.push("spirit");
        spiritCircle = new CircleData(b, "spirit circle", DisplayConfigUtils.AnchorPoint.BOTTOMLEFT, 80, -16);
        spiritBar = new DisplayConfigUtils.DisplayData(b, "spirit bar", DisplayConfigUtils.AnchorPoint.BOTTOMCENTER, 4, -29);
        spiritNumber = new CircleData(b, "spirit number", DisplayConfigUtils.AnchorPoint.BOTTOMLEFT, 80, -38);
        _spiritColor = b.translation("wardance.config.spiritC").comment("spirit color in hexadecimal").define("spirit color", "00e3e3");
        b.pop();
        b.push("combo");
        combo = new DisplayConfigUtils.DisplayData(b, "combo", DisplayConfigUtils.AnchorPoint.MIDDLERIGHT, -40, -32);
        b.pop();
        b.push("player posture");
        playerPosture = new PostureData(b, "player posture", DisplayConfigUtils.AnchorPoint.BOTTOMCENTER, 0, -57);
        b.pop();
        b.push("enemy posture");
        enemyPosture = new PostureData(b, "target posture", DisplayConfigUtils.AnchorPoint.TOPCENTER, 0, 20);
        b.pop();
        b.push("your marks");
        playerAfflict = new DisplayConfigUtils.DisplayData(b, "your marks", DisplayConfigUtils.AnchorPoint.CROSSHAIR, 0, 18);
        b.pop();
        b.push("target marks");
        enemyAfflict = new DisplayConfigUtils.DisplayData(b, "target marks", DisplayConfigUtils.AnchorPoint.CROSSHAIR, 0, -18);
        b.pop();
        b.push("skill cooldown");
        skillCD = new DisplayConfigUtils.DisplayData(b, "skill cooldown", DisplayConfigUtils.AnchorPoint.BOTTOMRIGHT, -54, -36);
        b.pop();
        b.push("stealth");
        stealth = new DisplayConfigUtils.DisplayData(b, "stealth", DisplayConfigUtils.AnchorPoint.CROSSHAIR, 0, 0);
        b.pop();
        _customPosture = b.translation("wardance.config.postureMobs").comment("whether a mob is rotated when it is staggered.").defineList("mob stagger rotation", Lists.newArrayList("example:dragon, false", "example:ghast, true"), String.class::isInstance);
    }

    public static void bake() {
        CONFIG.mightBar.bake();
        CONFIG.mightCircle.bake();
        CONFIG.spiritNumber.bake();
        CONFIG.spiritBar.bake();
        CONFIG.spiritCircle.bake();
        CONFIG.mightNumber.bake();
        CONFIG.combo.bake();
        CONFIG.playerPosture.bake();
        CONFIG.enemyAfflict.bake();
        CONFIG.enemyPosture.bake();
        CONFIG.playerAfflict.bake();
        CONFIG.skillCD.bake();
        CONFIG.stealth.bake();
        hide = CONFIG._hidexp.get();
        spiritColor = Integer.parseInt(CONFIG._spiritColor.get(), 16);
        mightColor = Integer.parseInt(CONFIG._mightColor.get(), 16);
        autoCombat = CONFIG._autoCombat.get();
        ClientEvents.updateList(CONFIG._customPosture.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            if (GeneralConfig.debug)
                WarDance.LOGGER.debug("loading client config!");
            bake();
        }
    }

    public static enum BarType {
        CLASSIC,
        AMO,
        DARKMEGA,
        NEWDARK
    }

    public static class PostureData extends DisplayConfigUtils.DisplayData {
        private final ForgeConfigSpec.EnumValue<BarType> _bar;
        public BarType bar;

        private PostureData(ForgeConfigSpec.Builder b, String s, DisplayConfigUtils.AnchorPoint ap, int defX, int defY) {
            super(b, s, ap, defX, defY);
            _bar = b.translation("wardance.config." + s + "Type").comment("Determine which type of posture bar will be rendered. Valid values are 'classic' (ugly), 'amo' (minimalist), 'darkmega' (old), and 'newdark' (default).").defineEnum(s + " style", BarType.NEWDARK);
        }

        @Override
        public void bake() {
            super.bake();
            bar = _bar.get();
        }
    }

    public static class CircleData extends DisplayConfigUtils.DisplayData {
        private final ForgeConfigSpec.BooleanValue _display;
        public boolean enabled;

        private CircleData(ForgeConfigSpec.Builder b, String s, DisplayConfigUtils.AnchorPoint ap, int defX, int defY) {
            super(b, s, ap, defX, defY);
            _display = b.translation("footwork.config." + s + "enabled").comment("enable displaying this feature").define("enable " + s, false);
        }

        @Override
        public void bake() {
            super.bake();
            enabled = _display.get();
        }
    }
}
