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
            "ars_nouveau:ally_vex, ahmvo",
            "ars_nouveau:blazing_weald_walker, ahmvo",
            "ars_nouveau:carbuncle, ahmvo",
            "ars_nouveau:cascading_weald_walker, ahmvo",
            "ars_nouveau:drygmy, ahmvo",
            "ars_nouveau:familiar_bookwyrm, ahmvo",
            "ars_nouveau:familiar_carbuncle, ahmvo",
            "ars_nouveau:familiar_drygmy, ahmvo",
            "ars_nouveau:familiar_jabberwog, ahmvo",
            "ars_nouveau:familiar_sylph, ahmvo",
            "ars_nouveau:familiar_wixie, ahmvo",
            "ars_nouveau:flourishing_weald_walker, ahmvo",
            "ars_nouveau:summon_horse, ahmvo",
            "ars_nouveau:summon_wolf, ahmvo",
            "ars_nouveau:sylph, ahmvo",
            "ars_nouveau:vexing_weald_walker, ahmvo",
            "ars_nouveau:whelp, ahmvo",
            "ars_nouveau:wilden_boss, npw",
            "ars_nouveau:wilden_guardian, skp",
            "ars_nouveau:wilden_hunter, pvw",
            "ars_nouveau:wilden_stalker, pqw",
            "artifacts:mimic, aenpv",
            "atum:assassin, vampq",
            "atum:bandit_warlord, lk",
            "atum:barbarian, kw",
            "atum:brigand, sw",
            "atum:desert_wolf, nwo",
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
            "cavesandcliffs:axolotl, ahmvo",
            "cavesandcliffs:glow_squid, ahmvo",
            "cavesandcliffs:goat, ahmvo",
            "charm:coral_squid, ahmvo",
            "charm:moobloom, ahmvo",
            "conjurer_illager:conjurer, wpml",
            "darkerdepths:glowshroom_monster, wplse",
            "darkerdepths:magma_minion, wpk",
            "doggytalents:dog, ahmvo",
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
            "dungeons_mobs:geomancer, lmso",
            "dungeons_mobs:iceologer, lmso",
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
            "dungeons_mobs:windcaller, lmso",
            "dungeons_mobs:wraith, heq",
            "dungeons_mobs:zombified_armored_piglin, d",
            "dungeons_mobs:zombified_fungus_thrower, d",
            "eidolon:necromancer, ndl",
            "eidolon:wraith, heq",
            "eidolon:zombie_brute, d",
            "endergetic:bolloom_balloon, ahmvo",
            "endergetic:booflo, ahmvo",
            "endergetic:booflo_adolescent, ahmvo",
            "endergetic:booflo_baby, ahmvo",
            "endermail:ender_mailman, ahmvo",
            "farmingforblockheads:merchant, ahmvo",
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
            "iceandfire:hippocampus, ahmvo",
            "iceandfire:hippogryph, w",
            "iceandfire:hydra, w",
            "iceandfire:ice_dragon, lkmc",
            "iceandfire:lightning_dragon, w",
            "iceandfire:myrmex_queen, cehk",
            "iceandfire:myrmex_royal, cehl",
            "iceandfire:myrmex_sentinel, cehv",
            "iceandfire:myrmex_soldier, cehs",
            "iceandfire:myrmex_swarmer, ahmvo",
            "iceandfire:myrmex_worker, cehw",
            "iceandfire:pixie, ew",
            "iceandfire:sea_serpent, wps",
            "iceandfire:siren, ahmvo",
            "iceandfire:stymphalian_bird, m",
            "iceandfire:troll, nld",
            "infernalexp:basalt_giant, kl",
            "infernalexp:blackstone_dwarf, kl",
            "infernalexp:blindsight, ew",
            "infernalexp:embody, dw",
            "infernalexp:glowsilk_moth, mvc",
            "infernalexp:glowsquito, c",
            "infernalexp:shroomloin, p",
            "infernalexp:voline, ho",
            "infernalexp:warpbeetle, mvc",
            "inventorypets:anvil_pet_entity, ahmvo",
            "inventorypets:bed_pet_entity, ahmvo",
            "inventorypets:bill_gates_entity, ahmvo",
            "inventorypets:mini_quantum_blaze_entity, ahmvo",
            "inventorypets:mini_quantum_enderman_entity, ahmvo",
            "inventorypets:satya_nadella_entity, ahmvo",
            "inventorypets:siamese_entity, ahmvo",
            "inventorypets:steve_ballmer_entity, ahmvo",
            "losttrinkets:dark_vex, ahmvo",
            "majruszs_difficulty:creeperling, hp",
            "majruszs_difficulty:elite_skeleton, demw",
            "majruszs_difficulty:pillager_wolf, nwo",
            "majruszs_difficulty:sky_keeper, nwaq",
            "meetyourfight:bellringer, ahmvo",
            "meetyourfight:dame_fortuna, ahmvo",
            "meetyourfight:swampjaw, ahmvo",
            "minecraft:bat, ahmvo",
            "minecraft:bee, ahmvo",
            "minecraft:blaze, p",
            "minecraft:cat, elnqvw",
            "minecraft:cave_spider, cenq",
            "minecraft:chicken, ahmvo",
            "minecraft:cod, ahmvo",
            "minecraft:cow, ahmvo",
            "minecraft:creeper, hp",
            "minecraft:dolphin, ahmvo",
            "minecraft:donkey, ahmvo",
            "minecraft:drowned, dq",
            "minecraft:elder_guardian, nah",
            "minecraft:ender_dragon, namvko",
            "minecraft:enderman, naw",
            "minecraft:endermite, n",
            "minecraft:evoker, lmso",
            "minecraft:fox, ahmvo",
            "minecraft:ghast, nwd",
            "minecraft:guardian, na",
            "minecraft:horse, ahmvo",
            "minecraft:husk, d",
            "minecraft:illusioner, ns",
            "minecraft:iron_golem, naw",
            "minecraft:llama, ahmvo",
            "minecraft:mooshroom, ahmvo",
            "minecraft:mule, ahmvo",
            "minecraft:ocelot, ahmvo",
            "minecraft:panda, ahmvo",
            "minecraft:parrot, ahmvo",
            "minecraft:phantom, nwaq",
            "minecraft:pig, ahmvo",
            "minecraft:piglin, l",
            "minecraft:pillager, mps",
            "minecraft:polar_bear, ahmvo",
            "minecraft:pufferfish, ahmvo",
            "minecraft:rabbit, ahmvo",
            "minecraft:salmon, ahmvo",
            "minecraft:sheep, ahmvo",
            "minecraft:shulker, andq",
            "minecraft:silverfish, ne",
            "minecraft:skeleton, demw",
            "minecraft:skeleton_horse, demv",
            "minecraft:slime, dq",
            "minecraft:snow_golem, nwv",
            "minecraft:spider, cenq",
            "minecraft:squid, ahmvo",
            "minecraft:stray, dw",
            "minecraft:strider, ahmvo",
            "minecraft:trader_llama, ahmvo",
            "minecraft:tropical_fish, ahmvo",
            "minecraft:turtle, ahmvo",
            "minecraft:vex, ahmvo",
            "minecraft:villager, ahmvo",
            "minecraft:vindicator, wp",
            "minecraft:wandering_trader, mvw",
            "minecraft:wither, adnmkv",
            "minecraft:wither_skeleton, demh",
            "minecraft:zoglin, l",
            "minecraft:zombie, d",
            "minecraft:zombie_horse, ahmvo",
            "minecraft:zombie_villager, dp",
            "minecraft:zombified_piglin, d",
            "mod_lavacow:avaton, oe",
            "mod_lavacow:banshee, oe",
            "mod_lavacow:boneworm, dpe",
            "mod_lavacow:forsaken, dem",
            "mod_lavacow:frigid, dl",
            "mod_lavacow:ghostray, mv",
            "mod_lavacow:imp, h",
            "mod_lavacow:lavacow, mv",
            "mod_lavacow:lilsludge, p",
            "mod_lavacow:mimic, echl",
            "mod_lavacow:mummy, de",
            "mod_lavacow:mycosis, dl",
            "mod_lavacow:parasite, elqc",
            "mod_lavacow:pingu, de",
            "mod_lavacow:piranha, ph",
            "mod_lavacow:ptera, p",
            "mod_lavacow:raven, mv",
            "mod_lavacow:salamander, l",
            "mod_lavacow:scarecrow, op",
            "mod_lavacow:seagull, mv",
            "mod_lavacow:skeletonking, dwonke",
            "mod_lavacow:sludgelord, dke",
            "mod_lavacow:swarmer, p",
            "mod_lavacow:unburied, dle",
            "mod_lavacow:undeadswine, k",
            "mod_lavacow:undertaker, kde",
            "mod_lavacow:vespa, mvc",
            "mod_lavacow:vespacocoon, mvc",
            "mod_lavacow:wendigo, hmp",
            "mod_lavacow:weta, c",
            "mowziesmobs:barako, ahmvo",
            "mowziesmobs:barakoan_player, ahmvo",
            "mowziesmobs:ferrous_wroughtnaut, ahmvo",
            "mowziesmobs:foliaath, a",
            "mowziesmobs:frostmaw, s",
            "mowziesmobs:grottol, navw",
            "mowziesmobs:lantern, ahmvo",
            "mowziesmobs:naga, m",
            "mutantbeasts:creeper_minion, ahmvo",
            "mutantbeasts:endersoul_clone, ahmvo",
            "mutantbeasts:mutant_enderman, napw",
            "mutantbeasts:mutant_skeleton, demw",
            "mutantbeasts:mutant_snow_golem, ahmvo",
            "mutantbeasts:mutant_zombie, dw",
            "mutantbeasts:spider_pig, ahmvo",
            "mutantmore:mutant_blaze, wp",
            "mutantmore:mutant_hoglin, w",
            "mutantmore:mutant_husk, dw",
            "mutantmore:mutant_shulker, andqw",
            "mutantmore:mutant_wither_skeleton, ndwp",
            "mutantmore:rodling, ahmvo",
            "mysticalworld:beetle, ahmvo",
            "mysticalworld:deer, ahmvo",
            "mysticalworld:endermini, naw",
            "mysticalworld:frog, ahmvo",
            "mysticalworld:hell_sprout, ahmvo",
            "mysticalworld:lava_cat, ahmvo",
            "mysticalworld:owl, ahmvo",
            "mysticalworld:silkworm, ahmvo",
            "mysticalworld:silver_fox, ahmvo",
            "mysticalworld:sprout, ahmvo",
            "outer_end:chorus_squid, ahmvo",
            "outer_end:entombed, w",
            "outer_end:purpur_golem, w",
            "outer_end:spectrafly, ahmvo",
            "outer_end:stalker, nap",
            "quark:crab, ahmvo",
            "quark:forgotten, demw",
            "quark:foxhound, p",
            "quark:frog, ahmvo",
            "quark:shiba, ahmvo",
            "quark:stoneling, ep",
            "quark:toretoise, n",
            "quark:wraith, heq",
            "quark:wrapped, d",
            "savageandravage:executioner, p",
            "savageandravage:griefer, mps",
            "savageandravage:iceologer, lmso",
            "savageandravage:skeleton_villager, demwp",
            "switchbow:entitylittleirongolem, ahmvo",
            "thermal:basalz, p",
            "thermal:blitz, p",
            "thermal:blizz, p",
            "villagertools:guard, pw",
            "villagertools:reinforced_golem, naw"
    };
    private static final String[] SOUND = {
            "*armor.equip, 4",
            "*arrow, 4",
            "*scream, 16",
            "*door.open, 4",
            "*door.close, 4",
            "*.break, 4",
            "*.place, 4",
            "*music_disc, 8",
            "*note_block, 8",
            "*angry, 8",
            "*click_off, 4",
            "*click_on, 4",
            "entity.bee.loop_aggressive, 4",
            "item.crossbow.shoot, 4",
            "entity.generic.eat, 4",
            "entity.generic.drink, 4",
            "entity.minecart.riding, 4",
            "entity.generic.explode, 16",
            "entity.player.big_fall, 8",
            "entity.player.burp, 6",
            "entity.ravager.roar, 16"
    };
    public static float distract, unaware;
    public static boolean stealthSystem, ignore, inv, playerStealth;
    public static int baseHorizontalDetection, baseVerticalDetection, shout;
    public static double blockPerVolume;

    static {
        final Pair<StealthConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(StealthConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customDetection;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _sounds;
    private final ForgeConfigSpec.BooleanValue _stab;
    private final ForgeConfigSpec.BooleanValue _removeInv;
    private final ForgeConfigSpec.BooleanValue _player;
    private final ForgeConfigSpec.DoubleValue _distract;
    private final ForgeConfigSpec.DoubleValue _unaware;
    private final ForgeConfigSpec.BooleanValue _ignore;
    private final ForgeConfigSpec.IntValue _baseDetectionHorizontal;
    private final ForgeConfigSpec.IntValue _baseDetectionVertical;
    private final ForgeConfigSpec.IntValue _shoutSize;

    public StealthConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        _stab = b.translation("wardance.config.stabby").comment("enable or disable the entire system").define("enable stabbing", true);
        _removeInv = b.translation("wardance.config.removeInvis").comment("whether invisibility will be removed on attack").define("attacking dispels invisibility", true);
        _player = b.translation("wardance.config.player").comment("whether you must pass stealth checks to perceive a mob. Currently rather incomplete. I am not responsible for ragequits caused by this option. This physically ceases to work with Optifine installed.").define("use player senses", false);
        _baseDetectionHorizontal = b.translation("wardance.config.detectH").comment("angle of detection on the xz plane").defineInRange("default mob horizontal FoV", 120, 0, 360);
        _baseDetectionVertical = b.translation("wardance.config.detectV").comment("angle of detection on the y axis").defineInRange("default mob vertical FoV", 60, 0, 360);
        _shoutSize = b.translation("wardance.config.shoutSize").comment("how far a shout travels, in blocks").defineInRange("shouting sound size", 16, 0, Integer.MAX_VALUE);
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
                "\n(o)bservant mobs ignore invisibility. " +
                "\n(p)erceptive mobs ignore motion multipliers. " +
                "\n(q)uiet mobs do not broadcast sound cues, even if they do make sounds. " +
                "\n(s)keptical mobs will turn around before beginning to attack if you fail your luck check. " +
                "\n(v)igilant mobs are treated as alert even without an attack or revenge target."+
                "\n(w)ary mobs ignore luck. "
        ).defineList("mob detection rules", Arrays.asList(SNEAK), String.class::isInstance);
        _sounds = b.translation("wardance.config.sound").comment("Define which sounds generate cues for mobs to detect, followed by their size. Use *snippet to select all sounds that include the snippet in their full name. The list is processed top-down, so putting *tags first will allow you to override specific ones later. Shouting disregards this and always generates a sound cue of the defined radius, regardless of which sound clients have it set as.").defineList("sound cue list", Arrays.asList(SOUND), String.class::isInstance);
    }

    private static void bake() {
        stealthSystem = CONFIG._stab.get();
        distract = stealthSystem ? CONFIG._distract.get().floatValue() : 1;
        unaware = stealthSystem ? CONFIG._unaware.get().floatValue() : 1;
        ignore = stealthSystem & CONFIG._ignore.get();
        shout = CONFIG._shoutSize.get();
        baseHorizontalDetection = CONFIG._baseDetectionHorizontal.get();
        baseVerticalDetection = CONFIG._baseDetectionVertical.get();
        inv = CONFIG._removeInv.get();
        playerStealth=CONFIG._player.get();
        StealthUtils.updateMobDetection(CONFIG._customDetection.get());
        StealthUtils.updateSound(CONFIG._sounds.get());
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
