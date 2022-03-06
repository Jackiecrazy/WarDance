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
    public final DisplayData might, mightNumber, spirit, spiritNumber, combo, playerAfflict, enemyAfflict;
    public final PostureData playerPosture, enemyPosture;
    public static int spiritColor;
    public static int mightColor;
    public static int autoCombat;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.IntValue _autoCombat;
    private final ForgeConfigSpec.ConfigValue<String> _mightColor;
    private final ForgeConfigSpec.ConfigValue<String> _spiritColor;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        b.push("convenience");
        _autoCombat = b.translation("wardance.config.autoCombat").comment("combat mode will be automatically engaged once you attack or get attacked by an entity if it is not already on, for this number of ticks before turning itself off. Set to 0 to disable this feature.").defineInRange("auto combat mode", 260, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("might");
        might = new DisplayData(b, "might", AnchorPoint.BOTTOMLEFT, 64, -16);
        mightNumber = new DisplayData(b, "might number", AnchorPoint.BOTTOMLEFT, 64, -38);
        _mightColor = b.translation("wardance.config.mightC").comment("might color in hexadecimal").define("might color", "ccac00");
        b.pop();
        b.push("spirit");
        spirit = new DisplayData(b, "spirit", AnchorPoint.BOTTOMRIGHT, -64, -16);
        spiritNumber = new DisplayData(b, "spirit number", AnchorPoint.BOTTOMRIGHT, -64, -38);
        _spiritColor = b.translation("wardance.config.spiritC").comment("spirit color in hexadecimal").define("spirit color", "00e3e3");
        b.pop();
        b.push("combo");
        combo = new DisplayData(b, "combo", AnchorPoint.MIDDLERIGHT, -40, -32);
        b.pop();
        b.push("player posture");
        playerPosture = new PostureData(b, "player posture", AnchorPoint.BOTTOMCENTER, 0, -57);
        b.pop();
        b.push("enemy posture");
        enemyPosture = new PostureData(b, "target posture", AnchorPoint.TOPCENTER, 0, 20);
        b.pop();
        b.push("your afflictions");
        playerAfflict = new DisplayData(b, "your afflictions", AnchorPoint.CROSSHAIR, 0, 18);
        b.pop();
        b.push("target afflictions");
        enemyAfflict = new DisplayData(b, "target afflictions", AnchorPoint.CROSSHAIR, 0, -18);
        b.pop();
        _customPosture = b.translation("wardance.config.postureMobs").comment("whether a mob is rotated when it is staggered.").defineList("mob stagger rotation", Lists.newArrayList("example:dragon, false", "example:ghast, true"), String.class::isInstance);
    }

    public static void bake() {
        CONFIG.might.bake();
        CONFIG.spiritNumber.bake();
        CONFIG.spirit.bake();
        CONFIG.mightNumber.bake();
        CONFIG.combo.bake();
        CONFIG.playerPosture.bake();
        CONFIG.enemyAfflict.bake();
        CONFIG.enemyPosture.bake();
        CONFIG.playerAfflict.bake();
        spiritColor = Integer.parseInt(CONFIG._spiritColor.get(), 16);
        mightColor = Integer.parseInt(CONFIG._mightColor.get(), 16);
        autoCombat = CONFIG._autoCombat.get();
        ClientEvents.updateList(CONFIG._customPosture.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            if(GeneralConfig.debug)
            WarDance.LOGGER.debug("loading client config!");
            bake();
        }
    }

    public enum AnchorPoint {
        TOPLEFT,
        TOPCENTER,
        TOPRIGHT,
        MIDDLELEFT,
        CROSSHAIR,
        MIDDLERIGHT,
        BOTTOMLEFT,
        BOTTOMCENTER,
        BOTTOMRIGHT
    }

    public static class DisplayData {
        private final ForgeConfigSpec.EnumValue<AnchorPoint> _anchor;
        private final ForgeConfigSpec.IntValue _numberX;
        private final ForgeConfigSpec.IntValue _numberY;
        private final ForgeConfigSpec.BooleanValue _display;
        public AnchorPoint anchorPoint;
        public int numberX;
        public int numberY;
        public boolean enabled;

        private DisplayData(ForgeConfigSpec.Builder b, String s, AnchorPoint ap, int defX, int defY) {
            _display = b.translation("wardance.config." + s + "enabled").comment("enable displaying this feature").define("enable " + s, true);
            _anchor = b.translation("wardance.config." + s + "anchor").comment("the point from which offsets will calculate").defineEnum(s + " anchor point", ap);
            _numberX = b.translation("wardance.config." + s + "X").comment("where the center of the HUD element should be in relation to the anchor point").defineInRange(s + " x offset", defX, -Integer.MAX_VALUE, Integer.MAX_VALUE);
            _numberY = b.translation("wardance.config." + s + "Y").comment("where the center of the HUD element should be in relation to the anchor point").defineInRange(s + " y offset", defY, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        protected void bake() {
            anchorPoint = _anchor.get();
            numberX = _numberX.get();
            numberY = _numberY.get();
            enabled = _display.get();
        }
    }

    public static class PostureData extends DisplayData{
        public BarType bar;
        private final ForgeConfigSpec.EnumValue<BarType> _bar;
        private PostureData(ForgeConfigSpec.Builder b, String s, AnchorPoint ap, int defX, int defY) {
            super(b, s, ap, defX, defY);
            _bar=b.translation("wardance.config."+s+"Type").comment("Determine which type of posture bar will be rendered. Valid values are 'classic' (ugly), 'amo' (minimalist), and 'darkmega' (default).").defineEnum(s+" style", BarType.DARKMEGA);
        }

        @Override
        protected void bake() {
            super.bake();
            bar=_bar.get();
        }
    }

    public static enum BarType{
        CLASSIC,
        AMO,
        DARKMEGA
    }
}
