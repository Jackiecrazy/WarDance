package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourceConfig {
    public static final ResourceConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static int shatterCooldown;
    public static int qiGrace;
    public static int rankGrace;
    public static int spiritCD;
    public static int postureCD;
    public static int armorPostureCD;
    public static float wound, fatigue, burnout;
    public static ArrayList<String> woundList;
    public static boolean woundWL;
    public static ArrayList<String> immortal;
    public static boolean immortalWL;
    public static ThirdOption sleepingHealsDecay;

    static {
        final Pair<ResourceConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ResourceConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.IntValue _shatterCooldown;
    private final ForgeConfigSpec.IntValue _qiGrace;
    private final ForgeConfigSpec.IntValue _spiritCD;
    private final ForgeConfigSpec.IntValue _postureCD;
    private final ForgeConfigSpec.IntValue _armorPostureCD;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _woundBL;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _immortal;
    private final ForgeConfigSpec.BooleanValue _woundWL;
    private final ForgeConfigSpec.BooleanValue _immortalWL;
    private final ForgeConfigSpec.EnumValue<ThirdOption> _sleep;
    private final ForgeConfigSpec.DoubleValue _wound;
    private final ForgeConfigSpec.DoubleValue _fatig;
    private final ForgeConfigSpec.DoubleValue _burno;

    public ResourceConfig(ForgeConfigSpec.Builder b) {
        //master, resources, compat, stealth, items, misc
        _shatterCooldown = b.translation("wardance.config.shatterCD").comment("Ticks after a hit for which shatter will not be replenished").defineInRange("shatter cooldown", 200, 1, Integer.MAX_VALUE);
        _qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining might during which it will not decrease").defineInRange("might grace period", 100, 1, Integer.MAX_VALUE);
        _spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spirit cooldown", 80, 1, Integer.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("posture cooldown", 20, 1, Integer.MAX_VALUE);
        _armorPostureCD = b.translation("wardance.config.postureA").comment("Number of ticks full diamond armor will add onto posture cooldown. This will scale linearly between nothing and diamond, so by default iron adds 15 ticks, for instance.").defineInRange("armor posture cooldown", 20, 1, Integer.MAX_VALUE);
        _wound = b.translation("wardance.config.wound").comment("this percentage of incoming damage before armor is also added to wounding").defineInRange("wound percentage", 0.1, 0, 1d);
        _fatig = b.translation("wardance.config.fatigue").comment("this percentage of posture damage is also added to fatigue").defineInRange("fatigue percentage", 0.1, 0, 1d);
        _burno = b.translation("wardance.config.burnout").comment("this percentage of stamina use is also added to burnout").defineInRange("burnout percentage", 0.02, 0, 1d);
        _sleep = b.translation("wardance.config.sleeping").comment("whether sleeping clears wounding, fatigue, and burnout. Forced will make the act of lying on a bed, rather than waking up, the trigger, so change it to forced if it doesn't work.").defineEnum("sleeping heals decay", ThirdOption.TRUE);
        _customPosture = b.translation("wardance.config.postureMobs").comment("Here you can define custom max posture for mobs. Armor adds to this independently.").defineList("custom mob posture", Lists.newArrayList("example:dragon, 100", "example:ghast, 8"), String.class::isInstance);
        _woundBL = b.translation("wardance.config.woundBL").comment("damage sources added to this list will either not inflict wounding or be the only ones that inflict wounding, depending on whitelist mode").defineList("damage source list", Lists.newArrayList("magic", "indirectmagic", "survivaloverhaul.electrocution", "survivaloverhaul.hypothermia", "survivaloverhaul.hyperthermia", "inWall", "drown", "starve"), String.class::isInstance);
        _woundWL = b.translation("wardance.config.woundWL").comment("whether the wounding list is a whitelist or a blacklist").define("damage source whitelist mode", false);
        _immortal = b.translation("wardance.config.decayBL").comment("entities that are not (or are the only ones, depending on whitelist mode) susceptible to wounding, fatigue, and burnout. Save your pets!").defineList("decay list", Lists.newArrayList("ars_nouveau:whelp", "ars_nouveau:drygmy", "ars_nouveau:carbuncle", "ars_nouveau:wixie", "ars_nouveau:sylph", "atum:camel", "atum:desert_wolf", "atum:pharaoh", "atum:stoneguard_friendly", "doggytalents:dog", "endermail:ender_mailman", "iceandfire:amphithere", "iceandfire:cockatrice", "iceandfire:fire_dragon", "iceandfire:hippocampus", "iceandfire:hippogryph", "iceandfire:ice_dragon", "iceandfire:lightning_dragon", "minecraft:cat", "minecraft:ender_dragon", "minecraft:fox", "minecraft:horse", "minecraft:llama", "minecraft:mule", "minecraft:ocelot", "minecraft:parrot", "minecraft:strider", "minecraft:trader_llama", "minecraft:wolf", "mowziesmobs:grottol", "mutantbeasts:creeper_minion", "mutantbeasts:mutant_snow_golem", "mysticalworld:lava_cat", "mysticalworld:silver_fox", "quark:shiba", "securitycraft:securitycamera", "securitycraft:sentry", "villagertools:guard", "villagertools:reinforced_golem", "minecraft:villager", "guardvillagers:guard"), String.class::isInstance);
        _immortalWL = b.translation("wardance.config.decayWL").comment("whether the decay list is a whitelist or a blacklist").define("decay whitelist mode", false);
    }

    private static void bake() {
        qiGrace = CONFIG._qiGrace.get();
        spiritCD = CONFIG._spiritCD.get();
        postureCD = CONFIG._postureCD.get();
        shatterCooldown = CONFIG._shatterCooldown.get();
        wound = CONFIG._wound.get().floatValue();
        fatigue = CONFIG._fatig.get().floatValue();
        burnout = CONFIG._burno.get().floatValue();
        woundList = new ArrayList<>(CONFIG._woundBL.get());
        immortal = new ArrayList<>(CONFIG._immortal.get());
        woundWL = CONFIG._woundWL.get();
        immortalWL = CONFIG._immortalWL.get();
        armorPostureCD = CONFIG._armorPostureCD.get();
        sleepingHealsDecay = CONFIG._sleep.get();
        CombatUtils.updateMobPosture(CONFIG._customPosture.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
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
