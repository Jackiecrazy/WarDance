package jackiecrazy.wardance.config;

import com.google.common.collect.Lists;
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
    private static final String[] THANKS_DARKMEGA = {
            "example:sword, 3.5, 1.5, false",
            "example:shield, 0.3, 0.6, true, 20, 1",
            "minecraft:netherite_pickaxe, 0.75, 1.0, false",
            "minecraft:netherite_shovel, 2.3, 0.9, false",
            "minecraft:netherite_sword, 1.3, 1.0, false",
            "minecraft:netherite_axe, 3.5, 0.8, false",
            "minecraft:netherite_hoe, 0.5, 1.4, false",
            "minecraft:wooden_pickaxe, 0.25, 1.0, false",
            "minecraft:wooden_shovel, 1.5, 0.9, false",
            "minecraft:wooden_sword, 1.0, 1.0, false",
            "minecraft:wooden_axe, 2.5, 1.0, false",
            "minecraft:wooden_hoe, 0.2, 1.6, false",
            "minecraft:stone_pickaxe, 0.4, 1.0, false",
            "minecraft:stone_shovel, 1.7, 0.9, false",
            "minecraft:stone_sword, 1.1, 1.0, false",
            "minecraft:stone_axe, 2.7, 0.9, false",
            "minecraft:stone_hoe, 0.3, 1.5, false",
            "minecraft:iron_pickaxe, 0.5, 1.0, false",
            "minecraft:iron_shovel, 1.8, 0.9, false",
            "minecraft:iron_sword, 1.2, 1.0, false",
            "minecraft:iron_axe, 3.0, 0.8, false",
            "minecraft:iron_hoe, 0.4, 1.4, false",
            "minecraft:golden_pickaxe, 0.4, 1.0, false",
            "minecraft:golden_shovel, 1.0, 0.9, false",
            "minecraft:golden_sword, 1.2, 1.0, false",
            "minecraft:golden_axe, 3.0, 0.8, false",
            "minecraft:golden_hoe, 0.4, 1.5, false",
            "minecraft:diamond_pickaxe, 0.6, 1.0, false",
            "minecraft:diamond_shovel, 2.3, 0.9, false",
            "minecraft:diamond_sword, 1.3, 1.0, false",
            "minecraft:diamond_axe, 3.5, 0.8, false",
            "minecraft:diamond_hoe, 0.5, 1.4, false",
            "minecraft:shield, 1.0, 0.8, true, 30, 1",
            "spartanshields:shield_basic_wood, 1.0, 1.0, true, 20, 1",
            "spartanshields:shield_basic_stone, 1.0, 0.9, true, 20, 2",
            "spartanshields:shield_basic_iron, 1.0, 0.8, true, 20, 3",
            "spartanshields:shield_basic_gold, 1.0, 0.9, true, 20, 3",
            "spartanshields:shield_basic_diamond, 1.0, 0.7, true, 20, 4",
            "spartanshields:shield_basic_netherite, 1.0, 0.6, true, 20, 4",
            "spartanshields:shield_basic_obsidian, 1.0, 0.6, true, 20, 4",
            "spartanshields:shield_basic_copper, 1.0, 0.9, true, 20, 2",
            "spartanshields:shield_basic_tin, 1.0, 0.9, true, 20, 2",
            "spartanshields:shield_basic_bronze, 1.0, 0.8, true, 20, 3",
            "spartanshields:shield_basic_steel, 1.0, 0.7, true, 20, 3",
            "spartanshields:shield_basic_silver, 1.0, 0.9, true, 20, 2",
            "spartanshields:shield_basic_lead, 1.0, 0.6, true, 20, 3",
            "spartanshields:shield_basic_nickel, 1.0, 0.7, true, 20, 3",
            "spartanshields:shield_basic_invar, 1.0, 0.7, true, 20, 3",
            "spartanshields:shield_basic_constantan, 1.0, 0.8, true, 20, 3",
            "spartanshields:shield_basic_platinum, 1.0, 0.5, true, 20, 4",
            "spartanshields:shield_basic_electrum, 1.0, 0.8, true, 20, 3",
            "spartanshields:shield_tower_wood, 1.5, 0.9, true, 40, 2",
            "spartanshields:shield_tower_stone, 1.5, 0.8, true, 40, 3",
            "spartanshields:shield_tower_iron, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_tower_gold, 1.5, 0.8, true, 40, 2",
            "spartanshields:shield_tower_diamond, 1.5, 0.6, true, 40, 5",
            "spartanshields:shield_tower_netherite, 1.5, 0.6, true, 40, 5",
            "spartanshields:shield_tower_obsidian, 1.5, 0.6, true, 40, 5",
            "spartanshields:shield_tower_copper, 1.5, 0.9, true, 40, 3",
            "spartanshields:shield_tower_tin, 1.5, 0.9, true, 40, 3",
            "spartanshields:shield_tower_bronze, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_tower_steel, 1.5, 0.6, true, 40, 4",
            "spartanshields:shield_tower_silver, 1.5, 0.8, true, 40, 3",
            "spartanshields:shield_tower_lead, 1.5, 0.5, true, 40, 4",
            "spartanshields:shield_tower_nickel, 1.5, 0.8, true, 40, 4",
            "spartanshields:shield_tower_invar, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_tower_constantan, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_tower_platinum, 1.5, 0.4, true, 40, 5",
            "spartanshields:shield_tower_electrum, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_botania_manasteel, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_botania_terrasteel, 1.5, 0.5, true, 40, 5",
            "spartanshields:shield_botania_elementium, 1.5, 0.6, true, 40, 4",
            "spartanshields:shield_mekanism_osmium, 1.5, 0.7, true, 40, 4",
            "spartanshields:shield_mekanism_lapis_lazuli, 1.5, 0.8, true, 40, 3",
            "spartanshields:shield_mekanism_refined_glowstone, 1.5, 0.7, true, 40, 3",
            "spartanshields:shield_mekanism_refined_obsidian, 1.5, 0.4, true, 40, 5",
            "immersiveengineering:shield, 3.0, 0.5, true, 50, 5",
            "bountifulbaubles:shield_cobalt, 3.0, 0.5, true, 60, 5",
            "bountifulbaubles:shield_obsidian, 3.0, 0.5, true, 60, 5",
            "bountifulbaubles:shield_ankh, 3.0, 0.5, true, 60, 5",
            "silentgear:shield, 1.5, 0.7, true, 30, 1",
            "minecraft:trident, 1.0, 1.6, false",
            "cyclic:crystal_pickaxe, 0.75, 1.0, false",
            "cyclic:emerald_pickaxe, 0.75, 1.0, false",
            "cyclic:netherbrick_pickaxe, 0.5, 1.0, false",
            "cyclic:sandstone_pickaxe, 0.4, 1.0, false",
            "immersiveengineering:pickaxe_steel, 0.6, 1.0, false",
            "cyclic:crystal_shovel, 2.3, 1.0, false",
            "cyclic:emerald_shovel, 2.3, 1.0, false",
            "cyclic:netherbrick_shovel, 1.7, 1.0, false",
            "cyclic:sandstone_shovel, 1.7, 1.0, false",
            "immersiveengineering:shovel_steel, 1.9, 1.0, false",
            "cyclic:crystal_axe, 3.5, 1.0, false",
            "cyclic:emerald_axe, 3.5, 1.0, false",
            "cyclic:netherbrick_axe, 2.9, 1.0, false",
            "cyclic:sandstone_axe, 2.5, 1.0, false",
            "immersiveengineering:axe_steel, 2.9, 1.0, false",
            "cyclic:crystal_sword, 1.4, 1.0, false",
            "cyclic:emerald_sword, 1.3, 1.0, false",
            "cyclic:netherbrick_sword, 1.1, 1.0, false",
            "cyclic:sandstone_sword, 1.0, 1.0, false",
            "immersiveengineering:sword_steel, 1.2, 1.0, false",
            "cyclic:crystal_hoe, 0.5, 1.4, false",
            "cyclic:emerald_hoe, 0.5, 1.4, false",
            "cyclic:sandstone_hoe, 0.25, 1.6, false",
            "cyclic:netherbrick_hoe, 0.25, 1.5, false",
            "immersiveengineering:hoe_steel, 0.25, 1.4, false",
            "spartanweaponry:dagger_wood, 0.1, 2.3, false",
            "spartanweaponry:dagger_stone, 0.1, 2.1, false",
            "spartanweaponry:dagger_iron, 0.2, 1.9, false",
            "spartanweaponry:dagger_gold, 0.1, 2.0, false",
            "spartanweaponry:dagger_diamond, 0.3, 1.9, false",
            "spartanweaponry:dagger_netherite, 0.3, 1.9, false",
            "spartanweaponry:dagger_copper, 0.1, 2.1, false",
            "spartanweaponry:dagger_tin, 0.1, 2.1, false",
            "spartanweaponry:dagger_bronze, 0.2, 2.0, false",
            "spartanweaponry:dagger_steel, 0.2, 2.0, false",
            "spartanweaponry:dagger_silver, 0.1, 2.1, false",
            "spartanweaponry:dagger_invar, 0.2, 2.0, false",
            "spartanweaponry:dagger_platinum, 0.3, 1.9, false",
            "spartanweaponry:dagger_electrum, 0.2, 2.0, false",
            "spartanweaponry:dagger_nickel, 0.2, 2.0, false",
            "spartanweaponry:dagger_lead, 0.3, 1.8, false",
            "spartanweaponry:longsword_wood, 1.7, 1.0, false",
            "spartanweaponry:longsword_stone, 1.8, 0.9, false",
            "spartanweaponry:longsword_iron, 1.9, 0.8, false",
            "spartanweaponry:longsword_gold, 1.8, 0.9, false",
            "spartanweaponry:longsword_diamond, 2.0, 0.7, false",
            "spartanweaponry:longsword_netherite, 2.0, 0.7, false",
            "spartanweaponry:longsword_copper, 1.8, 0.9, false",
            "spartanweaponry:longsword_tin, 1.8, 0.9, false",
            "spartanweaponry:longsword_bronze, 1.9, 0.8, false",
            "spartanweaponry:longsword_steel, 1.9, 0.8, false",
            "spartanweaponry:longsword_silver, 1.8, 0.9, false",
            "spartanweaponry:longsword_invar, 1.9, 0.8, false",
            "spartanweaponry:longsword_platinum, 2.0, 0.7, false",
            "spartanweaponry:longsword_electrum, 1.9, 0.8, false",
            "spartanweaponry:longsword_nickel, 1.9, 0.8, false",
            "spartanweaponry:longsword_lead, 2.0, 0.7, false",
            "spartanweaponry:katana_wood, 1.0, 1.25, false",
            "spartanweaponry:katana_stone, 1.1, 1.2, false",
            "spartanweaponry:katana_iron, 1.2, 1.15, false",
            "spartanweaponry:katana_gold, 1.1, 1.2, false",
            "spartanweaponry:katana_diamond, 1.3, 1.1, false",
            "spartanweaponry:katana_netherite, 1.3, 1.1, false",
            "spartanweaponry:katana_copper, 1.1, 1.15, false",
            "spartanweaponry:katana_tin, 1.1, 1.15, false",
            "spartanweaponry:katana_bronze, 1.2, 1.2, false",
            "spartanweaponry:katana_steel, 1.2, 1.2, false",
            "spartanweaponry:katana_silver, 1.1, 1.2, false",
            "spartanweaponry:katana_invar, 1.2, 1.15, false",
            "spartanweaponry:katana_platinum, 1.3, 1.1, false",
            "spartanweaponry:katana_electrum, 1.1, 1.2, false",
            "spartanweaponry:katana_nickel, 1.2, 1.1, false",
            "spartanweaponry:katana_lead, 1.2, 1.0, false",
            "spartanweaponry:saber_wood, 1.1, 1.1, false",
            "spartanweaponry:saber_stone, 1.2, 1.0, false",
            "spartanweaponry:saber_iron, 1.3, 0.9, false",
            "spartanweaponry:saber_gold, 1.2, 1.0, false",
            "spartanweaponry:saber_diamond, 1.4, 0.8, false",
            "spartanweaponry:saber_netherite, 1.4, 0.8, false",
            "spartanweaponry:saber_copper, 1.2, 1.0, false",
            "spartanweaponry:saber_tin, 1.2, 1.0, false",
            "spartanweaponry:saber_bronze, 1.3, 0.9, false",
            "spartanweaponry:saber_steel, 1.3, 0.9, false",
            "spartanweaponry:saber_silver, 1.2, 1.0, false",
            "spartanweaponry:saber_invar, 1.3, 0.9, false",
            "spartanweaponry:saber_platinum, 1.4, 0.8, false",
            "spartanweaponry:saber_electrum, 1.3, 0.9, false",
            "spartanweaponry:saber_nickel, 1.3, 0.9, false",
            "spartanweaponry:saber_lead, 1.3, 0.8, false",
            "spartanweaponry:rapier_wood, 0.3, 1.2, false",
            "spartanweaponry:rapier_stone, 0.5, 1.1, false",
            "spartanweaponry:rapier_iron, 0.6, 1.0, false",
            "spartanweaponry:rapier_gold, 0.6, 1.1, false",
            "spartanweaponry:rapier_diamond, 0.7, 0.9, false",
            "spartanweaponry:rapier_netherite, 0.7, 0.9, false",
            "spartanweaponry:rapier_copper, 0.5, 1.1, false",
            "spartanweaponry:rapier_tin, 0.5, 1.1, false",
            "spartanweaponry:rapier_bronze, 0.6, 1.0, false",
            "spartanweaponry:rapier_steel, 0.6, 1.0, false",
            "spartanweaponry:rapier_silver, 0.5, 1.1, false",
            "spartanweaponry:rapier_invar, 0.5, 1.0, false",
            "spartanweaponry:rapier_platinum, 0.5, 0.9, false",
            "spartanweaponry:rapier_electrum, 0.5, 1.1, false",
            "spartanweaponry:rapier_nickel, 0.5, 1.0, false",
            "spartanweaponry:rapier_lead, 0.5, 0.9, false",
            "spartanweaponry:greatsword_wood, 2.3, 1.1, false",
            "spartanweaponry:greatsword_stone, 2.4, 1.05, false",
            "spartanweaponry:greatsword_iron, 2.5, 1.0, false",
            "spartanweaponry:greatsword_gold, 2.4, 1.0, false",
            "spartanweaponry:greatsword_diamond, 2.6, 0.95, false",
            "spartanweaponry:greatsword_netherite, 2.6, 0.95, false",
            "spartanweaponry:greatsword_copper, 2.4, 1.05, false",
            "spartanweaponry:greatsword_tin, 2.4, 1.05, false",
            "spartanweaponry:greatsword_bronze, 2.5, 1.0, false",
            "spartanweaponry:greatsword_steel, 2.5, 1.0, false",
            "spartanweaponry:greatsword_silver, 2.4, 1.05, false",
            "spartanweaponry:greatsword_invar, 2.5, 1.0, false",
            "spartanweaponry:greatsword_platinum, 2.6, 0.9, false",
            "spartanweaponry:greatsword_electrum, 2.5, 1.0, false",
            "spartanweaponry:greatsword_nickel, 2.5, 1.0, false",
            "spartanweaponry:greatsword_lead, 2.6, 0.9, false",
            "spartanweaponry:hammer_wood, 4.0, 1.7, false",
            "spartanweaponry:hammer_stone, 4.2, 1.6, false",
            "spartanweaponry:hammer_iron, 4.4, 1.5, false",
            "spartanweaponry:hammer_gold, 4.2, 1.6, false",
            "spartanweaponry:hammer_diamond, 4.6, 1.5, false",
            "spartanweaponry:hammer_netherite, 4.6, 1.4, false",
            "spartanweaponry:hammer_copper, 4.2, 1.6, false",
            "spartanweaponry:hammer_tin, 4.2, 1.6, false",
            "spartanweaponry:hammer_bronze, 4.4, 1.6, false",
            "spartanweaponry:hammer_steel, 4.4, 1.5, false",
            "spartanweaponry:hammer_silver, 4.2, 1.6, false",
            "spartanweaponry:hammer_invar, 4.4, 1.5, false",
            "spartanweaponry:hammer_platinum, 4.6, 1.4, false",
            "spartanweaponry:hammer_electrum, 4.4, 1.5, false",
            "spartanweaponry:hammer_nickel, 4.4, 1.5, false",
            "spartanweaponry:hammer_lead, 5.0, 1.4, false",
            "spartanweaponry:warhammer_wood, 6.0, 1.7, false",
            "spartanweaponry:warhammer_stone, 6.2, 1.6, false",
            "spartanweaponry:warhammer_iron, 6.4, 1.5, false",
            "spartanweaponry:warhammer_gold, 6.2, 1.6, false",
            "spartanweaponry:warhammer_diamond, 6.6, 1.4, false",
            "spartanweaponry:warhammer_netherite, 6.6, 1.4, false",
            "spartanweaponry:warhammer_copper, 6.2, 1.6, false",
            "spartanweaponry:warhammer_tin, 6.2, 1.6, false",
            "spartanweaponry:warhammer_bronze, 6.4, 1.5, false",
            "spartanweaponry:warhammer_steel, 6.4, 1.5, false",
            "spartanweaponry:warhammer_silver, 6.2, 1.6, false",
            "spartanweaponry:warhammer_invar, 6.4, 1.5, false",
            "spartanweaponry:warhammer_platinum, 6.6, 1.4, false",
            "spartanweaponry:warhammer_electrum, 6.4, 1.5, false",
            "spartanweaponry:warhammer_nickel, 6.2, 1.5, false",
            "spartanweaponry:warhammer_lead, 6.0, 1.4, false",
            "spartanweaponry:spear_wood, 0.4, 1.8, false",
            "spartanweaponry:spear_stone, 0.5, 1.7, false",
            "spartanweaponry:spear_iron, 0.6, 1.6, false",
            "spartanweaponry:spear_gold, 0.5, 1.7, false",
            "spartanweaponry:spear_diamond, 0.7, 1.5, false",
            "spartanweaponry:spear_netherite, 0.7, 1.5, false",
            "spartanweaponry:spear_copper, 0.5, 1.6, false",
            "spartanweaponry:spear_tin, 0.5, 1.7, false",
            "spartanweaponry:spear_bronze, 0.6, 1.6, false",
            "spartanweaponry:spear_steel, 0.6, 1.6, false",
            "spartanweaponry:spear_silver, 0.5, 1.7, false",
            "spartanweaponry:spear_invar, 0.6, 1.6, false",
            "spartanweaponry:spear_platinum, 0.7, 1.5, false",
            "spartanweaponry:spear_electrum, 0.6, 1.6, false",
            "spartanweaponry:spear_nickel, 0.6, 1.6, false",
            "spartanweaponry:spear_lead, 0.7, 1.5, false",
            "spartanweaponry:halberd_wood, 3.6, 1.5, false",
            "spartanweaponry:halberd_stone, 3.8, 1.4, false",
            "spartanweaponry:halberd_iron, 4.0, 1.3, false",
            "spartanweaponry:halberd_gold, 3.8, 1.4, false",
            "spartanweaponry:halberd_diamond, 4.2, 1.2, false",
            "spartanweaponry:halberd_netherite, 4.2, 1.2, false",
            "spartanweaponry:halberd_copper, 3.8, 1.4, false",
            "spartanweaponry:halberd_tin, 3.8, 1.4, false",
            "spartanweaponry:halberd_bronze, 4.0, 1.3, false",
            "spartanweaponry:halberd_steel, 4.0, 1.3, false",
            "spartanweaponry:halberd_silver, 3.8, 1.4, false",
            "spartanweaponry:halberd_invar, 4.0, 1.3, false",
            "spartanweaponry:halberd_platinum, 4.2, 1.2, false",
            "spartanweaponry:halberd_electrum, 4.0, 1.3, false",
            "spartanweaponry:halberd_nickel, 4.0, 1.3, false",
            "spartanweaponry:halberd_lead, 4.2, 1.2, false",
            "spartanweaponry:pike_wood, 0.4, 1.8, false",
            "spartanweaponry:pike_stone, 0.5, 1.7, false",
            "spartanweaponry:pike_iron, 0.6, 1.6, false",
            "spartanweaponry:pike_gold, 0.5, 1.7, false",
            "spartanweaponry:pike_diamond, 0.7, 1.5, false",
            "spartanweaponry:pike_netherite, 0.7, 1.5, false",
            "spartanweaponry:pike_copper, 0.5, 1.6, false",
            "spartanweaponry:pike_tin, 0.5, 1.7, false",
            "spartanweaponry:pike_bronze, 0.6, 1.6, false",
            "spartanweaponry:pike_steel, 0.6, 1.6, false",
            "spartanweaponry:pike_silver, 0.5, 1.7, false",
            "spartanweaponry:pike_invar, 0.6, 1.6, false",
            "spartanweaponry:pike_platinum, 0.7, 1.5, false",
            "spartanweaponry:pike_electrum, 0.6, 1.6, false",
            "spartanweaponry:pike_nickel, 0.6, 1.6, false",
            "spartanweaponry:pike_lead, 0.7, 1.5, false",
            "spartanweaponry:lance_wood, 1.7, 1.0, false",
            "spartanweaponry:lance_stone, 1.8, 0.9, false",
            "spartanweaponry:lance_iron, 1.9, 0.8, false",
            "spartanweaponry:lance_gold, 1.8, 0.9, false",
            "spartanweaponry:lance_diamond, 2.0, 0.7, false",
            "spartanweaponry:lance_netherite, 2.0, 0.7, false",
            "spartanweaponry:lance_copper, 1.8, 0.9, false",
            "spartanweaponry:lance_tin, 1.8, 0.9, false",
            "spartanweaponry:lance_bronze, 1.9, 0.8, false",
            "spartanweaponry:lance_steel, 1.9, 0.8, false",
            "spartanweaponry:lance_silver, 1.8, 0.9, false",
            "spartanweaponry:lance_invar, 1.9, 0.8, false",
            "spartanweaponry:lance_platinum, 2.0, 0.7, false",
            "spartanweaponry:lance_electrum, 1.9, 0.8, false",
            "spartanweaponry:lance_nickel, 1.9, 0.8, false",
            "spartanweaponry:lance_lead, 2.0, 0.7, false",
            "spartanweaponry:battleaxe_wood, 4.0, 1.7, false",
            "spartanweaponry:battleaxe_stone, 4.2, 1.6, false",
            "spartanweaponry:battleaxe_iron, 4.4, 1.5, false",
            "spartanweaponry:battleaxe_gold, 4.2, 1.6, false",
            "spartanweaponry:battleaxe_diamond, 4.6, 1.4, false",
            "spartanweaponry:battleaxe_netherite, 4.6, 1.4, false",
            "spartanweaponry:battleaxe_copper, 4.2, 1.6, false",
            "spartanweaponry:battleaxe_tin, 4.2, 1.6, false",
            "spartanweaponry:battleaxe_bronze, 4.4, 1.6, false",
            "spartanweaponry:battleaxe_steel, 4.4, 1.5, false",
            "spartanweaponry:battleaxe_silver, 4.2, 1.6, false",
            "spartanweaponry:battleaxe_invar, 4.4, 1.5, false",
            "spartanweaponry:battleaxe_platinum, 4.6, 1.4, false",
            "spartanweaponry:battleaxe_electrum, 4.4, 1.5, false",
            "spartanweaponry:battleaxe_nickel, 4.4, 1.5, false",
            "spartanweaponry:battleaxe_lead, 5.0, 1.4, false",
            "spartanweaponry:flanged_mace_wood, 4.0, 1.7, false",
            "spartanweaponry:flanged_mace_stone, 4.2, 1.6, false",
            "spartanweaponry:flanged_mace_iron, 4.4, 1.5, false",
            "spartanweaponry:flanged_mace_gold, 4.2, 1.6, false",
            "spartanweaponry:flanged_mace_diamond, 4.6, 1.5, false",
            "spartanweaponry:flanged_mace_netherite, 4.6, 1.4, false",
            "spartanweaponry:flanged_mace_copper, 4.2, 1.6, false",
            "spartanweaponry:flanged_mace_tin, 4.2, 1.6, false",
            "spartanweaponry:flanged_mace_bronze, 4.4, 1.6, false",
            "spartanweaponry:flanged_mace_steel, 4.4, 1.5, false",
            "spartanweaponry:flanged_mace_silver, 4.2, 1.6, false",
            "spartanweaponry:flanged_mace_invar, 4.4, 1.5, false",
            "spartanweaponry:flanged_mace_platinum, 4.6, 1.4, false",
            "spartanweaponry:flanged_mace_electrum, 4.4, 1.5, false",
            "spartanweaponry:flanged_mace_nickel, 4.4, 1.5, false",
            "spartanweaponry:flanged_mace_lead, 5.0, 1.4, false",
            "spartanweaponry:glaive_wood, 1.0, 1.2, false",
            "spartanweaponry:glaive_stone, 1.1, 1.1, false",
            "spartanweaponry:glaive_iron, 1.2, 1.0, false",
            "spartanweaponry:glaive_gold, 1.1, 1.0, false",
            "spartanweaponry:glaive_diamond, 1.3, 0.9, false",
            "spartanweaponry:glaive_netherite, 1.3, 0.9, false",
            "spartanweaponry:glaive_copper, 1.1, 1.1, false",
            "spartanweaponry:glaive_tin, 1.1, 1.1, false",
            "spartanweaponry:glaive_bronze, 1.2, 1.0, false",
            "spartanweaponry:glaive_steel, 1.2, 1.0, false",
            "spartanweaponry:glaive_silver, 1.1, 1.1, false",
            "spartanweaponry:glaive_invar, 1.2, 1.0, false",
            "spartanweaponry:glaive_platinum, 1.3, 0.9, false",
            "spartanweaponry:glaive_electrum, 1.2, 1.1, false",
            "spartanweaponry:glaive_nickel, 1.2, 1.0, false",
            "spartanweaponry:glaive_lead, 1.3, 1.0, false",
            "spartanweaponry:quarterstaff_wood, 2.0, 1.0, false",
            "spartanweaponry:quarterstaff_stone, 2.1, 0.9, false",
            "spartanweaponry:quarterstaff_iron, 2.2, 0.8, false",
            "spartanweaponry:quarterstaff_gold, 2.1, 0.8, false",
            "spartanweaponry:quarterstaff_diamond, 2.3, 0.7, false",
            "spartanweaponry:quarterstaff_netherite, 2.3, 0.7, false",
            "spartanweaponry:quarterstaff_copper, 2.1, 0.9, false",
            "spartanweaponry:quarterstaff_tin, 2.1, 0.9, false",
            "spartanweaponry:quarterstaff_bronze, 2.2, 0.8, false",
            "spartanweaponry:quarterstaff_steel, 2.2, 0.8, false",
            "spartanweaponry:quarterstaff_silver, 2.1, 0.9, false",
            "spartanweaponry:quarterstaff_invar, 2.1, 0.8, false",
            "spartanweaponry:quarterstaff_platinum, 2.3, 0.7, false",
            "spartanweaponry:quarterstaff_electrum, 2.1, 0.9, false",
            "spartanweaponry:quarterstaff_nickel, 2.1, 0.8, false",
            "spartanweaponry:quarterstaff_lead, 2.2, 0.8, false",
            "spartanweaponry:tomahawk_wood, 2.0, 1.25, false",
            "spartanweaponry:tomahawk_stone, 2.1, 1.2, false",
            "spartanweaponry:tomahawk_iron, 2.2, 1.15, false",
            "spartanweaponry:tomahawk_gold, 2.1, 1.2, false",
            "spartanweaponry:tomahawk_diamond, 2.3, 1.1, false",
            "spartanweaponry:tomahawk_netherite, 2.3, 1.1, false",
            "spartanweaponry:tomahawk_copper, 2.1, 1.15, false",
            "spartanweaponry:tomahawk_tin, 2.1, 1.15, false",
            "spartanweaponry:tomahawk_bronze, 2.2, 1.2, false",
            "spartanweaponry:tomahawk_steel, 2.2, 1.2, false",
            "spartanweaponry:tomahawk_silver, 2.1, 1.2, false",
            "spartanweaponry:tomahawk_invar, 2.2, 1.15, false",
            "spartanweaponry:tomahawk_platinum, 2.3, 1.1, false",
            "spartanweaponry:tomahawk_electrum, 2.1, 1.2, false",
            "spartanweaponry:tomahawk_nickel, 2.2, 1.1, false",
            "spartanweaponry:tomahawk_lead, 2.2, 1.0, false",
            "spartanweaponry:throwing_knife_wood, 0.1, 2.3, false",
            "spartanweaponry:throwing_knife_stone, 0.1, 2.1, false",
            "spartanweaponry:throwing_knife_iron, 0.2, 1.9, false",
            "spartanweaponry:throwing_knife_gold, 0.1, 2.0, false",
            "spartanweaponry:throwing_knife_diamond, 0.3, 1.9, false",
            "spartanweaponry:throwing_knife_netherite, 0.3, 1.9, false",
            "spartanweaponry:throwing_knife_copper, 0.1, 2.1, false",
            "spartanweaponry:throwing_knife_tin, 0.1, 2.1, false",
            "spartanweaponry:throwing_knife_bronze, 0.2, 2.0, false",
            "spartanweaponry:throwing_knife_steel, 0.2, 2.0, false",
            "spartanweaponry:throwing_knife_silver, 0.1, 2.1, false",
            "spartanweaponry:throwing_knife_invar, 0.2, 2.0, false",
            "spartanweaponry:throwing_knife_platinum, 0.3, 1.9, false",
            "spartanweaponry:throwing_knife_electrum, 0.2, 2.0, false",
            "spartanweaponry:throwing_knife_nickel, 0.2, 2.0, false",
            "spartanweaponry:throwing_knife_lead, 0.3, 1.8, false",
            "spartanweaponry:javelin_wood, 0.4, 1.8, false",
            "spartanweaponry:javelin_stone, 0.5, 1.7, false",
            "spartanweaponry:javelin_iron, 0.6, 1.6, false",
            "spartanweaponry:javelin_gold, 0.5, 1.7, false",
            "spartanweaponry:javelin_diamond, 0.7, 1.5, false",
            "spartanweaponry:javelin_netherite, 0.7, 1.5, false",
            "spartanweaponry:javelin_copper, 0.5, 1.6, false",
            "spartanweaponry:javelin_tin, 0.5, 1.7, false",
            "spartanweaponry:javelin_bronze, 0.6, 1.6, false",
            "spartanweaponry:javelin_steel, 0.6, 1.6, false",
            "spartanweaponry:javelin_silver, 0.5, 1.7, false",
            "spartanweaponry:javelin_invar, 0.6, 1.6, false",
            "spartanweaponry:javelin_platinum, 0.7, 1.5, false",
            "spartanweaponry:javelin_electrum, 0.6, 1.6, false",
            "spartanweaponry:javelin_nickel, 0.6, 1.6, false",
            "spartanweaponry:javelin_lead, 0.7, 1.5, false",
            "spartanweaponry:boomerang_wood, 1.0, 1.2, false",
            "spartanweaponry:boomerang_stone, 1.1, 1.1, false",
            "spartanweaponry:boomerang_iron, 1.2, 1.0, false",
            "spartanweaponry:boomerang_gold, 1.1, 1.0, false",
            "spartanweaponry:boomerang_diamond, 1.3, 0.9, false",
            "spartanweaponry:boomerang_netherite, 1.3, 0.9, false",
            "spartanweaponry:boomerang_copper, 1.1, 1.1, false",
            "spartanweaponry:boomerang_tin, 1.1, 1.1, false",
            "spartanweaponry:boomerang_bronze, 1.2, 1.0, false",
            "spartanweaponry:boomerang_steel, 1.2, 1.0, false",
            "spartanweaponry:boomerang_silver, 1.1, 1.1, false",
            "spartanweaponry:boomerang_invar, 1.2, 1.0, false",
            "spartanweaponry:boomerang_platinum, 1.3, 0.9, false",
            "spartanweaponry:boomerang_electrum, 1.2, 1.1, false",
            "spartanweaponry:boomerang_nickel, 1.2, 1.0, false",
            "spartanweaponry:boomerang_lead, 1.3, 1.0, false",
            "spartanweaponry:club_wood, 3.0, 1.5, false",
            "spartanweaponry:club_studded, 4.0, 1.4, false",
            "spartanweaponry:cestus, 2.0, 0.8, false",
            "spartanweaponry:cestus_studded, 2.4, 0.7, false",
            "silentgear:sword, 1.5, 1.0, false",
            "silentgear:katana, 1.3, 1.1, false",
            "silentgear:machete, 1.8, 1.0, false",
            "silentgear:spear, 0.7, 1.5, false",
            "silentgear:knife, 0.5, 1.8, false",
            "silentgear:dagger, 0.5, 1.8, false",
            "silentgear:pickaxe, 0.5, 1.0, false",
            "silentgear:shovel, 2.0, 1.1, false",
            "silentgear:axe, 2.8, 0.8, false",
            "silentgear:paxel, 2.0, 1.1, false",
            "silentgear:hammer, 4.0, 1.5, false",
            "silentgear:excavator, 3.0, 1.3, false",
            "silentgear:saw, 0.5, 1.7, false",
            "silentgear:prospector_hammer, 2.0, 1.2, false",
            "silentgear:mattock, 1.5, 1.2, false",
            "silentgear:sickle, 0.25, 1.6, false",
            "minecraft:netherite_pickaxe, 0.75, 1.0, false",
            "minecraft:netherite_shovel, 2.3, 0.9, false",
            "minecraft:netherite_sword, 1.3, 1.0, false",
            "iceandfire:copper_pickaxe, 0.4, 1.0, false", "iceandfire:copper_shovel, 1.7, 0.9, false", "iceandfire:copper_sword, 1.1, 1.0, false", "iceandfire:copper_axe, 2.7, 0.9, false", "iceandfire:copper_hoe, 0.3, 1.5, false", "iceandfire:silver_pickaxe, 0.5, 1.0, false", "iceandfire:silver_shovel, 1.8, 0.9, false", "iceandfire:silver_sword, 1.2, 1.0, false", "iceandfire:silver_axe, 3.0, 0.8, false", "iceandfire:silver_hoe, 0.4, 1.4, false", "iceandfire:dragonbone_pickaxe, 0.7, 1.0, false", "iceandfire:dragonbone_shovel, 2.6, 0.9, false", "iceandfire:dragonbone_sword, 1.4, 1.0, false", "iceandfire:dragonbone_sword_ice, 1.5, 1.0, false", "iceandfire:dragonbone_sword_fire, 1.4, 0.9, false", "iceandfire:dragonbone_sword_lightning, 1.5, 0.9, false", "iceandfire:hippogryph_sword, 1.2, 1.0, false", "iceandfire:hippocampus_slapper, 1.5, 1.4, false", "iceandfire:deathworm_gauntlet_red, 1.0, 0.8, false", "iceandfire:deathworm_gauntlet_white, 1.0, 0.8, false", "iceandfire:deathworm_gauntlet_yellow, 1.0, 0.8, false", "iceandfire:dragonbone_axe, 3.8, 0.8, false", "iceandfire:dragonbone_hoe, 0.6, 1.4, false", "iceandfire:myrmex_desert_pickaxe, 0.5, 1.0, false", "iceandfire:myrmex_desert_shovel, 1.8, 0.9, false", "iceandfire:myrmex_desert_sword, 1.3, 1.0, false", "iceandfire:myrmex_desert_sword_venom, 1.3, 1.0, false", "iceandfire:myrmex_desert_axe, 3.4, 0.8, false", "iceandfire:myrmex_desert_hoe, 0.5, 1.4, false", "iceandfire:myrmex_jungle_pickaxe, 0.5, 1.0, false", "iceandfire:myrmex_jungle_shovel, 1.8, 0.9, false", "iceandfire:myrmex_jungle_sword, 1.3, 1.0, false", "iceandfire:myrmex_jungle_sword_venom, 1.3, 1.0, false", "iceandfire:myrmex_jungle_axe, 3.4, 0.8, false", "iceandfire:myrmex_jungle_hoe, 0.5, 1.4, false", "iceandfire:dragonsteel_ice_pickaxe, 0.9, 0.85, false", "iceandfire:dragonsteel_ice_shovel, 3.0, 0.75, false", "iceandfire:dragonsteel_ice_sword, 1.6, 0.85, false", "iceandfire:dragonsteel_ice_axe, 4.0, 0.75, false", "iceandfire:dragonsteel_ice_hoe, 0.7, 1.2, false", "iceandfire:dragonsteel_fire_pickaxe, 1.0, 0.9, false", "iceandfire:dragonsteel_fire_shovel, 3.3, 0.8, false", "iceandfire:dragonsteel_fire_sword, 1.8, 0.9, false", "iceandfire:dragonsteel_fire_axe, 4.4, 0.8, false", "iceandfire:dragonsteel_fire_hoe, 0.9, 1.3, false", "iceandfire:dragonsteel_lightning_pickaxe, 0.9, 0.9, false", "iceandfire:dragonsteel_lightning_shovel, 3.0, 0.8, false", "iceandfire:dragonsteel_lightning_sword, 1.6, 0.9, false", "iceandfire:dragonsteel_lightning_axe, 4.0, 0.8, false", "iceandfire:dragonsteel_lightning_hoe, 0.7, 1.3, false", "iceandfire:dread_sword, 1.2, 1.0, false", "iceandfire:dread_knight_sword, 1.5, 0.9, false", "iceandfire:dread_queen_sword, 1.7, 0.9, false", "iceandfire:ghost_sword, 1.3, 0.9, false", "iceandfire:troll_weapon_axe, 10.0, 0.5, true, 20, 1", "iceandfire:troll_weapon_column, 10.0, 0.5, true, 20, 1", "iceandfire:troll_weapon_column_forest, 10.0, 0.5, true, 20, 1", "iceandfire:troll_weapon_column_frost, 10.0, 0.5, true, 20, 1", "iceandfire:troll_weapon_hammer, 10.0, 0.5, true, 20, 1", "iceandfire:troll_weapon_trunk, 10.0, 0.5, true, 20, 1", "iceandfire:troll_weapon_trunk_frost, 10.0, 0.5, true, 20, 1", "bloodmagic:soulpickaxe, 0.5, 1.0, false", "bloodmagic:soulshovel, 1.8, 0.9, false", "bloodmagic:soulsword, 1.2, 1.0, false", "bloodmagic:soulaxe, 3.0, 0.8, false", "bloodmagic:soulscythe, 0.4, 1.4, false", "pandoras_creatures:arachnon_hammer, 3.0, 0.8, false", "enigmaticlegacy:astral_breaker, 3.0, 0.8, false", "vampirism:heart_seeker_normal, 1.2, 1.1, true, 1,1", "vampirism:heart_seeker_enhanced, 1.3, 1.05, true, 1,1", "vampirism:heart_seeker_ultimate, 1.4, 1.0, true, 1,1", "vampirism:heart_striker_normal, 0.8, 0.9, true, 1,1", "vampirism:heart_striker_enhanced, 0.9, 0.85, true, 1,1", "vampirism:heart_striker_ultimate, 1.0, 0.8, true, 1,1", "vampirism:hunter_axe_normal, 4.0, 0.9, true, 1,1", "vampirism:hunter_axe_enhanced, 4.2, 0.8, true, 1,1", "vampirism:hunter_axe_ultimate, 4.5, 0.7, true, 1,1", "consecration:fire_stick, 0.5, 1.2, false", "eidolon:reversal_pick, 0.5, 1.0, false", "eidolon:reaper_scythe, 2.0, 0.9, false", "eidolon:sapping_sword, 1.2, 1.0, false", "wyrmroost:platinum_pickaxe, 0.5, 1.0, false", "wyrmroost:platinum_shovel, 1.8, 0.9, false", "wyrmroost:platinum_sword, 1.2, 1.0, false", "wyrmroost:platinum_axe, 3.0, 0.8, false", "wyrmroost:platinum_hoe, 0.4, 1.4, false", "wyrmroost:blue_geode_pickaxe, 0.5, 1.0, false", "wyrmroost:blue_geode_shovel, 2.0, 0.9, false", "wyrmroost:blue_geode_sword, 1.2, 1.0, false", "enigmaticlegacy:forbidden_axe, 1.2, 1.0, false", "wyrmroost:blue_geode_axe, 3.3, 0.8, false", "wyrmroost:blue_geode_hoe, 0.4, 1.4, false", "wyrmroost:red_geode_pickaxe, 0.6, 1.0, false", "wyrmroost:red_geode_shovel, 2.4, 0.9, false", "wyrmroost:red_geode_sword, 1.3, 1.0, false", "wyrmroost:red_geode_axe, 3.6, 0.8, false", "wyrmroost:red_geode_hoe, 0.5, 1.4, false", "wyrmroost:purple_geode_pickaxe, 0.7, 1.0, false", "wyrmroost:purple_geode_shovel, 2.6, 0.9, false", "wyrmroost:purple_geode_sword, 1.4, 1.0, false", "wyrmroost:purple_geode_axe, 3.8, 0.8, false", "wyrmroost:purple_geode_hoe, 0.6, 1.4, false", "byg:pendorite_pickaxe, 0.7, 1.0, false", "byg:pendorite_shovel, 2.6, 0.9, false", "byg:pendorite_sword, 1.4, 1.0, false", "byg:pendorite_axe, 3.8, 0.8, false", "byg:pendorite_battleaxe, 4.0, 0.9, false", "byg:pendorite_hoe, 0.6, 1.4, false", "aquaculture:neptunium_pickaxe, 0.7, 1.0, false", "aquaculture:neptunium_shovel, 2.6, 0.9, false", "aquaculture:neptunium_sword, 1.4, 1.0, false", "aquaculture:neptunium_axe, 3.8, 0.8, false", "aquaculture:neptunium_hoe, 0.6, 1.4, false", "aquaculture:wooden_fillet_knife, 0.5, 1.4, false", "aquaculture:stone_fillet_knife, 0.5, 1.4, false", "aquaculture:iron_fillet_knife, 0.6, 1.3, false", "aquaculture:gold_fillet_knife, 0.6, 1.4, false", "aquaculture:diamond_fillet_knife, 0.7, 1.3, false", "aquaculture:neptunium_fillet_knife, 0.8, 1.1, false", "mahoutsukai:caliburn, 1.2, 1.0, false", "mahoutsukai:clarent, 1.2, 1.0, false", "mahoutsukai:morgan, 1.2, 1.0, false", "mahoutsukai:dagger, 0.5, 1.3, false", "mahoutsukai:theripper, 0.5, 1.3, false", "mahoutsukai:proximity_projection_keys, 0.5, 1.3, false", "enigmaticlegacy:etherium_pickaxe, 0.7, 1.0, false", "enigmaticlegacy:etherium_shovel, 2.6, 0.9, false", "enigmaticlegacy:etherium_sword, 1.4, 1.0, false", "enigmaticlegacy:etherium_axe, 3.8, 0.8, false", "enigmaticlegacy:etherium_hoe, 0.6, 1.4, false", "create:deforester, 3.8, 0.8, false", "astral:phantasmal_pickaxe, 0.4, 1.0, false", "astral:phantasmal_shovel, 1.7, 0.9, false", "astral:phantasmal_sword, 1.1, 1.0, false", "astral:phantasmal_axe, 2.7, 0.9, false", "astral:phantasmal_hoe, 0.3, 1.5, false", "vampirism:pitchfork, 1.0, 1.6, false", "vampirism:stake, 1.0, 1.6, false", "meetyourfight:depth_star, 3.0, 1.1, false", "meetyourfight:cocktail_cutlass, 1.2, 1.1, false", "evilcraft:mace_of_distortion, 2.4, 0.9, false", "evilcraft:mace_of_destruction, 2.4, 0.9, false", "evilcraft:vein_sword, 1.3, 1.0, false", "evilcraft:vengeance_pickaxe, 0.4, 1.3, false"
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
            "minecraft:netherite_boots, 0.2, 6, 0"

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
    private final ForgeConfigSpec.DoubleValue _mobParryChanceWeapon;
    private final ForgeConfigSpec.DoubleValue _mobParryChanceShield;
    private final ForgeConfigSpec.DoubleValue _mobScaler;
    private final ForgeConfigSpec.DoubleValue _kenshiroScaler;
    private final ForgeConfigSpec.DoubleValue _wound;
    private final ForgeConfigSpec.DoubleValue _fatig;
    private final ForgeConfigSpec.DoubleValue _burno;
    private final ForgeConfigSpec.BooleanValue _elenai;
    private final ForgeConfigSpec.BooleanValue _elenaiP;
    private final ForgeConfigSpec.BooleanValue _elenaiC;

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
    public static int mobUpdateInterval;
    public static int qiGrace;
    public static int comboGrace;
    public static int spiritCD;
    public static int postureCD;
    public static float mobParryChanceWeapon, mobParryChanceShield, mobScaler, kenshiroScaler;
    public static float wound, fatigue, burnout;
    public static boolean elenai, elenaiP, elenaiC;

    public CombatConfig(ForgeConfigSpec.Builder b) {
        b.push("numbers");
        _posturePerProjectile = b.translation("wardance.config.ppp").comment("Posture consumed per projectile parried").defineInRange("posture per projectile", 0.5, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureAttack = b.translation("wardance.config.dmpa").comment("Default multiplier for any items not defined in the config, multiplied by their attack damage").defineInRange("default attack multiplier", 0.15, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureDefend = b.translation("wardance.config.dmpd").comment("Default multiplier for any item not defined in the config, when used for parrying").defineInRange("default defense multiplier", 1.4, 0, Double.MAX_VALUE);
        _rollThreshold = b.translation("wardance.config.rollT").comment("Within this number of ticks after rolling the entity is considered invulnerable. This also determines the animation, so changing it might mean you land before or after standing up again").defineInRange("roll time", 10, 0, Integer.MAX_VALUE);
        _rollCooldown = b.translation("wardance.config.rollC").comment("Within this number of ticks after dodging the entity cannot dodge again").defineInRange("roll cooldown", 20, 0, Integer.MAX_VALUE);
        _shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("default shield time", 16, 0, Integer.MAX_VALUE);
        _shieldCount = b.translation("wardance.config.shieldT").comment("This many parries are free after a parry that cost posture").defineInRange("default shield count", 1, 0, Integer.MAX_VALUE);
        _mobUpdateInterval = b.translation("wardance.config.mobU").comment("Mobs are forced to sync to client every this number of ticks").defineInRange("forced mob update interval", 100, 1, Integer.MAX_VALUE);
        _qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining qi during which it will not decrease").defineInRange("qi grace period", 100, 1, Integer.MAX_VALUE);
        _comboGrace = b.translation("wardance.config.comboG").comment("Number of ticks after gaining combo during which it will not decrease").defineInRange("combo grace period", 100, 1, Integer.MAX_VALUE);
        _spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spirit cooldown", 30, 1, Integer.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("posture cooldown", 30, 1, Integer.MAX_VALUE);
        _staggerDuration = b.translation("wardance.config.staggerD").comment("Maximum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("max stagger duration", 60, 1, Integer.MAX_VALUE);
        _staggerDurationMin = b.translation("wardance.config.staggerM").comment("Minimum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("min stagger duration", 10, 1, Integer.MAX_VALUE);
        _staggerHits = b.translation("wardance.config.staggerH").comment("Number of hits a staggered entity will take before stagger is automatically canceled").defineInRange("stagger hits", 3, 1, Integer.MAX_VALUE);
        _shatterCooldown = b.translation("wardance.config.shatterCD").comment("Ticks after a hit for which shatter will not be replenished").defineInRange("shatter cooldown", 200, 1, Integer.MAX_VALUE);
        _mobParryChanceWeapon = b.translation("wardance.config.mobPW").comment("chance that a mob parries with a weapon out of 1").defineInRange("mob weapon parry chance", 0.3, 0, 1);
        _mobParryChanceShield = b.translation("wardance.config.mobPS").comment("chance that a mob parries with a shield out of 1").defineInRange("mob shield parry chance", 0.9, 0, 1);
        _mobScaler = b.translation("wardance.config.mobB").comment("posture damage from mob attacks will be scaled by this number").defineInRange("mob posture damage buff", 2, 0, Double.MAX_VALUE);
        _kenshiroScaler = b.translation("wardance.config.kenB").comment("posture damage from empty fists will be scaled by this number. Notice many mobs, such as endermen and ravagers, technically are empty-handed!").defineInRange("unarmed buff", 1.6, 0, Double.MAX_VALUE);
        _wound = b.translation("wardance.config.wound").comment("this percentage of incoming damage before armor is also added to wounding").defineInRange("wound percentage", 0.1, 0, 1d);
        _fatig = b.translation("wardance.config.fatigue").comment("this percentage of posture damage is also added to fatigue").defineInRange("fatigue percentage", 0.1, 0, 1d);
        _burno = b.translation("wardance.config.burnout").comment("this percentage of stamina use is also added to burnout").defineInRange("burnout percentage", 0.1, 0, 1d);
        b.pop();
        b.push("compat");
        _elenai = b.translation("wardance.config.elenaiCompat").comment("whether Elenai Dodge 2 compat is enabled. This disables sidesteps and rolls, turns dodging into a safety roll when staggered, and causes dodges to reset posture cooldown").define("enable Elenai Dodge compat", true);
        _elenaiP = b.translation("wardance.config.elenaiPosture").comment("if compat is enabled, whether posture cooldown disables feather recharging").define("feather posture", true);
        _elenaiC = b.translation("wardance.config.elenaiCombo").comment("if compat is enabled, whether high combo multiplies feather regeneration speed").define("feather combo", true);
        b.pop();
        b.push("lists");
        _combatItems = b.translation("wardance.config.combatItems").comment("Items eligible for parrying. Format should be name, attack posture consumption, defense multiplier, is shield. Shields can have two extra variables that dictate their parry time and parry counter. Default values provided graciously by DarkMega, thank you!").defineList("combat items", Arrays.asList(THANKS_DARKMEGA), String.class::isInstance);
        _customPosture = b.translation("wardance.config.postureMobs").comment("Here you can define custom max posture for mobs. Format is name, max posture, whether they're rotated when staggered. Armor is still calculated").defineList("custom mob posture", Lists.newArrayList("example:dragon, 100, false", "example:ghast, 8, true"), String.class::isInstance);
        _customArmor = b.translation("wardance.config.armorItems").comment("define protective stats of armor and held items here. Format is item, absorption, deflection, shatter.").defineList("custom protection attributes", Lists.newArrayList(ARMOR), String.class::isInstance);
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
        shatterCooldown = CONFIG._shatterCooldown.get();
        mobParryChanceWeapon = CONFIG._mobParryChanceWeapon.get().floatValue();
        mobParryChanceShield = CONFIG._mobParryChanceShield.get().floatValue();
        mobScaler = CONFIG._mobScaler.get().floatValue();
        kenshiroScaler = CONFIG._kenshiroScaler.get().floatValue();
        wound = CONFIG._wound.get().floatValue();
        fatigue = CONFIG._fatig.get().floatValue();
        burnout = CONFIG._burno.get().floatValue();
        elenai = CONFIG._elenai.get();
        elenaiC = CONFIG._elenaiC.get();
        elenaiP = CONFIG._elenaiP.get();
        CombatUtils.updateLists(CONFIG._combatItems.get(), CONFIG._customPosture.get(), CONFIG._customArmor.get(), defaultMultiplierPostureAttack, defaultMultiplierPostureDefend, shieldThreshold, shieldCount);
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            WarDance.LOGGER.debug("loading combat config!");
            bake();
        }
    }
}
