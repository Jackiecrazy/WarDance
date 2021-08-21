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
public class CombatConfig {
    public static final CombatConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static float posturePerProjectile;
    public static float defaultMultiplierPostureDefend;
    public static float defaultMultiplierPostureAttack;
    public static int rollEndsAt;
    public static int rollCooldown;
    public static int shieldThreshold;
    public static int shieldCount;
    public static int staggerDuration;
    public static int staggerDurationMin;
    public static int staggerHits;
    public static float staggerDamage;
    public static int sneakParry;
    public static int recovery;
    public static int foodCool;
    public static float mobParryChanceWeapon, mobParryChanceShield, mobDeflectChance, mobScaler, kenshiroScaler;
    public static float posCap;
    public static boolean dodge;
    public static float kbNerf;

    static {
        final Pair<CombatConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CombatConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.DoubleValue _posturePerProjectile;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureDefend;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureAttack;
    private final ForgeConfigSpec.IntValue _rollThreshold;
    private final ForgeConfigSpec.IntValue _rollCooldown;
    private final ForgeConfigSpec.IntValue _shieldThreshold;
    private final ForgeConfigSpec.IntValue _shieldCount;
    private final ForgeConfigSpec.IntValue _staggerDuration;
    private final ForgeConfigSpec.IntValue _staggerDurationMin;
    private final ForgeConfigSpec.IntValue _staggerHits;
    private final ForgeConfigSpec.IntValue _recovery;
    private final ForgeConfigSpec.BooleanValue _dodge;
    private final ForgeConfigSpec.IntValue _sneakParry;
    private final ForgeConfigSpec.IntValue _foodCool;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceWeapon;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceShield;
    private final ForgeConfigSpec.DoubleValue _mobDeflectChance;
    private final ForgeConfigSpec.DoubleValue _mobScaler;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customParry;
    private final ForgeConfigSpec.DoubleValue _kenshiroScaler;
    private final ForgeConfigSpec.DoubleValue _posCap;
    private final ForgeConfigSpec.DoubleValue _stagger;
    private final ForgeConfigSpec.DoubleValue _knockbackNerf;

    public CombatConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        b.push("posture");
        _posCap = b.translation("wardance.config.posCap").comment("percentage of max posture that can be dealt in a single hit").defineInRange("posture cap", 0.4, 0, 1);
        _recovery = b.translation("wardance.config.recovery").comment("amount of ticks over which you'll quickly recover back to one posture cap's worth of posture if you're lower than that. This triggers after your posture cooldown elapses, plus 50% of that time. Set to 0 to disable this feature.").defineInRange("recovery speed", 15, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("parrying");
        _sneakParry = b.translation("wardance.config.sneakParry").comment("parries will only work in this many ticks after pressing the designated key, and cannot be triggered again for the same amount of time afterwards; 0 to disable. I don't know why everyone wants this option, but here it is. Set to -1 to toggle auto parry on and off with the key instead.").defineInRange("manual parry time", 0, -1, Integer.MAX_VALUE);
        _posturePerProjectile = b.translation("wardance.config.ppp").comment("Posture consumed per projectile parried").defineInRange("posture per projectile", 0.5, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureAttack = b.translation("wardance.config.dmpa").comment("Default multiplier for any items not defined in the config, multiplied by their attack damage").defineInRange("default attack multiplier", 0.15, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureDefend = b.translation("wardance.config.dmpd").comment("Default multiplier for any item not defined in the config, when used for parrying").defineInRange("default defense multiplier", 1.4, 0, Double.MAX_VALUE);
        _shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("default shield time", 16, 0, Integer.MAX_VALUE);
        _shieldCount = b.translation("wardance.config.shieldT").comment("This many parries are free after a parry that cost posture").defineInRange("default shield count", 1, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("dodging");
        _dodge = b.translation("wardance.config.dodge").define("enable dodges", true);
        _rollThreshold = b.translation("wardance.config.rollT").comment("Within this number of ticks after rolling the entity is considered invulnerable.").defineInRange("roll time", 10, 0, Integer.MAX_VALUE);
        _rollCooldown = b.translation("wardance.config.rollC").comment("Within this number of ticks after dodging the entity cannot dodge again").defineInRange("roll cooldown", 20, 0, Integer.MAX_VALUE);
        b.pop();
        b.push("stagger");
        _staggerDuration = b.translation("wardance.config.staggerD").comment("Maximum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("max stagger duration", 100, 1, Integer.MAX_VALUE);
        _staggerDurationMin = b.translation("wardance.config.staggerM").comment("Minimum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("min stagger duration", 40, 1, Integer.MAX_VALUE);
        _staggerHits = b.translation("wardance.config.staggerH").comment("Number of hits a staggered entity will take before stagger is automatically canceled").defineInRange("stagger hits", 3, 1, Integer.MAX_VALUE);
        _stagger = b.translation("wardance.config.stagger").comment("Extra damage taken by a staggered entity").defineInRange("stagger damage multiplier", 1.5, 1, Double.MAX_VALUE);
        b.pop();
        b.push("difficulty");
        _mobParryChanceWeapon = b.translation("wardance.config.mobPW").comment("chance that a mob parries with a weapon out of 1. Hands are individually calculated.").defineInRange("mob weapon parry chance", 0.3, 0, 1);
        _mobParryChanceShield = b.translation("wardance.config.mobPS").comment("chance that a mob parries with a shield out of 1. Hands are individually calculated.").defineInRange("mob shield parry chance", 0.9, 0, 1);
        _mobDeflectChance = b.translation("wardance.config.mobD").comment("chance that a mob deflects with armor out of 1").defineInRange("mob deflect chance", 0.6, 0, 1);
        _customParry = b.translation("wardance.config.parryMobs").comment("Define mobs that are automatically capable of parrying. Entity settings override weapon settings. Format is name, defense posture multiplier, parry chance. Hands are individually calculated for chance. Filling in 0 for parry chance will disable parrying for that mob, regardless of weaponry.").defineList("auto parry mobs", Lists.newArrayList("example:golem, 0.9, 0.5", "example:fish, 1, 0"), String.class::isInstance);
        _kenshiroScaler = b.translation("wardance.config.kenB").comment("posture damage from empty fists will be scaled by this number. Notice many mobs, such as endermen and ravagers, technically are empty-handed!").defineInRange("unarmed buff", 1.6, 0, Double.MAX_VALUE);
        _mobScaler = b.translation("wardance.config.mobB").comment("posture damage from mob attacks will be scaled by this number").defineInRange("mob posture damage buff", 1.5, 0, Double.MAX_VALUE);
        _knockbackNerf = b.translation("wardance.config.knockback").comment("knockback from all sources to everything will be multiplied by this amount").defineInRange("knockback multiplier", 1, 0, 10d);
        b.pop();
        b.push("misc");
        _foodCool = b.translation("wardance.config.foodCool").comment("number of ticks to disable a certain food item for after taking physical damage while eating it. Set to 0 to just interrupt eating, and -1 to disable this feature.").defineInRange("food disable time", 20, -1, Integer.MAX_VALUE);
        b.pop();
    }

    private static void bake() {
        posturePerProjectile = CONFIG._posturePerProjectile.get().floatValue();
        defaultMultiplierPostureDefend = CONFIG._defaultMultiplierPostureDefend.get().floatValue();
        defaultMultiplierPostureAttack = CONFIG._defaultMultiplierPostureAttack.get().floatValue();
        rollCooldown = CONFIG._rollCooldown.get();
        rollEndsAt = rollCooldown - CONFIG._rollThreshold.get();
        shieldThreshold = CONFIG._shieldThreshold.get();
        shieldCount = CONFIG._shieldCount.get();
        staggerDuration = CONFIG._staggerDuration.get();
        staggerDurationMin = CONFIG._staggerDurationMin.get();
        staggerHits = CONFIG._staggerHits.get();
        staggerDamage = CONFIG._stagger.get().floatValue();
        mobParryChanceWeapon = CONFIG._mobParryChanceWeapon.get().floatValue();
        mobParryChanceShield = CONFIG._mobParryChanceShield.get().floatValue();
        mobDeflectChance = CONFIG._mobDeflectChance.get().floatValue();
        mobScaler = CONFIG._mobScaler.get().floatValue();
        kenshiroScaler = CONFIG._kenshiroScaler.get().floatValue();
        posCap = CONFIG._posCap.get().floatValue();
        dodge = CONFIG._dodge.get();
        kbNerf = CONFIG._knockbackNerf.get().floatValue();
        sneakParry = CONFIG._sneakParry.get();
        recovery = CONFIG._recovery.get();
        foodCool = CONFIG._foodCool.get();
        CombatUtils.updateMobParrying(CONFIG._customParry.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
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
