package jackiecrazy.wardance.config;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CombatConfig {
    public static final CombatConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final String[] MOBS = {
            "minecraft:bat, -1.0, 1.0",
            "minecraft:bee, -1.0, 1.0",
            "minecraft:blaze, 1.5, 0.33",
            "minecraft:cave_spider, -1.0, 1.0",
            "minecraft:chicken, -1.0, 1.0",
            "minecraft:cod, -1.0, 1.0",
            "minecraft:cow, -1.0, 1.0",
            "minecraft:creeper, -1.0, 1.0",
            "minecraft:dolphin, -1.0, 1.0",
            "minecraft:donkey, -1.0, 1.0",
            "minecraft:elder_guardian, 0.8, 0.66",
            "minecraft:ender_dragon, 0.6, 0.66",
            "minecraft:evoker, 2.0, 0.33",
            "minecraft:fox, -1.0, 1.0",
            "minecraft:ghast, -1.0, 1.0",
            "minecraft:guardian, 0.9, 0.4",
            "minecraft:hoglin, -1.0, 1.0",
            "minecraft:horse, -1.0, 1.0",
            "minecraft:illusioner, 2.0, 0.4",
            "minecraft:iron_golem, 0.7, 0.75",
            "minecraft:llama, -1.0, 1.0",
            "minecraft:magma_cube, -1.0, 1.0",
            "minecraft:mooshroom, -1.0, 1.0",
            "minecraft:mule, -1.0, 1.0",
            "minecraft:parrot, -1.0, 1.0",
            "minecraft:phantom, -1.0, 1.0",
            "minecraft:pig, -1.0, 1.0",
            "minecraft:piglin_brute, 0.9, 0.33",
            "minecraft:pillager, -1.0, 1.0",
            "minecraft:polar_bear, -1.0, 1.0",
            "minecraft:pufferfish, -1.0, 1.0",
            "minecraft:rabbit, -1.0, 1.0",
            "minecraft:ravager, -1.0, 0.33",
            "minecraft:salmon, -1.0, 1.0",
            "minecraft:sheep, -1.0, 1.0",
            "minecraft:shulker, 0.6, 0.5",
            "minecraft:silverfish, -1.0, 1.0",
            "minecraft:skeleton_horse, -1.0, 1.0",
            "minecraft:slime, -1.0, 1.0",
            "minecraft:spider, -1.0, 1.0",
            "minecraft:squid, -1.0, 1.0",
            "minecraft:strider, -1.0, 1.0",
            "minecraft:trader_llama, -1.0, 1.0",
            "minecraft:tropical_fish, -1.0, 1.0",
            "minecraft:turtle, -1.0, 1.0",
            "minecraft:wandering_trader, -1.0, 1.0",
            "minecraft:wither, 1.0, 0.33",
            "minecraft:zoglin, -1.0, 1.0",
            "minecraft:zombie_horse, -1.0, 1.0",
            "ars_nouveau:wilden_boss, 0.9, 0.66",
            "ars_nouveau:wilden_guardian, 0.9, 0.5",
            "ars_nouveau:wilden_hunter, 1.2, 0.5",
            "artifacts:mimic, 0.7, 0.66",
            "atum:assassin, 1.6, 0.75",
            "atum:bandit_warlord, 1.4, 0.5",
            "atum:barbarian, 1.4, 0.5",
            "atum:bonestorm, 1.5, 0.33",
            "atum:brigand, 1.5, 0.33",
            "atum:camel, -1.0, 1.0",
            "atum:desert_rabbit, -1.0, 1.0",
            "atum:nomad, 2.0, 0.5",
            "atum:pharaoh, 0.9, 0.75",
            "atum:quail, -1.0, 1.0",
            "atum:scarab, -1.0, 1.0",
            "atum:sergeant, 1.4, 0.5",
            "atum:stoneguard, 0.8, 0.75",
            "atum:stoneguard_friendly, 0.8, 0.75",
            "atum:stonewarden, 0.7, 1.0",
            "atum:stonewarden_friendly, 0.7, 1.0",
            "atum:tarantula, -1.0, 1.0",
            "cavesandcliffs:goat, 1.5, 0.33",
            "conjurer_illager:conjurer, 1.6, 0.66",
            "darkerdepths:glowshroom_monster, 0.9, 0.4",
            "dungeons_mobs:armored_drowned, 1.2, 0.4",
            "dungeons_mobs:armored_mountaineer, 1.2, 0.4",
            "dungeons_mobs:armored_piglin, 1.2, 0.4",
            "dungeons_mobs:armored_pillager, 1.2, 0.4",
            "dungeons_mobs:armored_skeleton, 1.2, 0.4",
            "dungeons_mobs:armored_sunken_skeleton, 1.2, 0.4",
            "dungeons_mobs:armored_vindicator, 1.2, 0.4",
            "dungeons_mobs:armored_zombie, 1.2, 0.4",
            "dungeons_mobs:frozen_zombie, 1.5, 0.5",
            "dungeons_mobs:geomancer, 1.5, 0.66",
            "dungeons_mobs:icy_creeper, 1.5, 0.5",
            "dungeons_mobs:illusioner, 1.5, 0.75",
            "dungeons_mobs:illusioner_clone, 2.0, 0.33",
            "dungeons_mobs:mountaineer, 1.2, 0.5",
            "dungeons_mobs:necromancer, 1.5, 0.5",
            "dungeons_mobs:redstone_cube, 1.0, 0.66",
            "dungeons_mobs:redstone_golem, 0.7, 0.75",
            "dungeons_mobs:royal_guard, 0.8, 0.9",
            "dungeons_mobs:squall_golem, 0.7, 0.75",
            "dungeons_mobs:zombified_armored_piglin, 1.0, 0.4",
            "eidolon:zombie_brute, 1.2, 0.4",
            "guardvillagers:guard, 1.4, 0.5",
            "iceandfire:amphithere, 1.2, 0.45",
            "iceandfire:cockatrice, 1.6, 0.33",
            "iceandfire:dread_knight, 1.2, 0.66",
            "iceandfire:dread_lich, 1.5, 0.5",
            "iceandfire:fire_dragon, 1.0, 0.5",
            "iceandfire:gorgon, 1.2, 0.75",
            "iceandfire:ice_dragon, 1.0, 0.5",
            "iceandfire:lightning_dragon, 1.0, 0.5",
            "iceandfire:pixie, 3.0, 0.75",
            "iceandfire:sea_serpent, 1.2, 0.5",
            "iceandfire:stymphalian_bird, 1.6, 0.33",
            "iceandfire:troll, 0.6, 0.66",
            "inventorypets:anvil_pet_entity, 0.6, 1.0",
            "inventorypets:bed_pet_entity, 0.6, 1.0",
            "inventorypets:mini_quantum_blaze_entity, 1.5, 0.33",
            "majruszs_difficulty:elite_skeleton, 1.4, 0.5",
            "majruszs_difficulty:giant, 1.0, 0.9",
            "majruszs_difficulty:pillager_wolf, 0.9, 1.0",
            "majruszs_difficulty:sky_keeper, -1.0, 1.0",
            "meetyourfight:bellringer, 1.6, 0.33",
            "meetyourfight:dame_fortuna, 1.6, 0.33",
            "meetyourfight:swampjaw, -1.0, 1.0",
            "mowziesmobs:baby_foliaath, -1.0, 1.0",
            "mowziesmobs:barakoan_barakoana, 1.4, 0.5",
            "mowziesmobs:barakoan_player, 1.4, 0.5",
            "mowziesmobs:barakoana, 1.4, 0.5",
            "mowziesmobs:barakoaya, 1.4, 0.5",
            "mowziesmobs:ferrous_wroughtnaut, -1.0, 1.0",
            "mowziesmobs:foliaath, -1.0, 1.0",
            "mowziesmobs:grottol, -1.0, 1.0",
            "mowziesmobs:lantern, -1.0, 1.0",
            "mowziesmobs:naga, -1.0, 1.0",
            "mutantbeasts:creeper_minion, -1.0, 1.0",
            "mutantbeasts:endersoul_clone, 2.0, 0.33",
            "mutantbeasts:mutant_creeper, -1.0, 1.0",
            "mutantbeasts:mutant_enderman, 1.5, 0.33",
            "mutantbeasts:mutant_skeleton, 1.0, 0.66",
            "mutantbeasts:mutant_snow_golem, 0.9, 0.5",
            "mutantbeasts:mutant_zombie, -1.0, 1.0",
            "mutantbeasts:spider_pig, -1.0, 1.0",
            "mutantmore:mutant_blaze, 1.3, 0.4",
            "mutantmore:mutant_hoglin, 0.9, 0.5",
            "mutantmore:mutant_shulker, 0.8, 0.8",
            "mutantmore:mutant_wither_skeleton, 0.9, 0.75",
            "mutantmore:rodling, -1.0, 1.0",
            "mysticalworld:beetle, -1.0, 1.0",
            "mysticalworld:deer, -1.0, 1.0",
            "mysticalworld:duck, -1.0, 1.0",
            "mysticalworld:frog, -1.0, 1.0",
            "mysticalworld:hell_sprout, -1.0, 1.0",
            "mysticalworld:lava_cat, -1.0, 1.0",
            "mysticalworld:owl, -1.0, 1.0",
            "mysticalworld:silkworm, -1.0, 1.0",
            "mysticalworld:silver_fox, -1.0, 1.0",
            "mysticalworld:sprout, -1.0, 1.0",
            "outer_end:chorus_squid, -1.0, 1.0",
            "outer_end:entombed, 0.8, 0.66",
            "outer_end:himmelite, 1.2, 0.5",
            "outer_end:purpur_golem, 0.7, 0.8",
            "outer_end:spectrafly, -1.0, 1.0",
            "outer_end:stalker, -1.0, 1.0",
            "quark:crab, -1.0, 1.0",
            "quark:frog, -1.0, 1.0",
            "quark:shiba, -1.0, 1.0",
            "quark:stoneling, 0.8, 0.25",
            "quark:toretoise, 0.8, 0.66",
            "savageandravage:creepie, -1.0, 1.0",
            "savageandravage:griefer, -1.0, 1.0",
            "savageandravage:iceologer, -1.0, 1.0",
            "switchbow:entitylittleirongolem, 0.8, 0.66",
            "thermal:basalz, 1.5, 0.33",
            "thermal:blitz, 1.5, 0.33",
            "thermal:blizz, 1.5, 0.33",
            "tombstone:grave_guardian, 0.9, 0.8",
            "villagertools:reinforced_golem, 0.7, 1.0"
    };
    private static final String[] PROJECTILES = {
            "apotheosis:bh_arrow_entity, 3.0, 2, d",
            "apotheosis:ex_arrow_entity, 2.0, 2, t",
            "apotheosis:mn_arrow_entity, -1, 0",
            "apotheosis:ob_arrow_entity, 2.5, 2",
            "archers_paradox:blaze_arrow, 2.0, 1, d",
            "archers_paradox:challenge_arrow, 3.0, 2, t",
            "archers_paradox:diamond_arrow, -1, 3",
            "archers_paradox:displacement_arrow, 3.0, 1, t",
            "archers_paradox:ender_arrow, 5.0, 1, t",
            "archers_paradox:explosive_arrow, 5.0, 3, t",
            "archers_paradox:frost_arrow, 2.0, 1, t",
            "archers_paradox:glowstone_arrow, 2.0, 1, t",
            "archers_paradox:lightning_arrow, 2.0, 1, t",
            "archers_paradox:phantasmal_arrow, -1, 1",
            "archers_paradox:prismarine_arrow, 2.0, 1",
            "archers_paradox:quartz_arrow, 2.5, 1, t",
            "archers_paradox:redstone_arrow, 1.0, 1, t",
            "archers_paradox:shulker_arrow, 1.5, 2",
            "archers_paradox:slime_arrow, 3.0, 1, t",
            "archers_paradox:spore_arrow, 2.0, 1, t",
            "archers_paradox:training_arrow, 3.0, 1, t",
            "archers_paradox:verdant_arrow, 1.0, 1, t",
            "ars_nouveau:an_lightning, -1, 1",
            "ars_nouveau:fangs, -1, 1",
            "ars_nouveau:flying_item, 2.0, 1",
            "ars_nouveau:follow_proj, 1.5, 1",
            "ars_nouveau:linger, 2.0, 1",
            "ars_nouveau:spell_arrow, 2.0, 1, t",
            "ars_nouveau:spell_proj, 2.0, 1, d",
            "ars_nouveau:spike, 3.0, 2, d",
            "atmospheric:passionfruit_seed, 0.5, 1, d",
            "atum:arrow_double, 2.0, 1, d",
            "atum:arrow_explosive, 5.0, 3, t",
            "atum:arrow_fire, 2.0, 1, t",
            "atum:arrow_poison, 2.0, 1, t",
            "atum:arrow_quickdraw, 2.0, 1, d",
            "atum:arrow_rain, 2.5, 1, d",
            "atum:arrow_slowness, 2.0, 1, d",
            "atum:arrow_straight, 2.0, 1, d",
            "atum:pharaoh_orb, 4.0, 2, d",
            "atum:quail_egg, 1.0, 1, t",
            "atum:small_bone, 1.0, 1",
            "atum:tefnuts_call, -1, 3",
            "bloodmagic:soulsnare, 5.0, 1, d",
            "bloodmagic:throwing_dagger, 2.0, 1, d",
            "bloodmagic:throwing_dagger_syringe, 2.0, 1, d",
            "champions:arctic_bullet, 3.0, 1, d",
            "champions:enkindling_bullet, 3.0, 1, d",
            "charm:glowball, 1.0, 1, t",
            "conjurer_illager:bouncing_ball, 2.0, 1",
            "conjurer_illager:throwing_card, 1.0, 1, d",
            "cyclic:boomerang_carry, 1.0, 1",
            "cyclic:boomerang_damage, 3.0, 1",
            "cyclic:boomerang_stun, 5.0, 2",
            "cyclic:fire_bolt, 2.0, 1, d",
            "cyclic:lightning_bolt, 1.0, 1, t",
            "cyclic:snow_bolt, 1.0, 1, d",
            "cyclic:stone_bolt, 2.0, 2",
            "cyclic:torch_bolt, 2.0, 2, t",
            "doggytalents:dog_beam, -1, 1",
            "dungeons_gear:ice_cloud, -1, 1",
            "dungeons_mobs:blue_nethershroom, 2.0, 1, t",
            "dungeons_mobs:cobweb_projectile, -1, 1",
            "dungeons_mobs:cobweb_trap, -1, 1",
            "dungeons_mobs:geomancer_bomb, -1, 1",
            "dungeons_mobs:geomancer_wall, -1, 1",
            "dungeons_mobs:ice_cloud, -1, 1",
            "dungeons_mobs:laser_orb, 3.0, 1",
            "dungeons_mobs:redstone_mine, -1, 1",
            "dungeons_mobs:slimeball, 1.0, 1, d",
            "dungeons_mobs:tornado, -1, 1",
            "dungeons_mobs:wraith_fireball, 3.0, 1",
            "eidolon:bonechill_projectile, 2.0, 1",
            "eidolon:soulfire_projectile, 2.0, 1",
            "enigmaticlegacy:enigmatic_potion_entity, 2, 1, t",
            "enigmaticlegacy:ultimate_wither_skull_entity, 4.0, 2",
            "environmental:duck_egg, 1.0, 1, t",
            "environmental:mud_ball, 0.5, 1, t",
            "grapplemod:grapplehook, -1, 1",
            "gunswithoutroses:bullet, 1.0, 1",
            "iceandfire:amphithere_arrow, 2.0, 1",
            "iceandfire:cockatrice_egg, 2.0, 1, t",
            "iceandfire:dragon_arrow, 4.0, 2",
            "iceandfire:dread_lich_skull, 3.0, 2, t",
            "iceandfire:fire_dragon_charge, 8.0, 3",
            "iceandfire:ghost_sword, 2.0, 1",
            "iceandfire:hydra_arrow, 3.0, 2",
            "iceandfire:hydra_breath, 1.0, 1, d",
            "iceandfire:ice_dragon_charge, 8.0, 3",
            "iceandfire:lightning_dragon_charge, 8.0, 3",
            "iceandfire:pixie_charge, -1, 1",
            "iceandfire:sea_serpent_arrow, 3.5, 1",
            "iceandfire:stymphalian_arrow, 4.0, 1",
            "iceandfire:stymphalian_feather, 3.0, 1, d",
            "iceandfire:tide_trident, -1, 1",
            "immersiveengineering:chemthrower_shot, 2.0, 1, d",
            "immersiveengineering:railgun_shot, -1, 20",
            "immersiveengineering:revolver_shot_flare, 2.0, 2, t",
            "immersiveengineering:revolver_shot_homing, 2.0, 2, d",
            "immersiveengineering:revolver_shot_wolfpack, 2.0, 2, d",
            "immersiveengineering:revolvershot, 2.0, 2, d",
            "immersiveengineering:sawblade, 8.0, 3",
            "infernalexp:ascus_bomb, 3.0, 2, t",
            "infernalexp:throwable_brick, 2.0, 2",
            "infernalexp:throwable_fire_charge, 2.0, 2, d",
            "infernalexp:throwable_magma_cream, 1.0, 2, d",
            "infernalexp:throwable_nether_brick, 2.0, 2",
            "inventorypets:apple_entity, 4.0, 1, d",
            "inventorypets:banana_entity, 3.0, 1, d",
            "inventorypets:golden_apple_entity, -1, 1",
            "meetyourfight:projectile_line, 2.0, 1",
            "meetyourfight:swamp_mine, 2.0, 1",
            "minecraft:arrow, 2.0, 1",
            "minecraft:dragon_fireball, 5.0, 3, t",
            "minecraft:egg, 1.0, 1, t",
            "minecraft:ender_pearl, 5.0, 3, t",
            "minecraft:experience_bottle, -1, 1",
            "minecraft:fireball, 4.0, 2",
            "minecraft:firework_rocket, 3.0, 2, t",
            "minecraft:fishing_bobber, -1, 1",
            "minecraft:lightning_bolt, -1, 1",
            "minecraft:llama_spit, 1.0, 1",
            "minecraft:potion, 1.0, 1, t",
            "minecraft:shulker_bullet, 4.0, 2, d",
            "minecraft:small_fireball, 2.0, 1, d",
            "minecraft:snowball, 1.0, 1, d",
            "minecraft:spectral_arrow, 3.0, 2",
            "minecraft:trident, 6.0, 3",
            "minecraft:wither_skull, 4.0, 2",
            "mod_lavacow:acidjet, 2.0, 3, t",
            "mod_lavacow:deathcoil, 3.0, 2",
            "mod_lavacow:flamejet, 2.0, 2",
            "mod_lavacow:ghostbomb, 4.0, 3, t",
            "mod_lavacow:holygrenade, 3.0, 2, t",
            "mod_lavacow:piranhalauncher, 2.0, 2, t",
            "mod_lavacow:sandburst, 1.0, 1, d",
            "mod_lavacow:sludgejet, 2.0, 2",
            "mod_lavacow:sonicbomb, 4.0, 3, t",
            "mod_lavacow:warsmallfireball, 1.0, 1, d",
            "mowziesmobs:axe_attack, 6.0, 3",
            "mowziesmobs:boulder_huge, 20.0, 5, t",
            "mowziesmobs:boulder_large, 15.0, 4, t",
            "mowziesmobs:boulder_medium, 10.0, 3, t",
            "mowziesmobs:boulder_small, 5.0, 2, t",
            "mowziesmobs:dart, 1.0, 1, d",
            "mowziesmobs:ice_ball, 10.0, 2, d",
            "mowziesmobs:poison_ball, 2.0, 1",
            "mutantbeasts:chemical_x, 1.0, 1, t",
            "mutantbeasts:throwable_block, 5.0, 2, t",
            "mutantmore:rodling_fireball, 1.0, 1, d",
            "mutantmore:seismic_wave, 8.0, 4",
            "mutantmore:thrown_block, 5.0, 2, t",
            "projecte:fire_projectile, 3.0, 1",
            "psi:spell_charge, 2.0, 1, t",
            "psi:spell_grenade, 4.0, 1, t",
            "psi:spell_projectile, 2.0, 1, t",
            "quark:pickarang, 6.0, 2",
            "relics:shadow_glaive, 5, 2, t",
            "relics:space_dissector, 5, 2, t",
            "relics:stellar_catalyst_projectile, 8.0, 3, t",
            "savageandravage:ice_chunk, 8.0, 3",
            "savageandravage:mischief_arrow, 2.0, 1, t",
            "savageandravage:spore_bomb, 2.0, 1, t",
            "securitycraft:bouncingbetty, 15, 3, t",
            "securitycraft:bullet, 2.0, 1, d",
            "securitycraft:imsbomb, 5.0, 3, t",
            "spartanweaponry:arrow, 4.0, 2",
            "spartanweaponry:arrow_explosive, 5.0, 3, t",
            "spartanweaponry:bolt, 5.0, 3",
            "spartanweaponry:bolt_spectral, 6.0, 3",
            "spartanweaponry:boomerang, 6.0, 2, 2",
            "spartanweaponry:dynamite, 5.0, 2, t",
            "spartanweaponry:javelin, 6.0, 3",
            "spartanweaponry:throwing_weapon, 4.0, 2",
            "spartanweaponry:tomahawk, 4.0, 2",
            "switchbow:entityarrowair, 2.0, 1",
            "switchbow:entityarrowairupgrade, 3.0, 1",
            "switchbow:entityarrowbone, 3.0, 1",
            "switchbow:entityarrowboneaoe, 3.0, 1",
            "switchbow:entityarrowbouncy, 3.0, 2, t",
            "switchbow:entityarrowburial, 3.0, 2, t",
            "switchbow:entityarrowburialaoe, 3.0, 2, t",
            "switchbow:entityarrowchorus, 5.0, 2, t",
            "switchbow:entityarrowdiamond, 6.0, 3",
            "switchbow:entityarrowdragonbreath, 3.0, 1, t",
            "switchbow:entityarrowenderperle, 3.0, 2, t",
            "switchbow:entityarrowfire, 3.0, 2, t",
            "switchbow:entityarrowfireupgrade, 2.0, 3, t",
            "switchbow:entityarrowflytomoon, 20.0, 4, t",
            "switchbow:entityarrowfrost, 3.0, 2, t",
            "switchbow:entityarrowknockback, 5.0, 3, t",
            "switchbow:entityarrowlightningbolt, 3.0, 1, t",
            "switchbow:entityarrowlightningboltstorm, 3.0, 1, t",
            "switchbow:entityarrowlove, 2.0, 1, t",
            "switchbow:entityarrowluck, 2.0, 1",
            "switchbow:entityarrowluckupgrade, 2.0, 1",
            "switchbow:entityarrowofthesearcher, 3.0, 1, d",
            "switchbow:entityarrowpiercing, 4.0, 3, t",
            "switchbow:entityarrowprotector, 4.0, 2, t",
            "switchbow:entityarrowredstone, 3.0, 1, t",
            "switchbow:entityarrowsplit, 3.0, 1, d",
            "switchbow:entityarrowsplitlove, 2.0, 1",
            "switchbow:entityarrowsplitloveupgrade, 2.0, 1",
            "switchbow:entityarrowsplitupgrade, 2.0, 1",
            "switchbow:entityarrowsprinkler, 2.0, 1",
            "switchbow:entityarrowtnt, 5.0, 3, t",
            "switchbow:entityarrowtorch, 3.0, 1",
            "switchbow:entityarrowtriple, 2.0, 1",
            "switchbow:entityarrowunderwater, 3.0, 1",
            "switchbow:entityarrowvampier, 3.0, 1",
            "switchbow:entityarrowwither, 3.0, 1",
            "thermal:basalz_projectile, 4.0, 2",
            "thermal:blitz_projectile, 3.0, 1",
            "thermal:blizz_projectile, 3.0, 1",
            "thermal:earth_grenade, 5.0, 3, t",
            "thermal:ender_grenade, 5.0, 3, t",
            "thermal:fire_grenade, 5.0, 3, t",
            "thermal:glowstone_grenade, 5.0, 3, t",
            "thermal:grenade, 5.0, 3, t",
            "thermal:ice_grenade, 5.0, 3, t",
            "thermal:lightning_grenade, 5.0, 3, t",
            "thermal:nuke_grenade, 5.0, 3, t",
            "thermal:phyto_grenade, 5.0, 3, t",
            "thermal:redstone_grenade, 5.0, 3, t",
            "thermal:slime_grenade, 5.0, 3, t",
            "vanillatweaks:dynamite, 5.0, 2, t",
            "wardance:thrown_weapon, 5.0, 2, t",
            "weaponthrow:weaponthrow, 5.0, 2, t",
            "xreliquary:aphrodite_potion, -1, 1",
            "xreliquary:blaze_shot, 4.0, 2",
            "xreliquary:buster_shot, 4.0, 2, t",
            "xreliquary:concussive_shot, 4.0, 2, t",
            "xreliquary:ender_shot, 4.0, 2, t",
            "xreliquary:exorcism_shot, 4.0, 2",
            "xreliquary:fertile_potion, -1, 1",
            "xreliquary:glowing_water, -1, 1",
            "xreliquary:holy_hand_grenade, 8, 5, t",
            "xreliquary:kraken_slime, 1.0, 1, t",
            "xreliquary:lyssa_hook, 3.0, 1, t",
            "xreliquary:neutral_shot, 4.0, 2",
            "xreliquary:sand_shot, 4.0, 2, t",
            "xreliquary:seeker_shot, 4.0, 2",
            "xreliquary:special_snowball, 1.0, 1",
            "xreliquary:storm_shot, 4.0, 2, t",
            "xreliquary:thrown_potion, 2.0, 1, t",
            "xreliquary:tipped_arrow, 3.0, 2, t"
    };
    public static float posturePerProjectile;
    public static float defaultMultiplierPostureDefend;
    public static float defaultMultiplierPostureAttack;
    public static float defaultMultiplierPostureMob;
    public static int rollEndsAt;
    public static int rollCooldown;
    public static int shieldCooldown;
    public static float barrierSize;
    public static int staggerDuration;
    public static int staggerDurationMin;
    public static int staggerHits, adrenaline;
    public static float staggerDamage;
    public static float unStaggerDamage;
    public static int sneakParry;
    public static int recovery;
    public static int foodCool;
    public static float mobParryChanceWeapon, mobParryChanceShield, mobDeflectChance, mobScaler;
    public static float posCap;
    public static boolean dodge;
    public static float kbNerf;

    static {
        final Pair<CombatConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CombatConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.DoubleValue _posturePerProjectile;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureMob;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureDefend;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureAttack;
    private final ForgeConfigSpec.IntValue _rollThreshold;
    private final ForgeConfigSpec.IntValue _rollCooldown;
    private final ForgeConfigSpec.IntValue _shieldThreshold;
    private final ForgeConfigSpec.DoubleValue _shieldCount;
    private final ForgeConfigSpec.IntValue _staggerDuration;
    private final ForgeConfigSpec.IntValue _staggerDurationMin;
    private final ForgeConfigSpec.IntValue _staggerHits;
    private final ForgeConfigSpec.IntValue _recovery;
    private final ForgeConfigSpec.BooleanValue _dodge;
    private final ForgeConfigSpec.IntValue _sneakParry;
    private final ForgeConfigSpec.IntValue _foodCool;
    private final ForgeConfigSpec.IntValue _adrenaline;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceWeapon;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceShield;
    private final ForgeConfigSpec.DoubleValue _mobDeflectChance;
    private final ForgeConfigSpec.DoubleValue _mobScaler;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customParry;
    private final ForgeConfigSpec.DoubleValue _posCap;
    private final ForgeConfigSpec.DoubleValue _stagger;
    private final ForgeConfigSpec.DoubleValue _unstagger;
    private final ForgeConfigSpec.DoubleValue _knockbackNerf;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customProjectile;

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
        _shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("default barrier cooldown", 16, 0, Integer.MAX_VALUE);
        _shieldCount = b.translation("wardance.config.shieldT").comment("This many parries are free after a parry that cost posture").defineInRange("default barrier size", 0.2, 0, Float.MAX_VALUE);
        _customProjectile = b.translation("wardance.config.projectilePosture").comment("Define custom projectile parrying behavior. Default list provided courtesy of DarkMega. Format is name, posture cost (negative to disable parrying this projectile), shield count cost, and a list of tags:\nProjectiles may be (d)estroyed upon parry.\nThey may also be allowed to (t)rigger their non-damage effects.").defineList("projectile parry rules", Arrays.asList(PROJECTILES), String.class::isInstance);
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
        _stagger = b.translation("wardance.config.stagger").comment("Extra damage taken by a staggered entity").defineInRange("stagger damage multiplier", 1.5, 0, Double.MAX_VALUE);
        _unstagger = b.translation("wardance.config.unstagger").comment("Damage taken by a non-staggered entity. Added out of curiosity.").defineInRange("normal damage multiplier", 1, 0, Double.MAX_VALUE);
        b.pop();
        b.push("difficulty");
        _defaultMultiplierPostureMob = b.translation("wardance.config.dmpm").comment("Default multiplier for mob attack posture, multiplied by their max posture. This is used when the mob is not wielding a weapon.").defineInRange("default mob multiplier", 0.2, 0, Double.MAX_VALUE);
        _mobParryChanceWeapon = b.translation("wardance.config.mobPW").comment("chance that a mob parries with a weapon out of 1. Hands are individually calculated.").defineInRange("mob weapon parry chance", 0.3, 0, 1);
        _mobParryChanceShield = b.translation("wardance.config.mobPS").comment("chance that a mob parries with a shield out of 1. Hands are individually calculated.").defineInRange("mob shield parry chance", 0.9, 0, 1);
        _mobDeflectChance = b.translation("wardance.config.mobD").comment("chance that a mob deflects with armor out of 1").defineInRange("mob deflect chance", 0.6, 0, 1);
        _customParry = b.translation("wardance.config.parryMobs").comment("Define mobs that are automatically capable of parrying. If the entity is simultaneously armed and capable of parry, the lowest multiplier will be chosen when the chance test passes. " +
                "Format is name, defense posture multiplier, parry chance. Hands are individually calculated for chance. " +
                "Filling in a negative parry multiplier will disable parrying regardless of weaponry with the defined chance, causing the damage to be multiplied by the negated parry multiplier.\n" +
                "Examples:\n" +
                "1, 0.3 has a 30% chance to parry with a multiplier of 1, independently of hand parrying, and will replace hand multiplier if it's better. \n" +
                "-1, -0.3 will have a 30% chance to fail a parry regardless of hand behavior, taking normal posture damage.\n" +
                "0, 1 will always parry for free. \n" +
                "-0, 1 can never parry nor take posture damage.\n" +
                "Additionally, you may tag mobs as (o)mnidirectional and/or (s)hielded.\n" +
                "Omnidirectional mobs can parry from any orientation\n" +
                "Shielded mobs can parry projectiles innately.").defineList("auto parry mobs", Arrays.asList(MOBS), String.class::isInstance);
        _mobScaler = b.translation("wardance.config.mobB").comment("posture damage from mob attacks will be scaled by this number.").defineInRange("mob posture damage buff", 1, 0, Double.MAX_VALUE);
        _knockbackNerf = b.translation("wardance.config.knockback").comment("knockback from all sources to everything will be multiplied by this amount").defineInRange("knockback multiplier", 1, 0, 10d);
        b.pop();
        b.push("misc");
        _foodCool = b.translation("wardance.config.foodCool").comment("number of ticks to disable a certain food item for after taking physical damage while eating it. Set to 0 to just interrupt eating, and -1 to disable this feature.").defineInRange("food disable time", 20, -1, Integer.MAX_VALUE);
        _adrenaline = b.translation("wardance.config.adrenaline").comment("number of ticks to halve adrenaline bonus after getting hit. Set to -1 to disable adrenaline altogether.").defineInRange("adrenaline downtime", 100, -1, Integer.MAX_VALUE);
        b.pop();
    }

    private static void bake() {
        posturePerProjectile = CONFIG._posturePerProjectile.get().floatValue();
        defaultMultiplierPostureDefend = CONFIG._defaultMultiplierPostureDefend.get().floatValue();
        defaultMultiplierPostureAttack = CONFIG._defaultMultiplierPostureAttack.get().floatValue();
        defaultMultiplierPostureMob = CONFIG._defaultMultiplierPostureMob.get().floatValue();
        rollCooldown = CONFIG._rollCooldown.get();
        rollEndsAt = rollCooldown - CONFIG._rollThreshold.get();
        shieldCooldown = CONFIG._shieldThreshold.get();
        barrierSize = CONFIG._shieldCount.get().floatValue();
        staggerDuration = CONFIG._staggerDuration.get();
        staggerDurationMin = CONFIG._staggerDurationMin.get();
        staggerHits = CONFIG._staggerHits.get();
        staggerDamage = CONFIG._stagger.get().floatValue();
        unStaggerDamage = CONFIG._unstagger.get().floatValue();
        mobParryChanceWeapon = CONFIG._mobParryChanceWeapon.get().floatValue();
        mobParryChanceShield = CONFIG._mobParryChanceShield.get().floatValue();
        mobDeflectChance = CONFIG._mobDeflectChance.get().floatValue();
        mobScaler = CONFIG._mobScaler.get().floatValue();
        posCap = CONFIG._posCap.get().floatValue();
        dodge = CONFIG._dodge.get();
        kbNerf = CONFIG._knockbackNerf.get().floatValue();
        sneakParry = CONFIG._sneakParry.get();
        recovery = CONFIG._recovery.get();
        foodCool = CONFIG._foodCool.get();
        adrenaline = CONFIG._adrenaline.get();
        CombatUtils.updateMobParrying(CONFIG._customParry.get());
        CombatUtils.updateProjectiles(CONFIG._customProjectile.get());
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
