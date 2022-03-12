package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StealthConfig {
    public static final StealthConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final String[] SNEAK = {
            "minecraft:bat, ahmv",
            "minecraft:bee, ahmv",
            "minecraft:blaze, p",
            "minecraft:cat, elnqvw",
            "minecraft:cave_spider, cenq",
            "minecraft:chicken, ahmv",
            "minecraft:cod, ahmv",
            "minecraft:cow, ahmv",
            "minecraft:creeper, hp",
            "minecraft:dolphin, ahmv",
            "minecraft:donkey, ahmv",
            "minecraft:drowned, dq",
            "minecraft:elder_guardian, nah",
            "minecraft:ender_dragon, navk",
            "minecraft:enderman, naw",
            "minecraft:endermite, n",
            "minecraft:evoker, lms",
            "minecraft:fox, ahmv",
            "minecraft:ghast, nwd",
            "minecraft:guardian, na",
            "minecraft:horse, ahmv",
            "minecraft:husk, d",
            "minecraft:illusioner, ns",
            "minecraft:iron_golem, naw",
            "minecraft:llama, ahmv",
            "minecraft:mooshroom, ahmv",
            "minecraft:mule, ahmv",
            "minecraft:ocelot, ahmv",
            "minecraft:panda, ahmv",
            "minecraft:parrot, ahmv",
            "minecraft:phantom, nwaq",
            "minecraft:pig, ahmv",
            "minecraft:piglin, l",
            "minecraft:pillager, mps",
            "minecraft:polar_bear, ahmv",
            "minecraft:pufferfish, ahmv",
            "minecraft:rabbit, ahmv",
            "minecraft:salmon, ahmv",
            "minecraft:sheep, ahmv",
            "minecraft:shulker, andq",
            "minecraft:silverfish, ne",
            "minecraft:skeleton, demw",
            "minecraft:skeleton_horse, demv",
            "minecraft:slime, dq",
            "minecraft:snow_golem, nwv",
            "minecraft:spider, cenq",
            "minecraft:squid, ahmv",
            "minecraft:stray, dw",
            "minecraft:strider, ahmv",
            "minecraft:trader_llama, ahmv",
            "minecraft:tropical_fish, ahmv",
            "minecraft:turtle, ahmv",
            "minecraft:vex, ahmv",
            "minecraft:villager, ahmv",
            "minecraft:vindicator, wp",
            "minecraft:wandering_trader, mvw",
            "minecraft:wither, adnmkv",
            "minecraft:wither_skeleton, demh",
            "minecraft:zoglin, l",
            "minecraft:zombie, d",
            "minecraft:zombie_horse, ahmv",
            "minecraft:zombie_villager, dp",
            "minecraft:zombified_piglin, d",
            "ars_nouveau:ally_vex, ahmv",
            "ars_nouveau:carbuncle, ahmv",
            "ars_nouveau:drygmy, ahmv",
            "ars_nouveau:summon_horse, ahmv",
            "ars_nouveau:summon_wolf, ahmv",
            "ars_nouveau:sylph, ahmv",
            "ars_nouveau:whelp, ahmv",
            "ars_nouveau:wilden_guardian, skp",
            "ars_nouveau:wilden_hunter, pvw",
            "ars_nouveau:wilden_stalker, pqw",
            "ars_nouveau:wilden_boss, npw",
            "ars_nouveau:familiar_bookwyrm, ahmv",
            "ars_nouveau:familiar_carbuncle, ahmv",
            "ars_nouveau:familiar_drygmy, ahmv",
            "ars_nouveau:familiar_jabberwog, ahmv",
            "ars_nouveau:familiar_sylph, ahmv",
            "ars_nouveau:familiar_wixie, ahmv",
            "ars_nouveau:blazing_weald_walker, ahmv",
            "ars_nouveau:cascading_weald_walker, ahmv",
            "ars_nouveau:flourishing_weald_walker, ahmv",
            "ars_nouveau:vexing_weald_walker, ahmv",
            "artifacts:mimic, aenpv",
            "atum:assassin, vampq",
            "atum:bandit_warlord, lk",
            "atum:barbarian, kw",
            "atum:brigand, sw",
            "atum:desert_wolf, nw",
            "atum:forsaken, d",
            "atum:mummy, d",
            "atum:nomad, wp",
            "atum:pharaoh, wnasv",
            "atum:serval, anw",
            "atum:stoneguard, deks",
            "atum:stoneguard_friendly, deknv",
            "atum:stonewarden, deks",
            "atum:stonewarden_friendly, deknv",
            "atum:tarantula, hc",
            "atum:wraith, heq",
            "cavesandcliffs:axolotl, ahmv",
            "cavesandcliffs:glow_squid, ahmv",
            "cavesandcliffs:goat, ahmv",
            "charm:coral_squid, ahmv",
            "charm:moobloom, ahmv",
            "conjurer_illager:conjurer, wpml",
            "darkerdepths:glowshroom_monster, wplse",
            "darkerdepths:magma_minion, wpk",
            "dungeons_mobs:armored_drowned, dq",
            "dungeons_mobs:armored_mountaineer, pm",
            "dungeons_mobs:armored_pillager, mps",
            "dungeons_mobs:armored_skeleton, demw",
            "dungeons_mobs:armored_sunken_skeleton, demw",
            "dungeons_mobs:armored_vindicator, wp",
            "dungeons_mobs:armored_zombie, d",
            "dungeons_mobs:conjured_slime, dq",
            "dungeons_mobs:drowned_necromancer, ndl",
            "dungeons_mobs:frozen_zombie, d",
            "dungeons_mobs:fungus_thrower, l",
            "dungeons_mobs:geomancer, lms",
            "dungeons_mobs:iceologer, lms",
            "dungeons_mobs:icy_creeper, hp",
            "dungeons_mobs:illusioner, ns",
            "dungeons_mobs:illusioner_clone, ns",
            "dungeons_mobs:jungle_zombie, d",
            "dungeons_mobs:leapleaf, w",
            "dungeons_mobs:mossy_skeleton, demw",
            "dungeons_mobs:mountaineer, pm",
            "dungeons_mobs:necromancer, ndl",
            "dungeons_mobs:poison_anemone, vpeq",
            "dungeons_mobs:poison_quill_vine, vpeq",
            "dungeons_mobs:quick_growing_anemone, vpeq",
            "dungeons_mobs:quick_growing_vine, vpeq",
            "dungeons_mobs:redstone_cube, dq",
            "dungeons_mobs:redstone_golem, pw",
            "dungeons_mobs:royal_guard, pw",
            "dungeons_mobs:skeleton_horseman, demw",
            "dungeons_mobs:skeleton_vanguard, dw",
            "dungeons_mobs:slimeball, p",
            "dungeons_mobs:squall_golem, pw",
            "dungeons_mobs:sunken_skeleton, demw",
            "dungeons_mobs:vindicator_chef, wp",
            "dungeons_mobs:wavewhisperer, ae",
            "dungeons_mobs:whisperer, ae",
            "dungeons_mobs:windcaller, lms",
            "dungeons_mobs:wraith, heq",
            "dungeons_mobs:zombified_armored_piglin, d",
            "dungeons_mobs:zombified_fungus_thrower, d",
            "doggytalents:dog, ahmv",
            "eidolon:necromancer, ndl",
            "eidolon:wraith, heq",
            "eidolon:zombie_brute, d",
            "endergetic:bolloom_balloon, ahmv",
            "endergetic:booflo, ahmv",
            "endergetic:booflo_adolescent, ahmv",
            "endergetic:booflo_baby, ahmv",
            "endermail:ender_mailman, ahmv",
            "farmingforblockheads:merchant, ahmv",
            "guardvillagers:guard, pw",
            "iceandfire:cockatrice, s",
            "iceandfire:deathworm, ha",
            "iceandfire:dread_beast, h",
            "iceandfire:dread_ghoul, d",
            "iceandfire:dread_horse, ned",
            "iceandfire:dread_knight, nk",
            "iceandfire:dread_lich, ndl",
            "iceandfire:dread_scuttler, c",
            "iceandfire:dread_thrall, d",
            "iceandfire:fire_dragon, klmc",
            "iceandfire:ghost, aw",
            "iceandfire:gorgon, aw",
            "iceandfire:hippocampus, ahmv",
            "iceandfire:hippogryph, w",
            "iceandfire:hydra, w",
            "iceandfire:ice_dragon, lkmc",
            "iceandfire:lightning_dragon, w",
            "iceandfire:myrmex_queen, cehk",
            "iceandfire:myrmex_royal, cehl",
            "iceandfire:myrmex_sentinel, cehv",
            "iceandfire:myrmex_soldier, cehs",
            "iceandfire:myrmex_swarmer, ahmv",
            "iceandfire:myrmex_worker, cehw",
            "iceandfire:pixie, ew",
            "iceandfire:siren, ahmv",
            "iceandfire:stymphalian_bird, m",
            "iceandfire:sea_serpent, wps",
            "iceandfire:troll, nld",
            "inventorypets:anvil_pet_entity, ahmv",
            "inventorypets:bed_pet_entity, ahmv",
            "inventorypets:bill_gates_entity, ahmv",
            "inventorypets:mini_quantum_blaze_entity, ahmv",
            "inventorypets:mini_quantum_enderman_entity, ahmv",
            "inventorypets:satya_nadella_entity, ahmv",
            "inventorypets:siamese_entity, ahmv",
            "inventorypets:steve_ballmer_entity, ahmv",
            "losttrinkets:dark_vex, ahmv",
            "majruszs_difficulty:creeperling, hp",
            "majruszs_difficulty:elite_skeleton, demw",
            "majruszs_difficulty:pillager_wolf, nw",
            "majruszs_difficulty:sky_keeper, nwaq",
            "meetyourfight:bellringer, ahmv",
            "meetyourfight:dame_fortuna, ahmv",
            "meetyourfight:swampjaw, ahmv",
            "mowziesmobs:barako, ahmv",
            "mowziesmobs:barakoan_player, ahmv",
            "mowziesmobs:ferrous_wroughtnaut, ahmv",
            "mowziesmobs:foliaath, a",
            "mowziesmobs:frostmaw, s",
            "mowziesmobs:grottol, navw",
            "mowziesmobs:lantern, ahmv",
            "mowziesmobs:naga, m",
            "mutantbeasts:creeper_minion, ahmv",
            "mutantbeasts:endersoul_clone, ahmv",
            "mutantbeasts:mutant_enderman, napw",
            "mutantbeasts:mutant_skeleton, demw",
            "mutantbeasts:mutant_snow_golem, ahmv",
            "mutantbeasts:mutant_zombie, dw",
            "mutantbeasts:spider_pig, ahmv",
            "mysticalworld:beetle, ahmv",
            "mysticalworld:deer, ahmv",
            "mysticalworld:endermini, naw",
            "mysticalworld:frog, ahmv",
            "mysticalworld:hell_sprout, ahmv",
            "mysticalworld:lava_cat, ahmv",
            "mysticalworld:owl, ahmv",
            "mysticalworld:silkworm, ahmv",
            "mysticalworld:silver_fox, ahmv",
            "mysticalworld:sprout, ahmv",
            "outer_end:chorus_squid, ahmv",
            "outer_end:entombed, w",
            "outer_end:purpur_golem, w",
            "outer_end:spectrafly, ahmv",
            "outer_end:stalker, nap",
            "quark:crab, ahmv",
            "quark:forgotten, demw",
            "quark:foxhound, p",
            "quark:frog, ahmv",
            "quark:shiba, ahmv",
            "quark:stoneling, ep",
            "quark:toretoise, n",
            "quark:wraith, heq",
            "quark:wrapped, d",
            "switchbow:entitylittleirongolem, ahmv",
            "villagertools:guard, pw",
            "villagertools:reinforced_golem, naw",
            "mutantmore:mutant_blaze, wp",
            "mutantmore:mutant_hoglin, w",
            "mutantmore:mutant_husk, dw",
            "mutantmore:mutant_shulker, andqw",
            "mutantmore:mutant_wither_skeleton, ndwp",
            "mutantmore:rodling, ahmv",
            "savageandravage:executioner, p",
            "savageandravage:griefer, mps",
            "savageandravage:iceologer, lms",
            "savageandravage:skeleton_villager, demwp",
            "thermal:basalz, p",
            "thermal:blitz, p",
            "thermal:blizz, p"
    };
    public static float distract, unaware;
    public static boolean stealthSystem, ignore, inv, playerStealth;
    public static int baseHorizontalDetection, baseVerticalDetection, anglePerArmor;
    public static double blockPerVolume;

    static {
        final Pair<StealthConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(StealthConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customDetection;
    private final ForgeConfigSpec.BooleanValue _stab;
    private final ForgeConfigSpec.BooleanValue _removeInv;
    private final ForgeConfigSpec.BooleanValue _player;
    private final ForgeConfigSpec.DoubleValue _distract;
    private final ForgeConfigSpec.DoubleValue _unaware;
    private final ForgeConfigSpec.BooleanValue _ignore;
    private final ForgeConfigSpec.IntValue _baseDetectionHorizontal;
    private final ForgeConfigSpec.IntValue _baseDetectionVertical;
    private final ForgeConfigSpec.IntValue _anglePerArmor;
    private final ForgeConfigSpec.DoubleValue _blockPerVolume;

    public StealthConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        _stab = b.translation("wardance.config.stabby").comment("enable or disable the entire system").define("enable stabbing", true);
        _removeInv = b.translation("wardance.config.removeInvis").comment("whether invisibility will be removed on attack").define("attacking dispels invisibility", true);
        _player = b.translation("wardance.config.player").comment("whether you must pass stealth checks to perceive a mob. Currently rather incomplete. I am not responsible for ragequits caused by this option.").define("use player senses", false);
        _baseDetectionHorizontal = b.translation("wardance.config.detectH").comment("mobs start out with this FoV of full detection on the xz plane").defineInRange("default mob horizontal FoV", 120, 0, 360);
        _baseDetectionVertical = b.translation("wardance.config.detectV").comment("mobs start out with this FoV of full detection on the y axis").defineInRange("default mob vertical FoV", 60, 0, 360);
        _anglePerArmor = b.translation("wardance.config.perarmor").comment("your stealth attribute is multiplied by this to generate a new FoV for the purpose of detection by mobs, if it is greater than the default").defineInRange("armor stealth debuff", 18, 0, 360);
        _blockPerVolume = b.translation("wardance.config.volume").comment("this value is multiplied by the volume of a sound to determine how far it'll alert mobs to investigate from. Extremely large values will GREATLY impact performance, you have been warned.").defineInRange("alert volume multiplier", 16, 0, Double.MAX_VALUE);
        _distract = b.translation("wardance.config.distract").comment("posture and health damage multiplier for distracted stabs").defineInRange("distracted stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _unaware = b.translation("wardance.config.unaware").comment("posture and health damage multiplier for unaware stabs").defineInRange("unaware stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _ignore = b.translation("wardance.config.ignore").comment("whether unaware stabs ignore parry, deflection, shatter, and absorption").define("unaware stab defense ignore", true);
        _customDetection = b.translation("wardance.config.mobDetection").comment("Define custom detection mechanics for mobs by tagging them as one of the following: " +
                "\n(a)ll-seeing mobs ignore FoV modifiers. " +
                "\n(c)heliceric mobs are not distracted by cobwebs. " +
                "\n(d)eaf mobs ignore sound cues. " +
                "\n(e)yeless mobs cannot be blinded. " +
                "\n(h)eat-seeking mobs ignore LoS modifiers. " +
                "\n(k)lutzy mobs have effectively no stealth. " +
                "\n(l)azy mobs do not turn around to search for you on a successful luck check. " +
                "\n(m)indful mobs are treated as alert even when attacking entities other than you. " +
                "\n(n)octurnal mobs ignore light level. Despite what you may imagine, this will make sneaking upon them very hard, so tag in moderation. The retina system exists for a reason. " +
                "\n(p)erceptive mobs ignore motion multipliers. " +
                "\n(q)uiet mobs do not broadcast sound cues, even if they do make sounds. " +
                "\n(s)keptical mobs will always turn around before beginning to attack, even if you fail your luck check. " +
                "\n(v)igilant mobs are treated as alert even without an attack or revenge target."+
                "\n(w)ary mobs ignore luck. "
        ).defineList("mob detection rules", Arrays.asList(SNEAK), String.class::isInstance);
    }

    private static void bake() {
        stealthSystem = CONFIG._stab.get();
        distract = stealthSystem ? CONFIG._distract.get().floatValue() : 1;
        unaware = stealthSystem ? CONFIG._unaware.get().floatValue() : 1;
        ignore = stealthSystem & CONFIG._ignore.get();
        anglePerArmor = CONFIG._anglePerArmor.get();
        baseHorizontalDetection = CONFIG._baseDetectionHorizontal.get();
        baseVerticalDetection = CONFIG._baseDetectionVertical.get();
        blockPerVolume = CONFIG._blockPerVolume.get();
        inv = CONFIG._removeInv.get();
        playerStealth=CONFIG._player.get();
        StealthUtils.updateMobDetection(CONFIG._customDetection.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            if(GeneralConfig.debug)
            WarDance.LOGGER.debug("loading stealth config!");
            bake();
        }
    }

    public enum ThirdOption {
        TRUE,
        FALSE,
        FORCED
    }
}
