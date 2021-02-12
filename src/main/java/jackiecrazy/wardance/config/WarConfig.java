package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WarConfig {
    public static final WarConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final Pair<WarConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(WarConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.DoubleValue _posturePerProjectile;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureDefend;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureAttack;
    private final ForgeConfigSpec.IntValue _rollThreshold;
    private final ForgeConfigSpec.IntValue _rollCooldown;
    private final ForgeConfigSpec.IntValue _shieldThreshold;
    private final ForgeConfigSpec.IntValue _staggerDuration;
    private final ForgeConfigSpec.IntValue _staggerHits;
    private final ForgeConfigSpec.IntValue _mobUpdateInterval;
    private final ForgeConfigSpec.IntValue _qiGrace;
    private final ForgeConfigSpec.IntValue _comboGrace;
    private final ForgeConfigSpec.IntValue _spiritCD;
    private final ForgeConfigSpec.IntValue _postureCD;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _combatItems;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;
    private final ForgeConfigSpec.DoubleValue _mobParryChance;

    public static float posturePerProjectile;
    public static float defaultMultiplierPostureDefend;
    public static float defaultMultiplierPostureAttack;
    public static int rollThreshold;
    public static int rollCooldown;
    public static int shieldThreshold;
    public static int staggerDuration;
    public static int staggerHits;
    public static int mobUpdateInterval;
    public static int qiGrace;
    public static int comboGrace;
    public static int spiritCD;
    public static int postureCD;
    public static float mobParryChance;

    public WarConfig(ForgeConfigSpec.Builder b) {
        b.push("numbers");
        _posturePerProjectile = b.translation("wardance.config.ppp").comment("Posture consumed per projectile parried").defineInRange("posturePerProjectile", 0.5, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureAttack = b.translation("wardance.config.dmpa").comment("Default multiplier for any items not defined in the config, multiplied by their attack damage").defineInRange("defaultPostureMultiplierAttack", 0.15, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureDefend = b.translation("wardance.config.dmpd").comment("Default multiplier for any item not defined in the config, when used for parrying").defineInRange("defaultPostureMultiplierDefend", 1.4, 0, Double.MAX_VALUE);
        _rollThreshold = b.translation("wardance.config.rollT").comment("Within this number of ticks after rolling the entity is considered invulnerable").defineInRange("rollThreshold", 15, 0, Integer.MAX_VALUE);
        _rollCooldown = b.translation("wardance.config.rollC").comment("Within this number of ticks after rolling the entity cannot roll again").defineInRange("rollCooldown", 20, 0, Integer.MAX_VALUE);
        _shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("shieldThreshold", 16, 0, Integer.MAX_VALUE);
        _mobUpdateInterval = b.translation("wardance.config.mobU").comment("Mobs are forced to sync to client every this number of ticks").defineInRange("mobUpdateInterval", 100, 1, Integer.MAX_VALUE);
        _qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining qi during which it will not decrease").defineInRange("qiGrace", 100, 1, Integer.MAX_VALUE);
        _comboGrace = b.translation("wardance.config.comboG").comment("Number of ticks after gaining combo during which it will not decrease").defineInRange("comboGrace", 100, 1, Integer.MAX_VALUE);
        _spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spiritCD", 30, 1, Integer.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("postureCD", 30, 1, Integer.MAX_VALUE);
        _staggerDuration = b.translation("wardance.config.staggerD").comment("Number of ticks an entity should be staggered for when its posture reaches 0").defineInRange("staggerDuration", 60, 1, Integer.MAX_VALUE);
        _staggerHits = b.translation("wardance.config.staggerH").comment("Number of hits a staggered entity will take before stagger is automatically canceled").defineInRange("staggerHits", 3, 1, Integer.MAX_VALUE);
        _mobParryChance = b.translation("wardance.config.mobP").comment("chance that a mob parries out of 1").defineInRange("mobParryChance", 0.6, 0, 1);
        b.pop();
        b.push("lists");
        _combatItems = b.translation("wardance.config.combatItems").comment("Items eligible for parrying. Format should be name, attack posture consumption, defense multiplier, is shield").defineList("combatItems", Lists.newArrayList("example:sword, 3.5, 1.5, false", "example:shield, 0.3, 0.6, true"), String.class::isInstance);
        _customPosture = b.translation("wardance.config.combatItems").comment("Here you can define custom max posture for mobs. Format is name, max posture. Armor is still calculated").defineList("customPosture", Lists.newArrayList("example:dragon, 100", "example:ghast, 8"), String.class::isInstance);
        b.pop();
    }

    private static void bake() {
        posturePerProjectile = CONFIG._posturePerProjectile.get().floatValue();
        defaultMultiplierPostureDefend = CONFIG._defaultMultiplierPostureDefend.get().floatValue();
        defaultMultiplierPostureAttack = CONFIG._defaultMultiplierPostureAttack.get().floatValue();
        rollCooldown = CONFIG._rollCooldown.get();
        rollThreshold = CONFIG._rollThreshold.get();
        shieldThreshold = CONFIG._shieldThreshold.get();
        mobUpdateInterval = CONFIG._mobUpdateInterval.get();
        qiGrace = CONFIG._qiGrace.get();
        comboGrace = CONFIG._comboGrace.get();
        spiritCD = CONFIG._spiritCD.get();
        postureCD = CONFIG._postureCD.get();
        staggerDuration = CONFIG._staggerDuration.get();
        staggerHits = CONFIG._staggerHits.get();
        mobParryChance = CONFIG._mobParryChance.get().floatValue();
        CombatUtils.updateLists(CONFIG._combatItems.get(), CONFIG._customPosture.get(), defaultMultiplierPostureAttack, defaultMultiplierPostureDefend);
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        //this is not being called!
        WarDance.LOGGER.debug("loading config!");
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }
    }
}
