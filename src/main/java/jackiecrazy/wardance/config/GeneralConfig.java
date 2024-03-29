package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneralConfig {
    public static final GeneralConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static boolean elenai, elenaiP, elenaiC, blindness;
    public static float weakness;
    public static float hunger;
    public static float poison;
    public static float luck;
    public static float nausea;
    public static double rangeMult;
    public static boolean sweepDurability, resistance, dual, debug, test;
    public static boolean betterSweep;

    static {
        final Pair<GeneralConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(GeneralConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.BooleanValue _betterSweep;
    private final ForgeConfigSpec.BooleanValue _sweepDurability;
    private final ForgeConfigSpec.BooleanValue _elenai;
    private final ForgeConfigSpec.BooleanValue _elenaiP;
    private final ForgeConfigSpec.BooleanValue _elenaiC;
    private final ForgeConfigSpec.BooleanValue _blindness;
    private final ForgeConfigSpec.DoubleValue _nausea;
    private final ForgeConfigSpec.DoubleValue _poison;
    private final ForgeConfigSpec.DoubleValue _weakness;
    private final ForgeConfigSpec.DoubleValue _hunger;
    private final ForgeConfigSpec.DoubleValue _luck;
    private final ForgeConfigSpec.BooleanValue _resistance;
    private final ForgeConfigSpec.BooleanValue _dual;
    private final ForgeConfigSpec.BooleanValue _debug, _testing;

    public GeneralConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        b.push("general");
        _debug = b.translation("wardance.config.debug").comment("primarily tells you how much damage was applied before and after armor. These will only appear in debug.log.").define("enable debug messages", false);
        _testing = b.translation("wardance.config.testing").comment("used by the developer and testers to experiment with some unusual features that change from version to version. Only the dev himself really knows what this option does at any given moment. Enable at your own risk!").define("testing mode", false);
        b.pop();
        b.push("dual wielding");
        _dual = b.translation("wardance.config.dualwield").comment("added per request. Shield bash will not work from the offhand when this is off, and clients will not render their offhand.").define("enable dual wielding", true);
        b.pop();
        b.push("sweep");
        _betterSweep = b.translation("wardance.config.sweep").comment("overrides vanilla sweep with a version hits all affected entities for full damage and effects and works regardless of aim. Disabling this will also disable all functions that extend this, such as flurry.").define("enable better sweep", true);
        _sweepDurability = b.translation("wardance.config.sweepD").comment("whether better sweep deals durability damage for each mob hit").define("durability damage per hit mob", false);
        b.pop();
        b.push("compat");
        _elenai = b.translation("wardance.config.elenaiCompat").comment("whether Elenai Dodge 2 compat is enabled. This disables sidesteps and rolls, turns dodging into a safety roll when staggered, and causes dodges to reset posture cooldown").define("enable Elenai Dodge compat", true);
        _elenaiP = b.translation("wardance.config.elenaiPosture").comment("if compat is enabled, whether posture cooldown disables feather recharging").define("feather posture", true);
        _elenaiC = b.translation("wardance.config.elenaiCombo").comment("if compat is enabled, whether high combo multiplies feather regeneration speed").define("feather combo", true);
        b.pop();
        b.push("potion");
        _blindness = b.translation("wardance.config.blindness").comment("whether blindness will cause mobs to drop aggro").define("mob blindness", true);
        _nausea = b.translation("wardance.config.nausea").comment("how much posture nausea deducts per tick, for mobs only").defineInRange("nausea posture damage", 0.05, 0, Double.MAX_VALUE);
        _poison = b.translation("wardance.config.poison").comment("how much each level of poison multiplies posture regeneration by").defineInRange("poison posture debuff", 0.8, 0, Double.MAX_VALUE);
        _hunger = b.translation("wardance.config.hunger").comment("how much the hunger effect extends the posture cooldown by").defineInRange("hunger posture extension", 1.25, 0, Double.MAX_VALUE);
        _weakness = b.translation("wardance.config.weakness").comment("how much weakness multiplies might generation rate").defineInRange("weakness might debuff", 0.7, 0, Double.MAX_VALUE);
        _luck = b.translation("wardance.config.luck").comment("when attacking an entity, a number between 0 and luck is rolled for both parties. The difference between the attacker's and defender's rolled values is multiplied by this and dealt as additional damage. If the target is more lucky... well, it might not be hurt at all.").defineInRange("luck multiplier", 1.5, 0, Double.MAX_VALUE);
        _resistance = b.translation("wardance.config.resistance").comment("whether resistance also affects posture damage.").define("resistance posture", true);
        b.pop();
    }

    private static void bake() {
        debug = CONFIG._debug.get();
        elenai = CONFIG._elenai.get();
        elenaiC = CONFIG._elenaiC.get();
        elenaiP = CONFIG._elenaiP.get();
        blindness = CONFIG._blindness.get();
        weakness = CONFIG._weakness.get().floatValue();
        poison = CONFIG._poison.get().floatValue();
        hunger = CONFIG._hunger.get().floatValue();
        luck = CONFIG._luck.get().floatValue();
        nausea = CONFIG._nausea.get().floatValue();
        resistance = CONFIG._resistance.get();
        sweepDurability = CONFIG._sweepDurability.get();
        betterSweep = CONFIG._betterSweep.get();
        dual = CONFIG._dual.get();
        test = CONFIG._testing.get();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            if(GeneralConfig.debug)
               WarDance.LOGGER.debug("loading general config!");
            bake();
        }
    }

    public enum ThirdOption {
        TRUE,
        FALSE,
        FORCED
    }
}
