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
    private static final String[] THANKS_DARKMEGA={
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
            "silentgear:shield, 1.5, 0.7, true",
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
            "silentgear:sickle, 0.25, 1.6, false"
    };

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
    public static int shieldCount;
    public static int staggerDuration;
    public static int staggerDurationMin;
    public static int staggerHits;
    public static int mobUpdateInterval;
    public static int qiGrace;
    public static int comboGrace;
    public static int spiritCD;
    public static int postureCD;
    public static float mobParryChance;

    public CombatConfig(ForgeConfigSpec.Builder b) {
        b.push("numbers");
        _posturePerProjectile = b.translation("wardance.config.ppp").comment("Posture consumed per projectile parried").defineInRange("posturePerProjectile", 0.5, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureAttack = b.translation("wardance.config.dmpa").comment("Default multiplier for any items not defined in the config, multiplied by their attack damage").defineInRange("defaultPostureMultiplierAttack", 0.15, 0, Double.MAX_VALUE);
        _defaultMultiplierPostureDefend = b.translation("wardance.config.dmpd").comment("Default multiplier for any item not defined in the config, when used for parrying").defineInRange("defaultPostureMultiplierDefend", 1.4, 0, Double.MAX_VALUE);
        _rollThreshold = b.translation("wardance.config.rollT").comment("Within this number of ticks after rolling the entity is considered invulnerable").defineInRange("rollThreshold", 15, 0, Integer.MAX_VALUE);
        _rollCooldown = b.translation("wardance.config.rollC").comment("Within this number of ticks after rolling the entity cannot roll again").defineInRange("rollCooldown", 20, 0, Integer.MAX_VALUE);
        _shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("shieldThreshold", 16, 0, Integer.MAX_VALUE);
        _shieldCount = b.translation("wardance.config.shieldT").comment("This many parries are free after a parry that cost posture").defineInRange("shieldCount", 1, 0, Integer.MAX_VALUE);
        _mobUpdateInterval = b.translation("wardance.config.mobU").comment("Mobs are forced to sync to client every this number of ticks").defineInRange("mobUpdateInterval", 100, 1, Integer.MAX_VALUE);
        _qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining qi during which it will not decrease").defineInRange("qiGrace", 100, 1, Integer.MAX_VALUE);
        _comboGrace = b.translation("wardance.config.comboG").comment("Number of ticks after gaining combo during which it will not decrease").defineInRange("comboGrace", 100, 1, Integer.MAX_VALUE);
        _spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spiritCD", 30, 1, Integer.MAX_VALUE);
        _postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("postureCD", 30, 1, Integer.MAX_VALUE);
        _staggerDuration = b.translation("wardance.config.staggerD").comment("Maximum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("staggerDurationMax", 60, 1, Integer.MAX_VALUE);
        _staggerDurationMin = b.translation("wardance.config.staggerM").comment("Minimum number of ticks an entity should be staggered for when its posture reaches 0. The actual length of a given stagger is scaled by HP between the min and max values").defineInRange("staggerDurationMin", 10, 1, Integer.MAX_VALUE);
        _staggerHits = b.translation("wardance.config.staggerH").comment("Number of hits a staggered entity will take before stagger is automatically canceled").defineInRange("staggerHits", 3, 1, Integer.MAX_VALUE);
        _mobParryChance = b.translation("wardance.config.mobP").comment("chance that a mob parries out of 1").defineInRange("mobParryChance", 0.6, 0, 1);
        b.pop();
        b.push("lists");
        _combatItems = b.translation("wardance.config.combatItems").comment("Items eligible for parrying. Format should be name, attack posture consumption, defense multiplier, is shield. Shields can have two extra variables that dictate their parry time and parry counter. Default values provided graciously by DarkMega, thank you!").defineList("combatItems", Arrays.asList(THANKS_DARKMEGA), String.class::isInstance);
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
        shieldCount = CONFIG._shieldCount.get();
        mobUpdateInterval = CONFIG._mobUpdateInterval.get();
        qiGrace = CONFIG._qiGrace.get();
        comboGrace = CONFIG._comboGrace.get();
        spiritCD = CONFIG._spiritCD.get();
        postureCD = CONFIG._postureCD.get();
        staggerDuration = CONFIG._staggerDuration.get();
        staggerDurationMin = CONFIG._staggerDurationMin.get();
        staggerHits = CONFIG._staggerHits.get();
        mobParryChance = CONFIG._mobParryChance.get().floatValue();
        CombatUtils.updateLists(CONFIG._combatItems.get(), CONFIG._customPosture.get(), defaultMultiplierPostureAttack, defaultMultiplierPostureDefend, shieldThreshold, shieldCount);
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            WarDance.LOGGER.debug("loading combat config!");
            bake();
        }
    }
}
