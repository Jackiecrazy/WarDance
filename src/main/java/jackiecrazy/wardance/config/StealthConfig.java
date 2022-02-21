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
    private static final String[] SNEAK = {"minecraft:bat, nv",
            "minecraft:bee, v",
            "minecraft:cat, v",
            "minecraft:cave_spider, n",
            "minecraft:chicken, v",
            "minecraft:cod, v",
            "minecraft:cow, v",
            "minecraft:creeper, o",
            "minecraft:dolphin, v",
            "minecraft:donkey, v",
            "minecraft:drowned, n",
            "minecraft:elder_guardian, na",
            "minecraft:ender_dragon, a",
            "minecraft:enderman, nao",
            "minecraft:endermite, n",
            "minecraft:evoker, o",
            "minecraft:fox, v",
            "minecraft:ghast, no",
            "minecraft:guardian, na",
            "minecraft:horse, v",
            "minecraft:husk, dn",
            "minecraft:illusioner, no",
            "minecraft:iron_golem, nao",
            "minecraft:llama, v",
            "minecraft:mooshroom, v",
            "minecraft:mule, v",
            "minecraft:ocelot, v",
            "minecraft:panda, v",
            "minecraft:parrot, v",
            "minecraft:phantom, noa",
            "minecraft:pig, v",
            "minecraft:pillager, o",
            "minecraft:polar_bear, v",
            "minecraft:pufferfish, v",
            "minecraft:rabbit, v",
            "minecraft:salmon, v",
            "minecraft:sheep, v",
            "minecraft:shulker, a",
            "minecraft:silverfish, n",
            "minecraft:skeleton, dno",
            "minecraft:skeleton_horse, dnv",
            "minecraft:slime, d",
            "minecraft:snow_golem, nov",
            "minecraft:spider, n",
            "minecraft:squid, v",
            "minecraft:stray, dno",
            "minecraft:strider, v",
            "minecraft:trader_llama, v",
            "minecraft:tropical_fish, v",
            "minecraft:turtle, v",
            "minecraft:vex, v",
            "minecraft:villager, v",
            "minecraft:vindicator, o",
            "minecraft:wandering_trader, mov",
            "minecraft:wither, nv",
            "minecraft:wither_skeleton, dno",
            "minecraft:zoglin, n",
            "minecraft:zombie, dn",
            "minecraft:zombie_horse, v",
            "minecraft:zombie_villager, n",
            "ars_nouveau:ally_vex, v",
            "ars_nouveau:carbuncle, v",
            "ars_nouveau:drygmy, v",
            "ars_nouveau:summon_horse, v",
            "ars_nouveau:summon_wolf, v",
            "ars_nouveau:sylph, v",
            "ars_nouveau:whelp, v",
            "ars_nouveau:wilden_guardian, np",
            "ars_nouveau:wilden_hunter, nop",
            "ars_nouveau:wilden_stalker, nop",
            "ars_nouveau:wilden_boss, nop",
            "ars_nouveau:familiar_bookwyrm, v",
            "ars_nouveau:familiar_carbuncle, v",
            "ars_nouveau:familiar_drygmy, v",
            "ars_nouveau:familiar_jabberwog, v",
            "ars_nouveau:familiar_sylph, v",
            "ars_nouveau:familiar_wixie, v",
            "ars_nouveau:blazing_weald_walker, v",
            "ars_nouveau:cascading_weald_walker, v",
            "ars_nouveau:flourishing_weald_walker, v",
            "ars_nouveau:vexing_weald_walker, v",
            "artifacts:mimic, v",
            "atum:assassin, v",
            "atum:bandit_warlord, o",
            "atum:barbarian, o",
            "atum:brigand, o",
            "atum:desert_wolf, o",
            "atum:forsaken, do",
            "atum:mummy, d",
            "atum:nomad, op",
            "atum:pharaoh, ov",
            "atum:serval, ao",
            "atum:stoneguard, dn",
            "atum:stoneguard_friendly, dnv",
            "atum:stonewarden, dn",
            "atum:stonewarden_friendly, dv",
            "atum:tarantula, n",
            "atum:wraith, no",
            "cavesandcliffs:axolotl, v",
            "cavesandcliffs:glow_squid, v",
            "cavesandcliffs:goat, v",
            "charm:coral_squid, v",
            "charm:moobloom, v",
            "conjurer_illager:conjurer, op",
            "darkerdepths:glowshroom_monster, op",
            "darkerdepths:magma_minion, op",
            "dungeons_mobs:armored_drowned, nd",
            "dungeons_mobs:armored_mountaineer, o",
            "dungeons_mobs:armored_piglin, n",
            "dungeons_mobs:armored_pillager, o",
            "dungeons_mobs:armored_skeleton, ndo",
            "dungeons_mobs:armored_sunken_skeleton, ndo",
            "dungeons_mobs:armored_vindicator, n",
            "dungeons_mobs:armored_zombie, nd",
            "dungeons_mobs:conjured_slime, n",
            "dungeons_mobs:drowned_necromancer, ndo",
            "dungeons_mobs:frozen_zombie, nd",
            "dungeons_mobs:fungus_thrower, nd",
            "dungeons_mobs:geomancer, o",
            "dungeons_mobs:iceologer, o",
            "dungeons_mobs:icy_creeper, o",
            "dungeons_mobs:illusioner, o",
            "dungeons_mobs:illusioner_clone, o",
            "dungeons_mobs:jungle_zombie, nd",
            "dungeons_mobs:leapleaf, o",
            "dungeons_mobs:mossy_skeleton, ndo",
            "dungeons_mobs:mountaineer, o",
            "dungeons_mobs:necromancer, ndo",
            "dungeons_mobs:poison_anemone, np",
            "dungeons_mobs:poison_quill_vine, np",
            "dungeons_mobs:quick_growing_anemone, np",
            "dungeons_mobs:quick_growing_vine, np",
            "dungeons_mobs:redstone_cube, ",
            "dungeons_mobs:redstone_golem, nop",
            "dungeons_mobs:royal_guard, op",
            "dungeons_mobs:skeleton_horseman, ndop",
            "dungeons_mobs:skeleton_vanguard, nd",
            "dungeons_mobs:slimeball, p",
            "dungeons_mobs:squall_golem, nop",
            "dungeons_mobs:sunken_skeleton, no",
            "dungeons_mobs:vindicator_chef, o",
            "dungeons_mobs:wavewhisperer, n",
            "dungeons_mobs:whisperer, n",
            "dungeons_mobs:windcaller, n",
            "dungeons_mobs:wraith, no",
            "dungeons_mobs:zombified_armored_piglin, nd",
            "dungeons_mobs:zombified_fungus_thrower, nd",
            "doggytalents:dog, v",
            "eidolon:necromancer, no",
            "eidolon:wraith, m",
            "eidolon:zombie_brute, d",
            "endergetic:bolloom_balloon, v",
            "endergetic:booflo, v",
            "endergetic:booflo_adolescent, v",
            "endergetic:booflo_baby, v",
            "endermail:ender_mailman, v",
            "farmingforblockheads:merchant, v",
            "guardvillagers:guard, o",
            "iceandfire:cockatrice, ao",
            "iceandfire:deathworm, na",
            "iceandfire:dread_beast, no",
            "iceandfire:dread_ghoul, dn",
            "iceandfire:dread_horse, n",
            "iceandfire:dread_knight, no",
            "iceandfire:dread_lich, no",
            "iceandfire:dread_scuttler, no",
            "iceandfire:dread_thrall, do",
            "iceandfire:fire_dragon, no",
            "iceandfire:ghost, nao",
            "iceandfire:gorgon, nao",
            "iceandfire:hippocampus, ov",
            "iceandfire:hippogryph, o",
            "iceandfire:hydra, o",
            "iceandfire:ice_dragon, no",
            "iceandfire:lightning_dragon, o",
            "iceandfire:myrmex_queen, n",
            "iceandfire:myrmex_royal, n",
            "iceandfire:myrmex_sentinel, n",
            "iceandfire:myrmex_soldier, n",
            "iceandfire:myrmex_swarmer, n",
            "iceandfire:myrmex_worker, n",
            "iceandfire:pixie, no",
            "iceandfire:siren, v",
            "iceandfire:stymphalian_bird, ",
            "iceandfire:sea_serpent, nop",
            "iceandfire:troll, n",
            "inventorypets:anvil_pet_entity, v",
            "inventorypets:bed_pet_entity, v",
            "inventorypets:bill_gates_entity, v",
            "inventorypets:mini_quantum_blaze_entity, v",
            "inventorypets:mini_quantum_enderman_entity, v",
            "inventorypets:satya_nadella_entity, v",
            "inventorypets:siamese_entity, v",
            "inventorypets:steve_ballmer_entity, v",
            "losttrinkets:dark_vex, v",
            "majruszs_difficulty:creeperling, o",
            "majruszs_difficulty:elite_skeleton, dna",
            "majruszs_difficulty:pillager_wolf, a",
            "majruszs_difficulty:sky_keeper, nao",
            "meetyourfight:bellringer, v",
            "meetyourfight:dame_fortuna, v",
            "meetyourfight:swampjaw, v",
            "mowziesmobs:barako, v",
            "mowziesmobs:barakoan_player, v",
            "mowziesmobs:ferrous_wroughtnaut, v",
            "mowziesmobs:foliaath, av",
            "mowziesmobs:frostmaw, o",
            "mowziesmobs:grottol, naov",
            "mowziesmobs:lantern, v",
            "mowziesmobs:naga, a",
            "mutantbeasts:creeper_minion, v",
            "mutantbeasts:endersoul_clone, v",
            "mutantbeasts:mutant_enderman, naop",
            "mutantbeasts:mutant_skeleton, dno",
            "mutantbeasts:mutant_snow_golem, v",
            "mutantbeasts:mutant_zombie, n",
            "mutantbeasts:spider_pig, n",
            "mysticalworld:beetle, v",
            "mysticalworld:deer, v",
            "mysticalworld:endermini, nao",
            "mysticalworld:frog, v",
            "mysticalworld:hell_sprout, v",
            "mysticalworld:lava_cat, v",
            "mysticalworld:owl, v",
            "mysticalworld:silkworm, v",
            "mysticalworld:silver_fox, v",
            "mysticalworld:sprout, v",
            "outer_end:chorus_squid, v",
            "outer_end:entombed, no",
            "outer_end:purpur_golem, no",
            "outer_end:spectrafly, v",
            "outer_end:stalker, n",
            "quark:crab, v",
            "quark:forgotten, dnao",
            "quark:foxhound, o",
            "quark:frog, v",
            "quark:shiba, v",
            "quark:stoneling, v",
            "quark:toretoise, n",
            "quark:wraith, n",
            "quark:wrapped, d",
            "switchbow:entitylittleirongolem, av",
            "villagertools:guard, o",
            "villagertools:reinforced_golem, na",
            "mutantmore:mutant_blaze, no",
            "mutantmore:mutant_hoglin, no",
            "mutantmore:mutant_husk, ndo",
            "mutantmore:mutant_shulker, no",
            "mutantmore:mutant_wither_skeleton, ndop",
            "mutantmore:rodling, no",
            "savageandravage:creepie, ",
            "savageandravage:executioner, o",
            "savageandravage:griefer, o",
            "savageandravage:iceologer, o",
            "savageandravage:skeleton_villager, no",
            "thermal:basalz, no",
            "thermal:blitz, no",
            "thermal:blizz, no"
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
        _blockPerVolume = b.translation("wardance.config.volume").comment("this value is multiplied by the volume of a sound to determine how far it'll alert mobs to investigate from. Large values will GREATLY impact performance, you have been warned.").defineInRange("alert volume multiplier", 8, 0, Double.MAX_VALUE);
        _distract = b.translation("wardance.config.distract").comment("posture and health damage multiplier for distracted stabs").defineInRange("distracted stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _unaware = b.translation("wardance.config.unaware").comment("posture and health damage multiplier for unaware stabs").defineInRange("unaware stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _ignore = b.translation("wardance.config.ignore").comment("whether unaware stabs ignore parry, deflection, shatter, and absorption").define("unaware stab defense ignore", true);
        _customDetection = b.translation("wardance.config.mobDetection").comment("Define custom detection mechanics for mobs by tagging them as one of the following: \n(a)ll-seeing mobs ignore LoS modifiers. \n(d)eaf mobs ignore sound cues and do not factor in the armor debuff assigned to darkness-induced stealth. \n(n)octurnal mobs ignore light level. \n(o)lfactory mobs ignore luck. \n(p)erceptive mobs ignore motion multipliers. \n(v)igilant mobs bypass the entire stealth and distraction system.").defineList("mob detection rules", Arrays.asList(SNEAK), String.class::isInstance);
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
