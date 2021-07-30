package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneralConfig {
    public static final GeneralConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static boolean elenai, elenaiP, elenaiC, blindness;
    public static float weakness, hunger, poison, luck, nausea;
    public static boolean betterSweep, sweepDurability, resistance, dual;
    public static int sweepAngle;

    static {
        final Pair<GeneralConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(GeneralConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.BooleanValue _betterSweep;
    private final ForgeConfigSpec.BooleanValue _sweepDurability;
    private final ForgeConfigSpec.IntValue _sweepPerSE;
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

    public GeneralConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        b.push("dual wielding");
        _dual = b.translation("wardance.config.dualwield").comment("added per request. Shield bash will not work from the offhand when this is off, and clients will not render their offhand.").define("enable dual wielding", true);
        b.pop();
        b.push("sweep");
        _betterSweep = b.translation("wardance.config.sweep").comment("overrides vanilla sweep with a version hits all affected entities for full damage and effects and works regardless of aim, with sweeping edge determining the angle which is swept. Sweeps will be completely suppressed if you don't have the enchantment.").define("enable better sweep", true);
        _sweepDurability = b.translation("wardance.config.sweepD").comment("whether better sweep deals durability damage for each mob hit").define("durability damage per hit mob", false);
        _sweepPerSE = b.translation("wardance.config.sweepE").comment("every level of sweeping edge gives this much extra angles when using better sweep").defineInRange("sweeping edge angle per level", 40, 0, Integer.MAX_VALUE);
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
        betterSweep = CONFIG._betterSweep.get();
        sweepAngle = CONFIG._sweepPerSE.get();
        sweepDurability = CONFIG._sweepDurability.get();
        dual = CONFIG._dual.get();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
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