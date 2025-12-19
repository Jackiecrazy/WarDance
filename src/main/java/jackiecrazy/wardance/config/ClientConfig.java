package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.RenderEvents;
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
    public static int adrenalineColor;
    public static int autoCombat;
    public static boolean hide;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public final DisplayConfigUtils.DisplayData adrenalineBar, adrenalineNumber, spiritBar, spiritNumber, combo, playerAfflict, enemyAfflict, skillCD, stealth;
    public final DisplayConfigUtils.DisplayData playerPosture, enemyPosture;
    public final CircleData adrenalineCircle, spiritCircle;
    private final ForgeConfigSpec.IntValue _autoCombat;
    private final ForgeConfigSpec.BooleanValue _hidexp;
    private final ForgeConfigSpec.ConfigValue<String> _adrenalineColor;
    private final ForgeConfigSpec.ConfigValue<String> _spiritColor;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;
    private final ForgeConfigSpec.EnumValue<ControlScheme> _ctrl;
    public static ControlScheme controlScheme;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        b.push("convenience");
        _autoCombat = b.translation("wardance.config.autoCombat").comment("combat mode will be automatically engaged once you attack or get attacked by an entity if it is not already on, for this number of ticks before turning itself off. Set to 0 to disable this feature.").defineInRange("auto combat mode", 0, 0, Integer.MAX_VALUE);
        _hidexp = b.translation("wardance.config.hidexp").comment("hide the exp bar while combat mode is on to make space for the adrenaline and spirit bars.").define("combat mode hides exp", true);
        _ctrl = b.translation("wardance.config.ControlType").comment("Change your control scheme in combat mode. Valid values are \nCLASSIC: right click defaults to main hand, or offhand if main hand has no right click action, and finally offhand attack. Holding the evoke key blocks main hand right clicking. Good for power users or complex weapons.\n DUAL: left click corresponds to main hand, and right click to offhand. Hold input briefly or hold evoke key to right click with the hand. Recommended for new users or weapons without complex right click behavior.").defineEnum("control scheme", ControlScheme.DUAL);
        b.pop();
        b.push("adrenaline");
        adrenalineCircle = new CircleData(b, "adrenaline circle", DisplayConfigUtils.AnchorPoint.BOTTOMLEFT, 32, -16);
        adrenalineBar = new DisplayConfigUtils.DisplayData(b, "adrenaline bar", DisplayConfigUtils.AnchorPoint.BOTTOMCENTER, -91, -29);
        adrenalineNumber = new CircleData(b, "adrenaline number", DisplayConfigUtils.AnchorPoint.BOTTOMLEFT, 32, -38);
        _adrenalineColor = b.translation("wardance.config.adrenalineC").comment("adrenaline color in hexadecimal").define("adrenaline color", "ccac00");
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
        playerPosture = new DisplayConfigUtils.DisplayData(b, "player posture", DisplayConfigUtils.AnchorPoint.BOTTOMCENTER, 0, -57);
        b.pop();
        b.push("enemy posture");
        enemyPosture = new DisplayConfigUtils.DisplayData(b, "target posture", DisplayConfigUtils.AnchorPoint.TOPCENTER, 0, 20);
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
        CONFIG.adrenalineBar.bake();
        CONFIG.adrenalineCircle.bake();
        CONFIG.spiritNumber.bake();
        CONFIG.spiritBar.bake();
        CONFIG.spiritCircle.bake();
        CONFIG.adrenalineNumber.bake();
        CONFIG.combo.bake();
        CONFIG.playerPosture.bake();
        CONFIG.enemyAfflict.bake();
        CONFIG.enemyPosture.bake();
        CONFIG.playerAfflict.bake();
        CONFIG.skillCD.bake();
        CONFIG.stealth.bake();
        hide = CONFIG._hidexp.get();
        spiritColor = Integer.parseInt(CONFIG._spiritColor.get(), 16);
        adrenalineColor = Integer.parseInt(CONFIG._adrenalineColor.get(), 16);
        autoCombat = CONFIG._autoCombat.get();
        controlScheme = CONFIG._ctrl.get();
        RenderEvents.updateList(CONFIG._customPosture.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            if (GeneralConfig.debug)
                WarDance.LOGGER.debug("loading client config!");
            bake();
        }
    }

    public enum ControlScheme {
        CLASSIC,
        DUAL
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
