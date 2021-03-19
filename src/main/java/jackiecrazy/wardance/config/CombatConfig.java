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
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CombatConfig {
    public static final CombatConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final String[] THANKS_DARKMEGA = {
            "example:sword, 3.5, 1.5, false, 1.25, 1.5",
            "example:shield, 0.3, 0.6, true, 20, 1",
            "minecraft:netherite_pickaxe, 2.2, 1, false, 1.4, 2",
            "minecraft:netherite_shovel, 3.8, 0.9, false, 1.1, 1.2",
            "minecraft:netherite_sword, 2.8, 1, false, 1.25, 1.5",
            "minecraft:netherite_axe, 5, 0.8, false, 1.1, 1.2",
            "minecraft:netherite_hoe, 2, 1.4, false, 1.1, 1.2",
            "minecraft:wooden_pickaxe, 1.8, 1, false, 1.4, 2",
            "minecraft:wooden_shovel, 3, 0.9, false, 1.4, 2",
            "minecraft:wooden_sword, 2.5, 1, false, 1.25, 1.5",
            "minecraft:wooden_axe, 4, 1, false, 1.1, 1.2",
            "minecraft:wooden_hoe, 1.7, 1.6, false, 1.4, 2",
            "minecraft:stone_pickaxe, 1.9, 1, false, 1.4, 2",
            "minecraft:stone_shovel, 3.2, 0.9, false, 1.1, 1.2",
            "minecraft:stone_sword, 2.6, 1, false, 1.25, 1.5",
            "minecraft:stone_axe, 4.2, 0.9, false, 1.1, 1.2",
            "minecraft:stone_hoe, 1.8, 1.5, false, 1.4, 2",
            "minecraft:iron_pickaxe, 2, 1, false, 1.4, 2",
            "minecraft:iron_shovel, 3.3, 0.9, false, 1.1, 1.2",
            "minecraft:iron_sword, 2.7, 1, false, 1.25, 1.5",
            "minecraft:iron_axe, 4.5, 0.8, false, 1.1, 1.2",
            "minecraft:iron_hoe, 1.9, 1.4, false, 1.4, 2",
            "minecraft:golden_pickaxe, 1.9, 1, false, 1.4, 2",
            "minecraft:golden_shovel, 2.5, 0.9, false, 1.1, 1.2",
            "minecraft:golden_sword, 2.7, 1, false, 1.25, 1.5",
            "minecraft:golden_axe, 4.5, 0.8, false, 1.1, 1.2",
            "minecraft:golden_hoe, 1.9, 1.5, false, 1.4, 2",
            "minecraft:diamond_pickaxe, 2.1, 1, false, 1.4, 2",
            "minecraft:diamond_shovel, 3.8, 0.9, false, 1.1, 1.2",
            "minecraft:diamond_sword, 2.8, 1, false, 1.25, 1.5",
            "minecraft:diamond_axe, 5, 0.8, false, 1.1, 1.2",
            "minecraft:diamond_hoe, 2, 1.4, false, 1.4, 2",
            "minecraft:shield, 2.5, 0.8, true, 30, 1",
            "spartanshields:shield_basic_wood, 2.5, 1, true, 20, 1",
            "spartanshields:shield_basic_stone, 2.5, 0.9, true, 20, 2",
            "spartanshields:shield_basic_iron, 2.5, 0.8, true, 20, 3",
            "spartanshields:shield_basic_gold, 2.5, 0.9, true, 20, 3",
            "spartanshields:shield_basic_diamond, 2.5, 0.7, true, 20, 4",
            "spartanshields:shield_basic_netherite, 2.5, 0.6, true, 20, 4",
            "spartanshields:shield_basic_obsidian, 2.5, 0.6, true, 20, 4",
            "spartanshields:shield_basic_copper, 2.5, 0.9, true, 20, 2",
            "spartanshields:shield_basic_tin, 2.5, 0.9, true, 20, 2",
            "spartanshields:shield_basic_bronze, 2.5, 0.8, true, 20, 3",
            "spartanshields:shield_basic_steel, 2.5, 0.7, true, 20, 3",
            "spartanshields:shield_basic_silver, 2.5, 0.9, true, 20, 2",
            "spartanshields:shield_basic_lead, 2.5, 0.6, true, 20, 3",
            "spartanshields:shield_basic_nickel, 2.5, 0.7, true, 20, 3",
            "spartanshields:shield_basic_invar, 2.5, 0.7, true, 20, 3",
            "spartanshields:shield_basic_constantan, 2.5, 0.8, true, 20, 3",
            "spartanshields:shield_basic_platinum, 2.5, 0.5, true, 20, 4",
            "spartanshields:shield_basic_electrum, 2.5, 0.8, true, 20, 3",
            "spartanshields:shield_tower_wood, 3, 0.9, true, 40, 2",
            "spartanshields:shield_tower_stone, 3, 0.8, true, 40, 3",
            "spartanshields:shield_tower_iron, 3, 0.7, true, 40, 4",
            "spartanshields:shield_tower_gold, 3, 0.8, true, 40, 2",
            "spartanshields:shield_tower_diamond, 3, 0.6, true, 40, 5",
            "spartanshields:shield_tower_netherite, 3, 0.6, true, 40, 5",
            "spartanshields:shield_tower_obsidian, 3, 0.6, true, 40, 5",
            "spartanshields:shield_tower_copper, 3, 0.9, true, 40, 3",
            "spartanshields:shield_tower_tin, 3, 0.9, true, 40, 3",
            "spartanshields:shield_tower_bronze, 3, 0.7, true, 40, 4",
            "spartanshields:shield_tower_steel, 3, 0.6, true, 40, 4",
            "spartanshields:shield_tower_silver, 3, 0.8, true, 40, 3",
            "spartanshields:shield_tower_lead, 3, 0.5, true, 40, 4",
            "spartanshields:shield_tower_nickel, 3, 0.8, true, 40, 4",
            "spartanshields:shield_tower_invar, 3, 0.7, true, 40, 4",
            "spartanshields:shield_tower_constantan, 3, 0.7, true, 40, 4",
            "spartanshields:shield_tower_platinum, 3, 0.4, true, 40, 5",
            "spartanshields:shield_tower_electrum, 3, 0.7, true, 40, 4",
            "spartanshields:shield_botania_manasteel, 3, 0.7, true, 40, 4",
            "spartanshields:shield_botania_terrasteel, 3, 0.5, true, 40, 5",
            "spartanshields:shield_botania_elementium, 3, 0.6, true, 40, 4",
            "spartanshields:shield_mekanism_osmium, 3, 0.7, true, 40, 4",
            "spartanshields:shield_mekanism_lapis_lazuli, 3, 0.8, true, 40, 3",
            "spartanshields:shield_mekanism_refined_glowstone, 3, 0.7, true, 40, 3",
            "spartanshields:shield_mekanism_refined_obsidian, 3, 0.4, true, 40, 5",
            "immersiveengineering:shield, 4.5, 0.5, true, 50, 5",
            "bountifulbaubles:shield_cobalt, 4.5, 0.5, true, 60, 5",
            "bountifulbaubles:shield_obsidian, 4.5, 0.5, true, 60, 5",
            "bountifulbaubles:shield_ankh, 4.5, 0.5, true, 60, 5",
            "silentgear:shield, 3, 0.7, true, 30, 1",
            "minecraft:trident, 2.5, 1.6, false, 1.1, 1.2",
            "cyclic:crystal_pickaxe, 2.2, 1, false, 1.4, 2",
            "cyclic:emerald_pickaxe, 2.2, 1, false, 1.4, 2",
            "cyclic:netherbrick_pickaxe, 2, 1, false, 1.4, 2",
            "cyclic:sandstone_pickaxe, 1.9, 1, false, 1.4, 2",
            "immersiveengineering:pickaxe_steel, 2.1, 1, false, 1.4, 2",
            "cyclic:crystal_shovel, 3.8, 1, false, 1.1, 1.2",
            "cyclic:emerald_shovel, 3.8, 1, false, 1.1, 1.2",
            "cyclic:netherbrick_shovel, 3.2, 1, false, 1.1, 1.2",
            "cyclic:sandstone_shovel, 3.2, 1, false, 1.1, 1.2",
            "immersiveengineering:shovel_steel, 3.4, 1, false, 1.1, 1.2",
            "cyclic:crystal_axe, 5, 1, false, 1.1, 1.2",
            "cyclic:emerald_axe, 5, 1, false, 1.1, 1.2",
            "cyclic:netherbrick_axe, 4.4, 1, false, 1.1, 1.2",
            "cyclic:sandstone_axe, 4, 1, false, 1.1, 1.2",
            "immersiveengineering:axe_steel, 4.4, 1, false, 1.1, 1.2",
            "cyclic:crystal_sword, 2.9, 1, false, 1.25, 1.5",
            "cyclic:emerald_sword, 2.8, 1, false, 1.25, 1.5",
            "cyclic:netherbrick_sword, 2.6, 1, false, 1.25, 1.5",
            "cyclic:sandstone_sword, 2.5, 1, false, 1.25, 1.5",
            "immersiveengineering:sword_steel, 2.7, 1, false, 1.25, 1.5",
            "cyclic:crystal_hoe, 2, 1.4, false, 1.4, 2",
            "cyclic:emerald_hoe, 2, 1.4, false, 1.4, 2",
            "cyclic:sandstone_hoe, 1.8, 1.6, false, 1.4, 2",
            "cyclic:netherbrick_hoe, 1.8, 1.5, false, 1.4, 2",
            "immersiveengineering:hoe_steel, 1.8, 1.4, false, 1.4, 2",
            "spartanweaponry:dagger_wood, 1.6, 2.3, false, 1.5, 2.5",
            "spartanweaponry:dagger_stone, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:dagger_iron, 1.7, 1.9, false, 1.5, 2.5",
            "spartanweaponry:dagger_gold, 1.6, 2, false, 1.5, 2.5",
            "spartanweaponry:dagger_diamond, 1.8, 1.9, false, 1.5, 2.5",
            "spartanweaponry:dagger_netherite, 1.8, 1.9, false, 1.5, 2.5",
            "spartanweaponry:dagger_copper, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:dagger_tin, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:dagger_bronze, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:dagger_steel, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:dagger_silver, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:dagger_invar, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:dagger_platinum, 1.8, 1.9, false, 1.5, 2.5",
            "spartanweaponry:dagger_electrum, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:dagger_nickel, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:dagger_lead, 1.8, 1.8, false, 1.5, 2.5",
            "spartanweaponry:longsword_wood, 3.2, 1, false, 1.25, 1.5",
            "spartanweaponry:longsword_stone, 3.3, 0.9, false, 1.25, 1.5",
            "spartanweaponry:longsword_iron, 3.4, 0.8, false, 1.25, 1.5",
            "spartanweaponry:longsword_gold, 3.3, 0.9, false, 1.25, 1.5",
            "spartanweaponry:longsword_diamond, 3.5, 0.7, false, 1.25, 1.5",
            "spartanweaponry:longsword_netherite, 3.5, 0.7, false, 1.25, 1.5",
            "spartanweaponry:longsword_copper, 3.3, 0.9, false, 1.25, 1.5",
            "spartanweaponry:longsword_tin, 3.3, 0.9, false, 1.25, 1.5",
            "spartanweaponry:longsword_bronze, 3.4, 0.8, false, 1.25, 1.5",
            "spartanweaponry:longsword_steel, 3.4, 0.8, false, 1.25, 1.5",
            "spartanweaponry:longsword_silver, 3.3, 0.9, false, 1.25, 1.5",
            "spartanweaponry:longsword_invar, 3.4, 0.8, false, 1.25, 1.5",
            "spartanweaponry:longsword_platinum, 3.5, 0.7, false, 1.25, 1.5",
            "spartanweaponry:longsword_electrum, 3.4, 0.8, false, 1.25, 1.5",
            "spartanweaponry:longsword_nickel, 3.4, 0.8, false, 1.25, 1.5",
            "spartanweaponry:longsword_lead, 3.5, 0.7, false, 1.25, 1.5",
            "spartanweaponry:katana_wood, 2.5, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_stone, 2.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_iron, 2.7, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_gold, 2.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_diamond, 2.8, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_netherite, 2.8, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_copper, 2.6, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_tin, 2.6, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_bronze, 2.7, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_steel, 2.7, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_silver, 2.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_invar, 2.7, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_platinum, 2.8, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_electrum, 2.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:katana_nickel, 2.7, 1.1, false, 1.25, 1.5",
            "spartanweaponry:katana_lead, 2.7, 1, false, 1.25, 1.5",
            "spartanweaponry:saber_wood, 2.6, 1.1, false, 1.25, 1.5",
            "spartanweaponry:saber_stone, 2.7, 1, false, 1.25, 1.5",
            "spartanweaponry:saber_iron, 2.8, 0.9, false, 1.25, 1.5",
            "spartanweaponry:saber_gold, 2.7, 1, false, 1.25, 1.5",
            "spartanweaponry:saber_diamond, 2.9, 0.8, false, 1.25, 1.5",
            "spartanweaponry:saber_netherite, 2.9, 0.8, false, 1.25, 1.5",
            "spartanweaponry:saber_copper, 2.7, 1, false, 1.25, 1.5",
            "spartanweaponry:saber_tin, 2.7, 1, false, 1.25, 1.5",
            "spartanweaponry:saber_bronze, 2.8, 0.9, false, 1.25, 1.5",
            "spartanweaponry:saber_steel, 2.8, 0.9, false, 1.25, 1.5",
            "spartanweaponry:saber_silver, 2.7, 1, false, 1.25, 1.5",
            "spartanweaponry:saber_invar, 2.8, 0.9, false, 1.25, 1.5",
            "spartanweaponry:saber_platinum, 2.9, 0.8, false, 1.25, 1.5",
            "spartanweaponry:saber_electrum, 2.8, 0.9, false, 1.25, 1.5",
            "spartanweaponry:saber_nickel, 2.8, 0.9, false, 1.25, 1.5",
            "spartanweaponry:saber_lead, 2.8, 0.8, false, 1.25, 1.5",
            "spartanweaponry:rapier_wood, 1.8, 1.2, false, 1.35, 1.6",
            "spartanweaponry:rapier_stone, 2, 1.1, false, 1.35, 1.6",
            "spartanweaponry:rapier_iron, 2.1, 1, false, 1.35, 1.6",
            "spartanweaponry:rapier_gold, 2.1, 1.1, false, 1.35, 1.6",
            "spartanweaponry:rapier_diamond, 2.2, 0.9, false, 1.35, 1.6",
            "spartanweaponry:rapier_netherite, 2.2, 0.9, false, 1.35, 1.6",
            "spartanweaponry:rapier_copper, 2, 1.1, false, 1.35, 1.6",
            "spartanweaponry:rapier_tin, 2, 1.1, false, 1.35, 1.6",
            "spartanweaponry:rapier_bronze, 2.1, 1, false, 1.35, 1.6",
            "spartanweaponry:rapier_steel, 2.1, 1, false, 1.35, 1.6",
            "spartanweaponry:rapier_silver, 2, 1.1, false, 1.35, 1.6",
            "spartanweaponry:rapier_invar, 2, 1, false, 1.35, 1.6",
            "spartanweaponry:rapier_platinum, 2, 0.9, false, 1.35, 1.6",
            "spartanweaponry:rapier_electrum, 2, 1.1, false, 1.35, 1.6",
            "spartanweaponry:rapier_nickel, 2, 1, false, 1.35, 1.6",
            "spartanweaponry:rapier_lead, 2, 0.9, false, 1.35, 1.6",
            "spartanweaponry:greatsword_wood, 3.8, 1.1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_stone, 3.9, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_iron, 4, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_gold, 3.9, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_diamond, 4.1, 0.9, false, 1.1, 1.2",
            "spartanweaponry:greatsword_netherite, 4.1, 0.9, false, 1.1, 1.2",
            "spartanweaponry:greatsword_copper, 3.9, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_tin, 3.9, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_bronze, 4, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_steel, 4, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_silver, 3.9, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_invar, 4, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_platinum, 4.1, 0.9, false, 1.1, 1.2",
            "spartanweaponry:greatsword_electrum, 4, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_nickel, 4, 1, false, 1.1, 1.2",
            "spartanweaponry:greatsword_lead, 4.1, 0.9, false, 1.1, 1.2",
            "spartanweaponry:hammer_wood, 5.5, 1.7, false, 1.1, 1.2",
            "spartanweaponry:hammer_stone, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:hammer_iron, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:hammer_gold, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:hammer_diamond, 6.1, 1.5, false, 1.1, 1.2",
            "spartanweaponry:hammer_netherite, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:hammer_copper, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:hammer_tin, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:hammer_bronze, 5.9, 1.6, false, 1.1, 1.2",
            "spartanweaponry:hammer_steel, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:hammer_silver, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:hammer_invar, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:hammer_platinum, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:hammer_electrum, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:hammer_nickel, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:hammer_lead, 6.5, 1.4, false, 1.1, 1.2",
            "spartanweaponry:warhammer_wood, 7.5, 1.7, false, 1.1, 1.2",
            "spartanweaponry:warhammer_stone, 7.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:warhammer_iron, 7.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:warhammer_gold, 7.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:warhammer_diamond, 8.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:warhammer_netherite, 8.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:warhammer_copper, 7.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:warhammer_tin, 7.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:warhammer_bronze, 7.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:warhammer_steel, 7.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:warhammer_silver, 7.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:warhammer_invar, 7.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:warhammer_platinum, 8.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:warhammer_electrum, 7.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:warhammer_nickel, 7.7, 1.5, false, 1.1, 1.2",
            "spartanweaponry:warhammer_lead, 7.5, 1.4, false, 1.1, 1.2",
            "spartanweaponry:spear_wood, 1.9, 1.8, false, 1.1, 1.2",
            "spartanweaponry:spear_stone, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:spear_iron, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_gold, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:spear_diamond, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:spear_netherite, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:spear_copper, 2, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_tin, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:spear_bronze, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_steel, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_silver, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:spear_invar, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_platinum, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:spear_electrum, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_nickel, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:spear_lead, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:halberd_wood, 5.1, 1.5, false, 1.1, 1.2",
            "spartanweaponry:halberd_stone, 5.3, 1.4, false, 1.1, 1.2",
            "spartanweaponry:halberd_iron, 5.5, 1.3, false, 1.1, 1.2",
            "spartanweaponry:halberd_gold, 5.3, 1.4, false, 1.1, 1.2",
            "spartanweaponry:halberd_diamond, 5.7, 1.2, false, 1.1, 1.2",
            "spartanweaponry:halberd_netherite, 5.7, 1.2, false, 1.1, 1.2",
            "spartanweaponry:halberd_copper, 5.3, 1.4, false, 1.1, 1.2",
            "spartanweaponry:halberd_tin, 5.3, 1.4, false, 1.1, 1.2",
            "spartanweaponry:halberd_bronze, 5.5, 1.3, false, 1.1, 1.2",
            "spartanweaponry:halberd_steel, 5.5, 1.3, false, 1.1, 1.2",
            "spartanweaponry:halberd_silver, 5.3, 1.4, false, 1.1, 1.2",
            "spartanweaponry:halberd_invar, 5.5, 1.3, false, 1.1, 1.2",
            "spartanweaponry:halberd_platinum, 5.7, 1.2, false, 1.1, 1.2",
            "spartanweaponry:halberd_electrum, 5.5, 1.3, false, 1.1, 1.2",
            "spartanweaponry:halberd_nickel, 5.5, 1.3, false, 1.1, 1.2",
            "spartanweaponry:halberd_lead, 5.7, 1.2, false, 1.1, 1.2",
            "spartanweaponry:pike_wood, 1.9, 1.8, false, 1.1, 1.2",
            "spartanweaponry:pike_stone, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:pike_iron, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_gold, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:pike_diamond, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:pike_netherite, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:pike_copper, 2, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_tin, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:pike_bronze, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_steel, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_silver, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:pike_invar, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_platinum, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:pike_electrum, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_nickel, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:pike_lead, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:lance_wood, 3.2, 1, false, 1.1, 1.2",
            "spartanweaponry:lance_stone, 3.3, 0.9, false, 1.1, 1.2",
            "spartanweaponry:lance_iron, 3.4, 0.8, false, 1.1, 1.2",
            "spartanweaponry:lance_gold, 3.3, 0.9, false, 1.1, 1.2",
            "spartanweaponry:lance_diamond, 3.5, 0.7, false, 1.1, 1.2",
            "spartanweaponry:lance_netherite, 3.5, 0.7, false, 1.1, 1.2",
            "spartanweaponry:lance_copper, 3.3, 0.9, false, 1.1, 1.2",
            "spartanweaponry:lance_tin, 3.3, 0.9, false, 1.1, 1.2",
            "spartanweaponry:lance_bronze, 3.4, 0.8, false, 1.1, 1.2",
            "spartanweaponry:lance_steel, 3.4, 0.8, false, 1.1, 1.2",
            "spartanweaponry:lance_silver, 3.3, 0.9, false, 1.1, 1.2",
            "spartanweaponry:lance_invar, 3.4, 0.8, false, 1.1, 1.2",
            "spartanweaponry:lance_platinum, 3.5, 0.7, false, 1.1, 1.2",
            "spartanweaponry:lance_electrum, 3.4, 0.8, false, 1.1, 1.2",
            "spartanweaponry:lance_nickel, 3.4, 0.8, false, 1.1, 1.2",
            "spartanweaponry:lance_lead, 3.5, 0.7, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_wood, 5.5, 1.7, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_stone, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_iron, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_gold, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_diamond, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_netherite, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_copper, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_tin, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_bronze, 5.9, 1.6, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_steel, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_silver, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_invar, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_platinum, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_electrum, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_nickel, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:battleaxe_lead, 6.5, 1.4, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_wood, 5.5, 1.7, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_stone, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_iron, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_gold, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_diamond, 6.1, 1.5, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_netherite, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_copper, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_tin, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_bronze, 5.9, 1.6, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_steel, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_silver, 5.7, 1.6, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_invar, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_platinum, 6.1, 1.4, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_electrum, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_nickel, 5.9, 1.5, false, 1.1, 1.2",
            "spartanweaponry:flanged_mace_lead, 6.5, 1.4, false, 1.1, 1.2",
            "spartanweaponry:glaive_wood, 2.5, 1.2, false, 1.1, 1.2",
            "spartanweaponry:glaive_stone, 2.6, 1.1, false, 1.1, 1.2",
            "spartanweaponry:glaive_iron, 2.7, 1, false, 1.1, 1.2",
            "spartanweaponry:glaive_gold, 2.6, 1, false, 1.1, 1.2",
            "spartanweaponry:glaive_diamond, 2.8, 0.9, false, 1.1, 1.2",
            "spartanweaponry:glaive_netherite, 2.8, 0.9, false, 1.1, 1.2",
            "spartanweaponry:glaive_copper, 2.6, 1.1, false, 1.1, 1.2",
            "spartanweaponry:glaive_tin, 2.6, 1.1, false, 1.1, 1.2",
            "spartanweaponry:glaive_bronze, 2.7, 1, false, 1.1, 1.2",
            "spartanweaponry:glaive_steel, 2.7, 1, false, 1.1, 1.2",
            "spartanweaponry:glaive_silver, 2.6, 1.1, false, 1.1, 1.2",
            "spartanweaponry:glaive_invar, 2.7, 1, false, 1.1, 1.2",
            "spartanweaponry:glaive_platinum, 2.8, 0.9, false, 1.1, 1.2",
            "spartanweaponry:glaive_electrum, 2.7, 1.1, false, 1.1, 1.2",
            "spartanweaponry:glaive_nickel, 2.7, 1, false, 1.1, 1.2",
            "spartanweaponry:glaive_lead, 2.8, 1, false, 1.1, 1.2",
            "spartanweaponry:quarterstaff_wood, 3.5, 1, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_stone, 3.6, 0.9, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_iron, 3.7, 0.8, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_gold, 3.6, 0.8, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_diamond, 3.8, 0.7, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_netherite, 3.8, 0.7, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_copper, 3.6, 0.9, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_tin, 3.6, 0.9, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_bronze, 3.7, 0.8, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_steel, 3.7, 0.8, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_silver, 3.6, 0.9, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_invar, 3.6, 0.8, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_platinum, 3.8, 0.7, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_electrum, 3.6, 0.9, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_nickel, 3.6, 0.8, false, 1.2, 1.4",
            "spartanweaponry:quarterstaff_lead, 3.7, 0.8, false, 1.2, 1.4",
            "spartanweaponry:tomahawk_wood, 3.5, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_stone, 3.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_iron, 3.7, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_gold, 3.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_diamond, 3.8, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_netherite, 3.8, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_copper, 3.6, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_tin, 3.6, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_bronze, 3.7, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_steel, 3.7, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_silver, 3.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_invar, 3.7, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_platinum, 3.8, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_electrum, 3.6, 1.2, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_nickel, 3.7, 1.1, false, 1.25, 1.5",
            "spartanweaponry:tomahawk_lead, 3.7, 1, false, 1.25, 1.5",
            "spartanweaponry:throwing_knife_wood, 1.6, 2.3, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_stone, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_iron, 1.7, 1.9, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_gold, 1.6, 2, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_diamond, 1.8, 1.9, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_netherite, 1.8, 1.9, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_copper, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_tin, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_bronze, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_steel, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_silver, 1.6, 2.1, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_invar, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_platinum, 1.8, 1.9, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_electrum, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_nickel, 1.7, 2, false, 1.5, 2.5",
            "spartanweaponry:throwing_knife_lead, 1.8, 1.8, false, 1.5, 2.5",
            "spartanweaponry:javelin_wood, 1.9, 1.8, false, 1.1, 1.2",
            "spartanweaponry:javelin_stone, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:javelin_iron, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_gold, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:javelin_diamond, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:javelin_netherite, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:javelin_copper, 2, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_tin, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:javelin_bronze, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_steel, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_silver, 2, 1.7, false, 1.1, 1.2",
            "spartanweaponry:javelin_invar, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_platinum, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:javelin_electrum, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_nickel, 2.1, 1.6, false, 1.1, 1.2",
            "spartanweaponry:javelin_lead, 2.2, 1.5, false, 1.1, 1.2",
            "spartanweaponry:boomerang_wood, 2.5, 1.2, false, 1.2, 1.4",
            "spartanweaponry:boomerang_stone, 2.6, 1.1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_iron, 2.7, 1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_gold, 2.6, 1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_diamond, 2.8, 0.9, false, 1.2, 1.4",
            "spartanweaponry:boomerang_netherite, 2.8, 0.9, false, 1.2, 1.4",
            "spartanweaponry:boomerang_copper, 2.6, 1.1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_tin, 2.6, 1.1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_bronze, 2.7, 1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_steel, 2.7, 1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_silver, 2.6, 1.1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_invar, 2.7, 1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_platinum, 2.8, 0.9, false, 1.2, 1.4",
            "spartanweaponry:boomerang_electrum, 2.7, 1.1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_nickel, 2.7, 1, false, 1.2, 1.4",
            "spartanweaponry:boomerang_lead, 2.8, 1, false, 1.2, 1.4",
            "spartanweaponry:club_wood, 4.5, 1.5, false, 1.2, 1.4",
            "spartanweaponry:club_studded, 5.5, 1.4, false, 1.2, 1.4",
            "spartanweaponry:cestus, 3.5, 0.8, false, 1.2, 1.4",
            "spartanweaponry:cestus_studded, 3.9, 0.7, false, 1.2, 1.4",
            "silentgear:sword, 3, 1, false, 1.25, 1.5",
            "silentgear:katana, 2.8, 1.1, false, 1.25, 1.5",
            "silentgear:machete, 3.3, 1, false, 1.25, 1.5",
            "silentgear:spear, 2.2, 1.5, false, 1.25, 1.5",
            "silentgear:knife, 2, 1.8, false, 1.5, 2.5",
            "silentgear:dagger, 2, 1.8, false, 1.5, 2.5",
            "silentgear:pickaxe, 2, 1, false, 1.4, 2",
            "silentgear:shovel, 3.5, 1.1, false, 1.1, 1.2",
            "silentgear:axe, 4.3, 0.8, false, 1.1, 1.2",
            "silentgear:paxel, 3.5, 1.1, false, 1.2, 1.4",
            "silentgear:hammer, 5.5, 1.5, false, 1.1, 1.2",
            "silentgear:excavator, 4.5, 1.3, false, 1.1, 1.2",
            "silentgear:saw, 2, 1.7, false, 1.1, 1.1",
            "silentgear:prospector_hammer, 3.5, 1.2, false, 1.1, 1.2",
            "silentgear:mattock, 3, 1.2, false, 1.1, 1.2",
            "silentgear:sickle, 1.8, 1.6, false, 1.1, 1.2",
            "iceandfire:copper_pickaxe, 1.9, 1, false, 1.4, 2",
            "iceandfire:copper_shovel, 3.2, 0.9, false, 1.1, 1.2",
            "iceandfire:copper_sword, 2.6, 1, false, 1.25, 1.5",
            "iceandfire:copper_axe, 4.2, 0.9, false, 1.1, 1.2",
            "iceandfire:copper_hoe, 1.8, 1.5, false, 1.4, 2",
            "iceandfire:silver_pickaxe, 2, 1, false, 1.4, 2",
            "iceandfire:silver_shovel, 3.3, 0.9, false, 1.1, 1.2",
            "iceandfire:silver_sword, 2.7, 1, false, 1.25, 1.5",
            "iceandfire:silver_axe, 4.5, 0.8, false, 1.1, 1.2",
            "iceandfire:silver_hoe, 1.9, 1.4, false, 1.4, 2",
            "iceandfire:dragonbone_pickaxe, 2.2, 1, false, 1.4, 2",
            "iceandfire:dragonbone_shovel, 4.1, 0.9, false, 1.1, 1.2",
            "iceandfire:dragonbone_sword, 2.9, 1, false, 1.25, 1.5",
            "iceandfire:dragonbone_sword_ice, 3, 1, false, 1.25, 1.5",
            "iceandfire:dragonbone_sword_fire, 2.9, 0.9, false, 1.25, 1.5",
            "iceandfire:dragonbone_sword_lightning, 3, 0.9, false, 1.25, 1.5",
            "iceandfire:hippogryph_sword, 2.7, 1, false, 1.25, 1.5",
            "iceandfire:hippocampus_slapper, 3, 1.4, false, 1.1, 1.2",
            "iceandfire:deathworm_gauntlet_red, 2.5, 0.8, false, 1.1, 1.2",
            "iceandfire:deathworm_gauntlet_white, 2.5, 0.8, false, 1.1, 1.2",
            "iceandfire:deathworm_gauntlet_yellow, 2.5, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonbone_axe, 5.3, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonbone_hoe, 2.1, 1.4, false, 1.4, 2",
            "iceandfire:myrmex_desert_pickaxe, 2, 1, false, 1.4, 2",
            "iceandfire:myrmex_desert_shovel, 3.3, 0.9, false, 1.1, 1.2",
            "iceandfire:myrmex_desert_sword, 2.8, 1, false, 1.25, 1.5",
            "iceandfire:myrmex_desert_sword_venom, 2.8, 1, false, 1.25, 1.5",
            "iceandfire:myrmex_desert_axe, 4.9, 0.8, false, 1.1, 1.2",
            "iceandfire:myrmex_desert_hoe, 2, 1.4, false, 1.4, 2",
            "iceandfire:myrmex_jungle_pickaxe, 2, 1, false, 1.4, 2",
            "iceandfire:myrmex_jungle_shovel, 3.3, 0.9, false, 1.1, 1.2",
            "iceandfire:myrmex_jungle_sword, 2.8, 1, false, 1.25, 1.5",
            "iceandfire:myrmex_jungle_sword_venom, 2.8, 1, false, 1.25, 1.5",
            "iceandfire:myrmex_jungle_axe, 4.9, 0.8, false, 1.1, 1.2",
            "iceandfire:myrmex_jungle_hoe, 2, 1.4, false, 1.4, 2",
            "iceandfire:dragonsteel_ice_pickaxe, 2.4, 0.9, false, 1.4, 2",
            "iceandfire:dragonsteel_ice_shovel, 4.5, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonsteel_ice_sword, 3.1, 0.9, false, 1.25, 1.5",
            "iceandfire:dragonsteel_ice_axe, 5.5, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonsteel_ice_hoe, 2.2, 1.2, false, 1.4, 2",
            "iceandfire:dragonsteel_fire_pickaxe, 2.5, 0.9, false, 1.4, 2",
            "iceandfire:dragonsteel_fire_shovel, 4.8, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonsteel_fire_sword, 3.3, 0.9, false, 1.25, 1.5",
            "iceandfire:dragonsteel_fire_axe, 5.9, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonsteel_fire_hoe, 2.4, 1.3, false, 1.4, 2",
            "iceandfire:dragonsteel_lightning_pickaxe, 2.4, 0.9, false, 1.4, 2",
            "iceandfire:dragonsteel_lightning_shovel, 4.5, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonsteel_lightning_sword, 3.1, 0.9, false, 1.25, 1.5",
            "iceandfire:dragonsteel_lightning_axe, 5.5, 0.8, false, 1.1, 1.2",
            "iceandfire:dragonsteel_lightning_hoe, 2.2, 1.3, false, 1.4, 2",
            "iceandfire:dread_sword, 2.7, 1, false, 1.25, 1.5",
            "iceandfire:dread_knight_sword, 3, 0.9, false, 1.25, 1.5",
            "iceandfire:dread_queen_sword, 3.2, 0.9, false, 1.25, 1.5",
            "iceandfire:ghost_sword, 2.8, 0.9, false, 1.25, 1.5",
            "iceandfire:troll_weapon_axe, 11.5, 0.5, true, 20, 1",
            "iceandfire:troll_weapon_column, 11.5, 0.5, true, 20, 1",
            "iceandfire:troll_weapon_column_forest, 11.5, 0.5, true, 20, 1",
            "iceandfire:troll_weapon_column_frost, 11.5, 0.5, true, 20, 1",
            "iceandfire:troll_weapon_hammer, 11.5, 0.5, true, 20, 1",
            "iceandfire:troll_weapon_trunk, 11.5, 0.5, true, 20, 1",
            "iceandfire:troll_weapon_trunk_frost, 11.5, 0.5, true, 20, 1",
            "bloodmagic:soulpickaxe, 2, 1, false, 1.4, 2",
            "bloodmagic:soulshovel, 3.3, 0.9, false, 1.1, 1.2",
            "bloodmagic:soulsword, 2.7, 1, false, 1.25, 1.5",
            "bloodmagic:soulaxe, 4.5, 0.8, false, 1.1, 1.2",
            "bloodmagic:soulscythe, 1.9, 1.4, false, 1.4, 2",
            "pandoras_creatures:arachnon_hammer, 4.5, 0.8, false, 1.1, 1.2",
            "enigmaticlegacy:astral_breaker, 4.5, 0.8, false, 1.1, 1.2",
            "vampirism:heart_seeker_normal, 2.7, 1.1, false, 1.2, 1.4",
            "vampirism:heart_seeker_enhanced, 2.8, 1, false, 1.25, 1.5",
            "vampirism:heart_seeker_ultimate, 2.9, 1, false, 1.3, 1.6",
            "vampirism:heart_striker_normal, 2.3, 0.9, false, 1.4, 2.4",
            "vampirism:heart_striker_enhanced, 2.4, 0.9, false, 1.5, 2.5",
            "vampirism:heart_striker_ultimate, 2.5, 0.8, false, 1.6, 2.6",
            "vampirism:hunter_axe_normal, 5.5, 0.9, false, 1.1, 1.2",
            "vampirism:hunter_axe_enhanced, 5.7, 0.8, true, 1.1, 1.2",
            "vampirism:hunter_axe_ultimate, 6, 0.7, true, 1.1, 1.2",
            "consecration:fire_stick, 2, 1.2, false, 1.1, 1.2",
            "eidolon:reversal_pick, 2, 1, false, 1.4, 2",
            "eidolon:reaper_scythe, 3.5, 0.9, false, 1.4, 2",
            "eidolon:sapping_sword, 2.7, 1, false, 1.25, 1.5",
            "wyrmroost:platinum_pickaxe, 2, 1, false, 1.4, 2",
            "wyrmroost:platinum_shovel, 3.3, 0.9, false, 1.1, 1.2",
            "wyrmroost:platinum_sword, 2.7, 1, false, 1.25, 1.5",
            "wyrmroost:platinum_axe, 4.5, 0.8, false, 1.1, 1.2",
            "wyrmroost:platinum_hoe, 1.9, 1.4, false, 1.4, 2",
            "wyrmroost:blue_geode_pickaxe, 2, 1, false, 1.4, 2",
            "wyrmroost:blue_geode_shovel, 3.5, 0.9, false, 1.1, 1.2",
            "wyrmroost:blue_geode_sword, 2.7, 1, false, 1.25, 1.5",
            "enigmaticlegacy:forbidden_axe, 2.7, 1, false, 1.1, 1.2",
            "wyrmroost:blue_geode_axe, 4.8, 0.8, false, 1.1, 1.2",
            "wyrmroost:blue_geode_hoe, 1.9, 1.4, false, 1.4, 2",
            "wyrmroost:red_geode_pickaxe, 2.1, 1, false, 1.4, 2",
            "wyrmroost:red_geode_shovel, 3.9, 0.9, false, 1.1, 1.2",
            "wyrmroost:red_geode_sword, 2.8, 1, false, 1.25, 1.5",
            "wyrmroost:red_geode_axe, 5.1, 0.8, false, 1.1, 1.2",
            "wyrmroost:red_geode_hoe, 2, 1.4, false, 1.4, 2",
            "wyrmroost:purple_geode_pickaxe, 2.2, 1, false, 1.4, 2",
            "wyrmroost:purple_geode_shovel, 4.1, 0.9, false, 1.1, 1.2",
            "wyrmroost:purple_geode_sword, 2.9, 1, false, 1.25, 1.5",
            "wyrmroost:purple_geode_axe, 5.3, 0.8, false, 1.1, 1.2",
            "wyrmroost:purple_geode_hoe, 2.1, 1.4, false, 1.4, 2",
            "byg:pendorite_pickaxe, 2.2, 1, false, 1.4, 2",
            "byg:pendorite_shovel, 4.1, 0.9, false, 1.1, 1.2",
            "byg:pendorite_sword, 2.9, 1, false, 1.25, 1.5",
            "byg:pendorite_axe, 5.3, 0.8, false, 1.1, 1.2",
            "byg:pendorite_battleaxe, 5.5, 0.9, false, 1.1, 1.2",
            "byg:pendorite_hoe, 2.1, 1.4, false, 1.4, 2, 1.4, 2",
            "aquaculture:neptunium_pickaxe, 2.2, 1, false, 1.4, 2",
            "aquaculture:neptunium_shovel, 4.1, 0.9, false, 1.1, 1.2",
            "aquaculture:neptunium_sword, 2.9, 1, false, 1.25, 1.5",
            "aquaculture:neptunium_axe, 5.3, 0.8, false, 1.1, 1.2",
            "aquaculture:neptunium_hoe, 1.1, 1.4, false, 1.4, 2",
            "aquaculture:wooden_fillet_knife, 1, 1.4, false, 1.5, 2.5",
            "aquaculture:stone_fillet_knife, 1, 1.4, false, 1.5, 2.5",
            "aquaculture:iron_fillet_knife, 1.1, 1.3, false, 1.5, 2.5",
            "aquaculture:gold_fillet_knife, 1.1, 1.4, false, 1.5, 2.5",
            "aquaculture:diamond_fillet_knife, 1.2, 1.3, false, 1.5, 2.5",
            "aquaculture:neptunium_fillet_knife, 1.3, 1.1, false, 1.5, 2.5",
            "mahoutsukai:caliburn, 2.7, 1, false, 1.25, 1.5",
            "mahoutsukai:clarent, 2.7, 1, false, 1.25, 1.5",
            "mahoutsukai:morgan, 2.7, 1, false, 1.25, 1.5",
            "mahoutsukai:dagger, 1.5, 1.3, false, 1.5, 2.5",
            "mahoutsukai:theripper, 1.5, 1.3, false, 1.5, 2.5",
            "mahoutsukai:proximity_projection_keys, 2, 1.3, false, 1.25, 1.5",
            "enigmaticlegacy:etherium_pickaxe, 2.2, 1, false, 1.4, 2",
            "enigmaticlegacy:etherium_shovel, 4.1, 0.9, false, 1.1, 1.2",
            "enigmaticlegacy:etherium_sword, 2.9, 1, false, 1.25, 1.5",
            "enigmaticlegacy:etherium_axe, 5.3, 0.8, false, 1.1, 1.2",
            "enigmaticlegacy:etherium_hoe, 2.1, 1.4, false, 1.4, 2",
            "create:deforester, 5.3, 0.8, false, 1.1, 1.2",
            "astral:phantasmal_pickaxe, 1.9, 1, false, 1.4, 2",
            "astral:phantasmal_shovel, 3.2, 0.9, false, 1.1, 1.2",
            "astral:phantasmal_sword, 2.6, 1, false, 1.25, 1.5",
            "astral:phantasmal_axe, 4.2, 0.9, false, 1.1, 1.2",
            "astral:phantasmal_hoe, 1.8, 1.5, false, 1.4, 2",
            "vampirism:pitchfork, 2.5, 1.6, false, 1.1, 1.2",
            "vampirism:stake, 2.5, 1.6, false, 1.5, 2",
            "meetyourfight:depth_star, 4.5, 1.1, false, 1.1, 1.2",
            "meetyourfight:cocktail_cutlass, 2.7, 1.1, false, 1.25, 1.5",
            "evilcraft:mace_of_distortion, 5.0, 0.9, false, 1.1, 1.2",
            "evilcraft:mace_of_destruction, 5.0, 0.9, false, 1.1, 1.2",
            "evilcraft:vein_sword, 2.8, 1, false, 1.25, 1.5",
            "evilcraft:vengeance_pickaxe, 1.9, 1.3, false, 1.4, 2",
            "epicfight:katana, 2.7, 1.1, false, 1.25, 1.5",
            "epicfight:greatsword, 4, 1, false, 1.1, 1.2",
            "epicfight:stone_spear, 2, 1.7, false, 1.1, 1.2",
            "epicfight:iron_spear, 2.1, 1.6, false, 1.1, 1.2",
            "epicfight:gold_spear, 2, 1.7, false, 1.1, 1.2",
            "epicfight:diamond_spear, 2.2, 1.5, false, 1.1, 1.2"

    };
    private static final String[] ARMOR = {
            "minecraft:leather_helmet, 0.3, 0, 0",
            "minecraft:leather_chestplate, 0.6, 0, 0",
            "minecraft:leather_leggings, 0.4, 0, 0",
            "minecraft:leather_boots, 0.3, 0, 0",
            "minecraft:chainmail_helmet, 0.1, 4, 3",
            "minecraft:chainmail_chestplate, 0.3, 9, 5",
            "minecraft:chainmail_leggings, 0.2, 7, 4",
            "minecraft:chainmail_boots, 0.1, 4, 2",
            "minecraft:golden_helmet, 0.4, 2, 1",
            "minecraft:golden_chestplate, 1.0, 6, 2",
            "minecraft:golden_leggings, 0.6, 5, 1",
            "minecraft:golden_boots, 0.2, 2, 1",
            "minecraft:turtle_helmet, 0.1, 7, 3",
            "minecraft:iron_helmet, 0.1, 6, 1",
            "minecraft:iron_chestplate, 0.1, 16, 3",
            "minecraft:iron_leggings, 0.1, 12, 2",
            "minecraft:iron_boots, 0.1, 6, 1",
            "minecraft:diamond_helmet, 0, 5, 6",
            "minecraft:diamond_chestplate, 0, 10, 16",
            "minecraft:diamond_leggings, 0, 8, 12",
            "minecraft:diamond_boots, 0, 5, 6",
            "minecraft:netherite_helmet, 0.4, 6, 0",
            "minecraft:netherite_chestplate, 1.0, 16, 0",
            "minecraft:netherite_leggings, 0.6, 12, 0",
            "minecraft:netherite_boots, 0.2, 6, 0",
            "environmental:thief_hood, 0.3, 0, 0",
            "environmental:healer_pouch, 0.6, 0, 0",
            "environmental:architect_belt, 0.4, 0, 0",
            "environmental:wanderer_boots, 0.3, 0, 0",
            "wyrmroost:blue_geode_helmet, 0, 5, 4",
            "wyrmroost:blue_geode_chestplate, 0, 10, 13",
            "wyrmroost:blue_geode_leggings, 0, 8, 11",
            "wyrmroost:blue_geode_boots, 0, 5, 4",
            "wyrmroost:red_geode_helmet, 0, 5, 6",
            "wyrmroost:red_geode_chestplate, 0, 10, 16",
            "wyrmroost:red_geode_leggings, 0, 8, 12",
            "wyrmroost:red_geode_boots, 0, 5, 6",
            "wyrmroost:purple_geode_helmet, 0, 5, 8",
            "wyrmroost:purple_geode_chestplate, 0, 10, 19",
            "wyrmroost:purple_geode_leggings, 0, 8, 15",
            "wyrmroost:purple_geode_boots, 0, 5, 8",
            "wyrmroost:platinum_helmet, 0.1, 7, 1",
            "wyrmroost:platinum_chestplate, 0.2, 16, 3",
            "wyrmroost:platinum_leggings, 0.2, 12, 2",
            "wyrmroost:platinum_boots, 0.1, 7, 1",
            "wyrmroost:drake_helmet, 0.4, 6, 0",
            "wyrmroost:drake_chestplate, 1.0, 16, 0",
            "wyrmroost:drake_leggings, 0.6, 12, 0",
            "wyrmroost:drake_boots, 0.2, 6, 0",
            "iceandfire:armor_silver_metal_helmet, 0.2, 6, 1",
            "iceandfire:armor_silver_metal_chestplate, 0.4, 16, 3",
            "iceandfire:armor_silver_metal_leggings, 0.3, 12, 2",
            "iceandfire:armor_silver_metal_boots, 0.2, 6, 1",
            "iceandfire:armor_copper_metal_helmet, 0.3, 6, 0",
            "iceandfire:armor_copper_metal_chestplate, 0.4, 12, 0",
            "iceandfire:armor_copper_metal_leggings, 0.4, 8, 0",
            "iceandfire:armor_copper_metal_boots, 0.3, 4, 0",
            "iceandfire:sheep_helmet, 0.4, 0, 0",
            "iceandfire:sheep_chestplate, 0.7, 0, 0",
            "iceandfire:sheep_leggings, 0.5, 0, 0",
            "iceandfire:sheep_boots, 0.4, 0, 0",
            "iceandfire:deathworm_red_helmet, 0.4, 5, 0",
            "iceandfire:deathworm_red_chestplate, 1.0, 9, 0",
            "iceandfire:deathworm_red_leggings, 0.6, 8, 0",
            "iceandfire:deathworm_red_boots, 0.2, 5, 0",
            "iceandfire:deathworm_yellow_helmet, 0.4, 5, 0",
            "iceandfire:deathworm_yellow_chestplate, 1.0, 9, 0",
            "iceandfire:deathworm_yellow_leggings, 0.6, 8, 0",
            "iceandfire:deathworm_yellow_boots, 0.2, 5, 0",
            "iceandfire:deathworm_white_helmet, 0.4, 5, 0",
            "iceandfire:deathworm_white_chestplate, 1.0, 9, 0",
            "iceandfire:deathworm_white_leggings, 0.6, 8, 0",
            "iceandfire:deathworm_white_boots, 0.2, 5, 0",
            "iceandfire:myrmex_desert_helmet, 0.3, 6, 1",
            "iceandfire:myrmex_desert_chestplate, 0.6, 16, 3",
            "iceandfire:myrmex_desert_leggings, 0.5, 12, 2",
            "iceandfire:myrmex_desert_boots, 0.3, 6, 1",
            "iceandfire:myrmex_jungle_helmet, 0.2, 8, 1",
            "iceandfire:myrmex_jungle_chestplate, 0.5, 18, 3",
            "iceandfire:myrmex_jungle_leggings, 0.4, 14, 2",
            "iceandfire:myrmex_jungle_boots, 0.2, 8, 1",
            "iceandfire:dragonsteel_ice_helmet, 0, 9, 3",
            "iceandfire:dragonsteel_ice_chestplate, 0, 24, 8",
            "iceandfire:dragonsteel_ice_leggings, 0, 18, 6",
            "iceandfire:dragonsteel_ice_boots, 0, 9, 3",
            "iceandfire:dragonsteel_fire_helmet, 0, 9, 3",
            "iceandfire:dragonsteel_fire_chestplate, 0, 24, 8",
            "iceandfire:dragonsteel_fire_leggings, 0, 18, 6",
            "iceandfire:dragonsteel_fire_boots, 0, 9, 3",
            "iceandfire:dragonsteel_lightning_helmet, 0, 9, 3",
            "iceandfire:dragonsteel_lightning_chestplate, 0, 24, 8",
            "iceandfire:dragonsteel_lightning_leggings, 0, 18, 6",
            "iceandfire:dragonsteel_lightning_boots, 0, 9, 3",
            "iceandfire:armor_red_helmet, 0.2, 6, 0",
            "iceandfire:armor_red_chestplate, 0.5, 16, 0",
            "iceandfire:armor_red_leggings, 0.4, 12, 0",
            "iceandfire:armor_red_boots, 0.2, 6, 0",
            "iceandfire:armor_bronze_helmet, 0.2, 6, 0",
            "iceandfire:armor_bronze_chestplate, 0.5, 16, 0",
            "iceandfire:armor_bronze_leggings, 0.4, 12, 0",
            "iceandfire:armor_bronze_boots, 0.2, 6, 0",
            "iceandfire:armor_green_helmet, 0.2, 6, 0",
            "iceandfire:armor_green_chestplate, 0.5, 16, 0",
            "iceandfire:armor_green_leggings, 0.4, 12, 0",
            "iceandfire:armor_green_boots, 0.2, 6, 0",
            "iceandfire:armor_gray_helmet, 0.2, 6, 0",
            "iceandfire:armor_gray_chestplate, 0.5, 16, 0",
            "iceandfire:armor_gray_leggings, 0.4, 12, 0",
            "iceandfire:armor_gray_boots, 0.2, 6, 0",
            "iceandfire:armor_blue_helmet, 0.2, 6, 0",
            "iceandfire:armor_blue_chestplate, 0.5, 16, 0",
            "iceandfire:armor_blue_leggings, 0.4, 12, 0",
            "iceandfire:armor_blue_boots, 0.2, 6, 0",
            "iceandfire:armor_white_helmet, 0.2, 6, 0",
            "iceandfire:armor_white_chestplate, 0.5, 16, 0",
            "iceandfire:armor_white_leggings, 0.4, 12, 0",
            "iceandfire:armor_white_boots, 0.2, 6, 0",
            "iceandfire:armor_sapphire_helmet, 0.2, 6, 0",
            "iceandfire:armor_sapphire_chestplate, 0.5, 16, 0",
            "iceandfire:armor_sapphire_leggings, 0.4, 12, 0",
            "iceandfire:armor_sapphire_boots, 0.2, 6, 0",
            "iceandfire:armor_silver_helmet, 0.2, 6, 0",
            "iceandfire:armor_silver_chestplate, 0.5, 16, 0",
            "iceandfire:armor_silver_leggings, 0.4, 12, 0",
            "iceandfire:armor_silver_boots, 0.2, 6, 0",
            "iceandfire:armor_electric_helmet, 0.2, 6, 0",
            "iceandfire:armor_electric_chestplate, 0.5, 16, 0",
            "iceandfire:armor_electric_leggings, 0.4, 12, 0",
            "iceandfire:armor_electric_boots, 0.2, 6, 0",
            "iceandfire:armor_amethyst_helmet, 0.2, 6, 0",
            "iceandfire:armor_amethyst_chestplate, 0.5, 16, 0",
            "iceandfire:armor_amethyst_leggings, 0.4, 12, 0",
            "iceandfire:armor_amethyst_boots, 0.2, 6, 0",
            "iceandfire:armor_copper_helmet, 0.2, 6, 0",
            "iceandfire:armor_copper_chestplate, 0.5, 16, 0",
            "iceandfire:armor_copper_leggings, 0.4, 12, 0",
            "iceandfire:armor_copper_boots, 0.2, 6, 0",
            "iceandfire:armor_black_helmet, 0.2, 6, 0",
            "iceandfire:armor_black_chestplate, 0.5, 16, 0",
            "iceandfire:armor_black_leggings, 0.4, 12, 0",
            "iceandfire:armor_black_boots, 0.2, 6, 0",
            "iceandfire:tide_blue_helmet, 0.4, 5, 0",
            "iceandfire:tide_blue_chestplate, 1.0, 13, 0",
            "iceandfire:tide_blue_leggings, 0.6, 9, 0",
            "iceandfire:tide_blue_boots, 0.2, 5, 0",
            "iceandfire:tide_bronze_helmet, 0.4, 5, 0",
            "iceandfire:tide_bronze_chestplate, 1.0, 13, 0",
            "iceandfire:tide_bronze_leggings, 0.6, 9, 0",
            "iceandfire:tide_bronze_boots, 0.2, 5, 0",
            "iceandfire:tide_deepblue_helmet, 0.4, 5, 0",
            "iceandfire:tide_deepblue_chestplate, 1.0, 13, 0",
            "iceandfire:tide_deepblue_leggings, 0.6, 9, 0",
            "iceandfire:tide_deepblue_boots, 0.2, 5, 0",
            "iceandfire:tide_green_helmet, 0.4, 5, 0",
            "iceandfire:tide_green_chestplate, 1.0, 13, 0",
            "iceandfire:tide_green_leggings, 0.6, 9, 0",
            "iceandfire:tide_green_boots, 0.2, 5, 0",
            "iceandfire:tide_purple_helmet, 0.4, 5, 0",
            "iceandfire:tide_purple_chestplate, 1.0, 13, 0",
            "iceandfire:tide_purple_leggings, 0.6, 9, 0",
            "iceandfire:tide_purple_boots, 0.2, 5, 0",
            "iceandfire:tide_red_helmet, 0.4, 5, 0",
            "iceandfire:tide_red_chestplate, 1.0, 13, 0",
            "iceandfire:tide_red_leggings, 0.6, 9, 0",
            "iceandfire:tide_red_boots, 0.2, 5, 0",
            "iceandfire:tide_teal_helmet, 0.4, 5, 0",
            "iceandfire:tide_teal_chestplate, 1.0, 13, 0",
            "iceandfire:tide_teal_leggings, 0.6, 9, 0",
            "iceandfire:tide_teal_boots, 0.2, 5, 0",
            "iceandfire:forest_troll_leather_helmet, 0.4, 0, 0",
            "iceandfire:forest_troll_leather_chestplate, 0.8, 16, 0",
            "iceandfire:forest_troll_leather_leggings, 0.6, 12, 0",
            "iceandfire:forest_troll_leather_boots, 0.4, 6, 0",
            "iceandfire:frost_troll_leather_helmet, 0.4, 0, 0",
            "iceandfire:frost_troll_leather_chestplate, 0.8, 16, 0",
            "iceandfire:frost_troll_leather_leggings, 0.6, 12, 0",
            "iceandfire:frost_troll_leather_boots, 0.4, 6, 0",
            "iceandfire:mountain_troll_leather_helmet, 0.4, 0, 0",
            "iceandfire:mountain_troll_leather_chestplate, 0.8, 16, 0",
            "iceandfire:mountain_troll_leather_leggings, 0.6, 12, 0",
            "iceandfire:mountain_troll_leather_boots, 0.4, 6, 0",
            "bloodmagic:livinghelmet, 0.2, 4, 1",
            "bloodmagic:livingplate, 0.2, 15, 3",
            "bloodmagic:livingleggings, 0.2, 10, 2",
            "bloodmagic:livingboots, 0.2, 4, 1",
            "pandoras_creatures:plant_hat, 0.3, 0, 0",
            "eidolon:warlock_hat, 0.4, 0, 0",
            "eidolon:warlock_cloak, 0.8, 0, 0",
            "eidolon:warlock_boots, 0.4, 0, 0",
            "eidolon:top_hat, 0.3, 0, 0",
            "enigmaticlegacy:etherium_helmet, 0.6, 6, 0",
            "enigmaticlegacy:etherium_chestplate, 1.2, 16, 0",
            "enigmaticlegacy:etherium_leggings, 0.8, 12, 0",
            "enigmaticlegacy:etherium_boots, 0.4, 6, 0",
            "vampirism:hunter_hat_head_0, 0.3, 0, 0",
            "vampirism:hunter_hat_head_1, 0.3, 0, 0",
            "vampirism:armor_of_swiftness_head_normal, 0.3, 0, 0",
            "vampirism:armor_of_swiftness_chest_normal, 0.6, 0, 0",
            "vampirism:armor_of_swiftness_legs_normal, 0.4, 0, 0",
            "vampirism:armor_of_swiftness_feet_normal, 0.3, 0, 0",
            "vampirism:armor_of_swiftness_head_enhanced, 0.3, 0, 0",
            "vampirism:armor_of_swiftness_chest_enhanced, 0.6, 0, 0",
            "vampirism:armor_of_swiftness_legs_enhanced, 0.4, 0, 0",
            "vampirism:armor_of_swiftness_feet_enhanced, 0.3, 0, 0",
            "vampirism:armor_of_swiftness_head_ultimate, 0.3, 0, 0",
            "vampirism:armor_of_swiftness_chest_ultimate, 0.6, 0, 0",
            "vampirism:armor_of_swiftness_legs_ultimate, 0.4, 0, 0",
            "vampirism:armor_of_swiftness_feet_ultimate, 0.3, 0, 0",
            "vampirism:hunter_coat_head_normal, 0.1, 4, 3",
            "vampirism:hunter_coat_chest_normal, 0.3, 9, 5",
            "vampirism:hunter_coat_legs_normal, 0.2, 7, 4",
            "vampirism:hunter_coat_feet_normal, 0.1, 4, 2",
            "vampirism:hunter_coat_head_enhanced, 0.1, 4, 3",
            "vampirism:hunter_coat_chest_enhanced, 0.3, 9, 5",
            "vampirism:hunter_coat_legs_enhanced, 0.2, 7, 4",
            "vampirism:hunter_coat_feet_enhanced, 0.1, 4, 2",
            "vampirism:hunter_coat_head_ultimate, 0.1, 4, 3",
            "vampirism:hunter_coat_chest_ultimate, 0.3, 9, 5",
            "vampirism:hunter_coat_legs_ultimate, 0.2, 7, 4",
            "vampirism:hunter_coat_feet_ultimate, 0.1, 4, 2",
            "vampirism:obsidian_armor_head_normal, 0, 5, 6",
            "vampirism:obsidian_armor_chest_normal, 0, 10, 16",
            "vampirism:obsidian_armor_legs_normal, 0, 8, 12",
            "vampirism:obsidian_armor_feet_normal, 0, 5, 6",
            "vampirism:obsidian_armor_head_enhanced, 0, 5, 6",
            "vampirism:obsidian_armor_chest_enhanced, 0, 10, 16",
            "vampirism:obsidian_armor_legs_enhanced, 0, 8, 12",
            "vampirism:obsidian_armor_feet_enhanced, 0, 5, 6",
            "vampirism:obsidian_armor_head_ultimate, 0, 5, 6",
            "vampirism:obsidian_armor_chest_ultimate, 0, 10, 16",
            "vampirism:obsidian_armor_legs_ultimate, 0, 8, 12",
            "vampirism:obsidian_armor_feet_ultimate, 0, 5, 6",
            "druidcraft:bone_helmet, 0, 3, 5",
            "druidcraft:bone_chestplate, 0, 8, 11",
            "druidcraft:bone_leggings, 0, 6, 9",
            "druidcraft:bone_boots, 0, 4, 5",
            "druidcraft:chitin_helmet, 0.4, 6, 0",
            "druidcraft:chitin_chestplate, 1.0, 16, 0",
            "druidcraft:chitin_leggings, 0.6, 12, 0",
            "druidcraft:chitin_boots, 0.2, 6, 0",
            "druidcraft:moonstone_helmet, 0, 5, 6",
            "druidcraft:moonstone_chestplate, 0, 10, 16",
            "druidcraft:moonstone_leggings, 0, 8, 12",
            "druidcraft:moonstone_boots, 0, 5, 6",
            "betterendforge:terminite_helmet, 0.1, 6, 2",
            "betterendforge:terminite_chestplate, 0.1, 16, 5",
            "betterendforge:terminite_leggings, 0.1, 12, 4",
            "betterendforge:terminite_boots, 0.1, 6, 1",
            "betterendforge:thallasium_helmet, 0.1, 4, 3",
            "betterendforge:thallasium_chestplate, 0.3, 9, 5",
            "betterendforge:thallasium_leggings, 0.2, 7, 4",
            "betterendforge:thallasium_boots, 0.1, 4, 2",
            "betterendforge:crystalite_helmet, 0, 7, 6",
            "betterendforge:crystalite_chestplate, 0, 12, 16",
            "betterendforge:crystalite_leggings, 0, 10, 12",
            "betterendforge:crystalite_boots, 0, 7, 6",
            "betterendforge:aeternium_helmet, 0.3, 6, 3",
            "betterendforge:aeternium_chestplate, 0.8, 16, 8",
            "betterendforge:aeternium_leggings, 0.5, 12, 6",
            "betterendforge:aeternium_boots, 0.2, 6, 3",
            "majruszs_difficulty:hermes_boots, 0.2, 0, 0",
            "epicfight:stray_hat, 0.3, 0, 0",
            "epicfight:stray_robes, 0.6, 0, 0",
            "epicfight:stray_pants, 0.4, 0, 0"
    };

    static {
        final Pair<CombatConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CombatConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.DoubleValue _posturePerProjectile;
    private final ForgeConfigSpec.IntValue _shatterCooldown;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureDefend;
    private final ForgeConfigSpec.DoubleValue _defaultMultiplierPostureAttack;
    private final ForgeConfigSpec.IntValue _rollThreshold;
    private final ForgeConfigSpec.IntValue _rollCooldown;
    private final ForgeConfigSpec.IntValue _shieldThreshold;
    private final ForgeConfigSpec.IntValue _shieldCount;
    private final ForgeConfigSpec.IntValue _staggerDuration;
    private final ForgeConfigSpec.IntValue _staggerDurationMin;
    private final ForgeConfigSpec.IntValue _staggerHits;
    private final ForgeConfigSpec.IntValue _mobUpdateInterval;
    private final ForgeConfigSpec.IntValue _qiGrace;
    private final ForgeConfigSpec.IntValue _comboGrace;
    private final ForgeConfigSpec.IntValue _spiritCD;
    private final ForgeConfigSpec.IntValue _postureCD;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _combatItems;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customPosture;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _customArmor;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _woundBL;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _immortal;
    private final ForgeConfigSpec.BooleanValue _woundWL;
    private final ForgeConfigSpec.BooleanValue _immortalWL;
    private final ForgeConfigSpec.BooleanValue _betterSweep;
    private final ForgeConfigSpec.BooleanValue _sweepDurability;
    private final ForgeConfigSpec.IntValue _sweepPerSE;
    private final ForgeConfigSpec.BooleanValue _dodge;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceWeapon;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceShield;
    private final ForgeConfigSpec.DoubleValue _mobDeflectChance;
    private final ForgeConfigSpec.DoubleValue _mobScaler;
    private final ForgeConfigSpec.DoubleValue _kenshiroScaler;
    private final ForgeConfigSpec.DoubleValue _wound;
    private final ForgeConfigSpec.DoubleValue _fatig;
    private final ForgeConfigSpec.DoubleValue _burno;
    private final ForgeConfigSpec.DoubleValue _posCap;
    private final ForgeConfigSpec.DoubleValue _stagger;
    private final ForgeConfigSpec.BooleanValue _elenai;
    private final ForgeConfigSpec.BooleanValue _elenaiP;
    private final ForgeConfigSpec.BooleanValue _elenaiC;
    private final ForgeConfigSpec.BooleanValue _stab;
    private final ForgeConfigSpec.DoubleValue _distract;
    private final ForgeConfigSpec.DoubleValue _unaware;
    private final ForgeConfigSpec.BooleanValue _ignore;
    private final ForgeConfigSpec.DoubleValue _nausea;
    private final ForgeConfigSpec.DoubleValue _poison;
    private final ForgeConfigSpec.DoubleValue _weakness;
    private final ForgeConfigSpec.DoubleValue _hunger;
    private final ForgeConfigSpec.DoubleValue _luck;
    private final ForgeConfigSpec.IntValue _baseDetectionHorizontal;
    private final ForgeConfigSpec.IntValue _baseDetectionVertical;
    private final ForgeConfigSpec.IntValue _anglePerArmor;
    private final ForgeConfigSpec.DoubleValue _blockPerVolume;
    private final ForgeConfigSpec.DoubleValue _knockbackNerf;

    public static float posturePerProjectile;
    public static int shatterCooldown;
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
    public static int mobUpdateInterval;
    public static int qiGrace;
    public static int comboGrace;
    public static int spiritCD;
    public static int postureCD;
    public static float mobParryChanceWeapon, mobParryChanceShield, mobDeflectChance, mobScaler, kenshiroScaler;
    public static float wound, fatigue, burnout, posCap, distract, unaware;
    public static boolean dodge, elenai, elenaiP, elenaiC, stealthSystem, ignore;
    public static float weakness, hunger, poison, luck, nausea;
    public static ArrayList<String> woundList;
    public static boolean woundWL;
    public static ArrayList<String> immortal;
    public static boolean immortalWL, betterSweep, sweepDurability;
    public static int sweepAngle, baseHorizontalDetection, baseVerticalDetection, anglePerArmor;
    public static double blockPerVolume;
    public static float kbNerf;

    public CombatConfig(ForgeConfigSpec.Builder b) {
        b.push("defense");
        _posturePerProjectile = b.translation("wardance.config.ppp").comment("Posture consumed per projectile parried").defineInRange("posture per projectile", 0.5, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureAttack = b.translation("wardance.config.dmpa").comment("Default multiplier for any items not defined in the config, multiplied by their attack damage").defineInRange("default attack multiplier", 0.15, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureDefend = b.translation("wardance.config.dmpd").comment("Default multiplier for any item not defined in the config, when used for parrying").defineInRange("default defense multiplier", 1.4, 0, Double.MAX_VALUE);
        _dodge = b.translation("wardance.config.dodge").define("enable dodges", true);
        _rollThreshold = b.translation("wardance.config.rollT").comment("Within this number of ticks after rolling the entity is considered invulnerable. This also determines the animation, so changing it might mean you land before or after standing up again").defineInRange("roll time", 10, 0, Integer.MAX_VALUE);
        _rollCooldown = b.translation("wardance.config.rollC").comment("Within this number of ticks after dodging the entity cannot dodge again").defineInRange("roll cooldown", 20, 0, Integer.MAX_VALUE);
        _shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("default shield time", 16, 0, Integer.MAX_VALUE);
        _shieldCount = b.translation("wardance.config.shieldT").comment("This many parries are free after a parry that cost posture").defineInRange("default shield count", 1, 0, Integer.MAX_VALUE);
        _mobParryChanceWeapon = b.translation("wardance.config.mobPW").comment("chance that a mob parries with a weapon out of 1. Hands are individually calculated.").defineInRange("mob weapon parry chance", 0.3, 0, 1);
        _mobParryChanceShield = b.translation("wardance.config.mobPS").comment("chance that a mob parries with a shield out of 1. Hands are individually calculated.").defineInRange("mob shield parry chance", 0.9, 0, 1);
        _mobDeflectChance = b.translation("wardance.config.mobD").comment("chance that a mob deflects with armor out of 1").defineInRange("mob deflect chance", 0.6, 0, 1);
        _posCap = b.translation("wardance.config.posCap").comment("percentage of max posture that can be dealt in a single hit").defineInRange("posture cap", 0.4, 0, 1);
        _shatterCooldown = b.translation("wardance.config.shatterCD").comment("Ticks after a hit for which shatter will not be replenished").defineInRange("shatter cooldown", 200, 1, Integer.MAX_VALUE);
        b.pop();
        b.push("offense");
        _betterSweep = b.translation("wardance.config.sweep").comment("overrides vanilla sweep with a version hits all affected entities for full damage and effects and works regardless of aim, with sweeping edge determining the angle which is swept. Sweeps will be completely suppressed if you don't have the enchantment.").define("enable better sweep", true);
        _sweepDurability = b.translation("wardance.config.sweepD").comment("whether better sweep deals durability damage for each mob hit").define("durability damage per hit mob", false);
        _sweepPerSE = b.translation("wardance.config.sweepE").comment("every level of sweeping edge gives this much extra angles when using better sweep").defineInRange("sweeping edge angle per level", 40, 0, Integer.MAX_VALUE);
        _staggerDuration = b.translation("wardance.config.staggerD").comment("Maximum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("max stagger duration", 60, 1, Integer.MAX_VALUE);
        _staggerDurationMin = b.translation("wardance.config.staggerM").comment("Minimum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("min stagger duration", 10, 1, Integer.MAX_VALUE);
        _staggerHits = b.translation("wardance.config.staggerH").comment("Number of hits a staggered entity will take before stagger is automatically canceled").defineInRange("stagger hits", 3, 1, Integer.MAX_VALUE);
        _stagger = b.translation("wardance.config.stagger").comment("Extra damage taken by a staggered entity").defineInRange("stagger damage multiplier", 1.5, 1, Double.MAX_VALUE);
        _kenshiroScaler = b.translation("wardance.config.kenB").comment("posture damage from empty fists will be scaled by this number. Notice many mobs, such as endermen and ravagers, technically are empty-handed!").defineInRange("unarmed buff", 1.6, 0, Double.MAX_VALUE);
        b.pop();
        b.push("difficulty");
        _qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining might during which it will not decrease").defineInRange("might grace period", 100, 1, Integer.MAX_VALUE);
        _comboGrace = b.translation("wardance.config.comboG").comment("Number of ticks after gaining combo during which it will not decrease").defineInRange("combo grace period", 100, 1, Integer.MAX_VALUE);
        _spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spirit cooldown", 30, 1, Integer.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("posture cooldown", 30, 1, Integer.MAX_VALUE);
        _mobScaler = b.translation("wardance.config.mobB").comment("posture damage from mob attacks will be scaled by this number").defineInRange("mob posture damage buff", 2, 0, Double.MAX_VALUE);
        _wound = b.translation("wardance.config.wound").comment("this percentage of incoming damage before armor is also added to wounding").defineInRange("wound percentage", 0.1, 0, 1d);
        _fatig = b.translation("wardance.config.fatigue").comment("this percentage of posture damage is also added to fatigue").defineInRange("fatigue percentage", 0.1, 0, 1d);
        _burno = b.translation("wardance.config.burnout").comment("this percentage of stamina use is also added to burnout").defineInRange("burnout percentage", 0.1, 0, 1d);
        _knockbackNerf = b.translation("wardance.config.knockback").comment("knockback from all sources to everything will be multiplied by this amount").defineInRange("knockback multiplier", 1, 0, 10d);
        b.pop();
        _mobUpdateInterval = b.translation("wardance.config.mobU").comment("Mobs are forced to sync to client every this number of ticks").defineInRange("forced mob update interval", 100, 1, Integer.MAX_VALUE);
        b.push("compat");
        _elenai = b.translation("wardance.config.elenaiCompat").comment("whether Elenai Dodge 2 compat is enabled. This disables sidesteps and rolls, turns dodging into a safety roll when staggered, and causes dodges to reset posture cooldown").define("enable Elenai Dodge compat", true);
        _elenaiP = b.translation("wardance.config.elenaiPosture").comment("if compat is enabled, whether posture cooldown disables feather recharging").define("feather posture", true);
        _elenaiC = b.translation("wardance.config.elenaiCombo").comment("if compat is enabled, whether high combo multiplies feather regeneration speed").define("feather combo", true);
        b.pop();
        b.push("stealth");
        _stab = b.translation("wardance.config.stabby").comment("enable or disable the entire system").define("enable stabbing", true);
        _baseDetectionHorizontal = b.translation("wardance.config.detectH").comment("mobs start out with this FoV of full detection on the xz plane").defineInRange("default mob horizontal FoV", 120, 0, 360);
        _baseDetectionVertical = b.translation("wardance.config.detectV").comment("mobs start out with this FoV of full detection on the y axis").defineInRange("default mob vertical FoV", 60, 0, 360);
        _anglePerArmor = b.translation("wardance.config.perarmor").comment("your armor points are multiplied by this to generate a new FoV for the purpose of detection by mobs, if it is greater than the default").defineInRange("armor stealth debuff", 18, 0, 360);
        _blockPerVolume = b.translation("wardance.config.volume").comment("this value is multiplied by the volume of a sound to determine how far it'll alert mobs to investigate from. Large values will GREATLY impact performance, you have been warned.").defineInRange("alert volume multiplier", 4, 0, Double.MAX_VALUE);
        _distract = b.translation("wardance.config.distract").comment("posture and health damage multiplier for distracted stabs").defineInRange("distracted stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _unaware = b.translation("wardance.config.unaware").comment("posture and health damage multiplier for unaware stabs").defineInRange("unaware stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _ignore = b.translation("wardance.config.ignore").comment("whether unaware stabs ignore parry, deflection, shatter, and absorption").define("unaware stab defense ignore", true);
        b.pop();
        b.push("lists");
        _combatItems = b.translation("wardance.config.combatItems").comment("Items eligible for parrying. Format should be name, attack posture consumption, defense multiplier, is shield. If the item is a shield, the next two numbers are their parry time and parry counter; if the item is a weapon, the next two numbers are distracted stab damage bonus and unaware stab damage bonus. Default values provided graciously by DarkMega, thank you!").defineList("combat items", Arrays.asList(THANKS_DARKMEGA), String.class::isInstance);
        _customPosture = b.translation("wardance.config.postureMobs").comment("Here you can define custom max posture for mobs. Format is name, max posture, whether they're rotated when staggered. Armor is still calculated").defineList("custom mob posture", Lists.newArrayList("example:dragon, 100, false", "example:ghast, 8, true"), String.class::isInstance);
        _customArmor = b.translation("wardance.config.armorItems").comment("define protective stats of armor here. Format is item, absorption, deflection, shatter.").defineList("custom protection attributes", Lists.newArrayList(ARMOR), String.class::isInstance);
        _woundBL = b.translation("wardance.config.woundBL").comment("damage sources added to this list will either not inflict wounding or be the only ones that inflict wounding, depending on whitelist mode").defineList("damage source list", Lists.newArrayList("magic", "indirectmagic", "survivaloverhaul.electrocution", "survivaloverhaul.hypothermia", "survivaloverhaul.hyperthermia", "inWall", "drown", "starve"), String.class::isInstance);
        _woundWL = b.translation("wardance.config.woundWL").comment("whether the wounding list is a whitelist or a blacklist").define("damage source whitelist mode", false);
        _immortal = b.translation("wardance.config.decayBL").comment("entities that are not (or are the only ones, depending on whitelist mode) susceptible to wounding, fatigue, and burnout. Save your pets!").defineList("decay list", Lists.newArrayList("example:scaryboss", "example:pet"), String.class::isInstance);
        _immortalWL = b.translation("wardance.config.decayWL").comment("whether the decay list is a whitelist or a blacklist").define("decay whitelist mode", false);
        b.pop();
        b.push("potion");
        _nausea = b.translation("wardance.config.nausea").comment("how much posture nausea deducts per tick, for mobs only").defineInRange("nausea posture damage", 0.05, 0, Double.MAX_VALUE);
        _poison = b.translation("wardance.config.poison").comment("how much each level of poison multiplies posture regeneration by").defineInRange("poison posture debuff", 0.8, 0, Double.MAX_VALUE);
        _hunger = b.translation("wardance.config.hunger").comment("how much the hunger effect extends the posture cooldown by").defineInRange("hunger posture extension", 1.25, 0, Double.MAX_VALUE);
        _weakness = b.translation("wardance.config.weakness").comment("how much weakness multiplies might generation rate").defineInRange("weakness might debuff", 0.7, 0, Double.MAX_VALUE);
        _luck = b.translation("wardance.config.luck").comment("when attacking an entity, a number between 0 and luck is rolled for both parties. The difference between the attacker's and defender's rolled values is multiplied by this and dealt as additional damage.").defineInRange("luck multiplier", 1.5, 0, Double.MAX_VALUE);
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
        mobUpdateInterval = CONFIG._mobUpdateInterval.get();
        qiGrace = CONFIG._qiGrace.get();
        comboGrace = CONFIG._comboGrace.get();
        spiritCD = CONFIG._spiritCD.get();
        postureCD = CONFIG._postureCD.get();
        staggerDuration = CONFIG._staggerDuration.get();
        staggerDurationMin = CONFIG._staggerDurationMin.get();
        staggerHits = CONFIG._staggerHits.get();
        staggerDamage = CONFIG._stagger.get().floatValue();
        shatterCooldown = CONFIG._shatterCooldown.get();
        mobParryChanceWeapon = CONFIG._mobParryChanceWeapon.get().floatValue();
        mobParryChanceShield = CONFIG._mobParryChanceShield.get().floatValue();
        mobDeflectChance = CONFIG._mobDeflectChance.get().floatValue();
        mobScaler = CONFIG._mobScaler.get().floatValue();
        kenshiroScaler = CONFIG._kenshiroScaler.get().floatValue();
        wound = CONFIG._wound.get().floatValue();
        fatigue = CONFIG._fatig.get().floatValue();
        burnout = CONFIG._burno.get().floatValue();
        posCap = CONFIG._posCap.get().floatValue();
        dodge = CONFIG._dodge.get();
        elenai = CONFIG._elenai.get();
        elenaiC = CONFIG._elenaiC.get();
        elenaiP = CONFIG._elenaiP.get();
        woundList = new ArrayList<>(CONFIG._woundBL.get());
        immortal = new ArrayList<>(CONFIG._immortal.get());
        woundWL = CONFIG._woundWL.get();
        immortalWL = CONFIG._immortalWL.get();
        stealthSystem = CONFIG._stab.get();
        distract = stealthSystem ? CONFIG._distract.get().floatValue() : 1;
        unaware = stealthSystem ? CONFIG._unaware.get().floatValue() : 1;
        ignore = stealthSystem & CONFIG._ignore.get();
        weakness = CONFIG._weakness.get().floatValue();
        poison = CONFIG._poison.get().floatValue();
        hunger = CONFIG._hunger.get().floatValue();
        luck = CONFIG._luck.get().floatValue();
        nausea = CONFIG._nausea.get().floatValue();
        betterSweep = CONFIG._betterSweep.get();
        sweepAngle = CONFIG._sweepPerSE.get();
        sweepDurability = CONFIG._sweepDurability.get();
        anglePerArmor = CONFIG._anglePerArmor.get();
        baseHorizontalDetection = CONFIG._baseDetectionHorizontal.get();
        baseVerticalDetection = CONFIG._baseDetectionVertical.get();
        blockPerVolume = CONFIG._blockPerVolume.get();
        kbNerf = CONFIG._knockbackNerf.get().floatValue();
        CombatUtils.updateLists(CONFIG._combatItems.get(), CONFIG._customPosture.get(), CONFIG._customArmor.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            WarDance.LOGGER.debug("loading combat config!");
            bake();
        }
    }
}
