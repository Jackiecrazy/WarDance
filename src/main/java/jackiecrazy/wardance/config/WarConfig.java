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

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class WarConfig {
    public static final WarConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final Pair<WarConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(WarConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    // WorldGen
    public final ForgeConfigSpec.DoubleValue posturePerProjectile;
    public final ForgeConfigSpec.DoubleValue defaultMultiplierPostureDefend;
    public final ForgeConfigSpec.DoubleValue defaultMultiplierPostureAttack;
    public final ForgeConfigSpec.IntValue rollThreshold;
    public final ForgeConfigSpec.IntValue rollCooldown;
    public final ForgeConfigSpec.IntValue shieldThreshold;
    public final ForgeConfigSpec.IntValue staggerDuration;
    public final ForgeConfigSpec.IntValue staggerHits;
    public final ForgeConfigSpec.IntValue mobUpdateInterval;
    public final ForgeConfigSpec.IntValue qiGrace;
    public final ForgeConfigSpec.IntValue comboGrace;
    public final ForgeConfigSpec.IntValue spiritCD;
    public final ForgeConfigSpec.IntValue postureCD;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> combatItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> customPosture;
    public final ForgeConfigSpec.DoubleValue mobParryChance;

    public WarConfig(ForgeConfigSpec.Builder b) {
        b.push("numbers");
        posturePerProjectile = b.translation("wardance.config.ppp").comment("Posture consumed per projectile parried").defineInRange("posturePerProjectile", 0.5, 0, Double.MAX_VALUE);
        defaultMultiplierPostureAttack = b.translation("wardance.config.dmpa").comment("Default multiplier for any items not defined in the config, multiplied by their attack damage").defineInRange("defaultPostureMultiplierAttack", 0.15, 0, Double.MAX_VALUE);
        defaultMultiplierPostureDefend = b.translation("wardance.config.dmpd").comment("Default multiplier for any item not defined in the config, when used for parrying").defineInRange("defaultPostureMultiplierDefend", 1.4, 0, Double.MAX_VALUE);
        rollThreshold = b.translation("wardance.config.rollT").comment("Within this number of ticks after rolling the entity is considered invulnerable").defineInRange("rollThreshold", 15, 0, Integer.MAX_VALUE);
        rollCooldown = b.translation("wardance.config.rollC").comment("Within this number of ticks after rolling the entity cannot roll again").defineInRange("rollCooldown", 20, 0, Integer.MAX_VALUE);
        shieldThreshold = b.translation("wardance.config.shieldT").comment("Within this number of ticks after a shield parry, parrying is free").defineInRange("shieldThreshold", 16, 0, Integer.MAX_VALUE);
        mobUpdateInterval = b.translation("wardance.config.mobU").comment("Mobs are forced to sync to client every this number of ticks").defineInRange("mobUpdateInterval", 100, 1, Integer.MAX_VALUE);
        qiGrace = b.translation("wardance.config.qiG").comment("Number of ticks after gaining qi during which it will not decrease").defineInRange("qiGrace", 100, 1, Integer.MAX_VALUE);
        comboGrace = b.translation("wardance.config.comboG").comment("Number of ticks after gaining combo during which it will not decrease").defineInRange("comboGrace", 100, 1, Integer.MAX_VALUE);
        spiritCD = b.translation("wardance.config.spiritC").comment("Number of ticks after consuming spirit during which it will not regenerate").defineInRange("spiritCD", 30, 1, Integer.MAX_VALUE);
        postureCD = b.translation("wardance.config.postureC").comment("Number of ticks after consuming posture during which it will not regenerate").defineInRange("postureCD", 30, 1, Integer.MAX_VALUE);
        staggerDuration = b.translation("wardance.config.staggerD").comment("Number of ticks an entity should be staggered for when its posture reaches 0").defineInRange("staggerDuration", 60, 1, Integer.MAX_VALUE);
        staggerHits = b.translation("wardance.config.staggerH").comment("Number of hits a staggered entity will take before stagger is automatically canceled").defineInRange("staggerHits", 3, 1, Integer.MAX_VALUE);
        mobParryChance = b.translation("wardance.config.mobP").comment("chance that a mob parries out of 1").defineInRange("mobParryChance", 0.6, 0, 1);
        b.pop();
        b.push("lists");
        combatItems = b.translation("wardance.config.combatItems").comment("Items eligible for parrying. Format should be name, attack posture consumption, defense multiplier, is shield").defineList("combatItems", Lists.newArrayList("example:sword, 3.5, 1.5, false", "example:shield, 0.3, 0.6, true"), String.class::isInstance);
        customPosture = b.translation("wardance.config.combatItems").comment("Here you can define custom max posture for mobs. Format is name, max posture. Armor is still calculated").defineList("customPosture", Lists.newArrayList("example:dragon, 100", "example:ghast, 8"), String.class::isInstance);
        b.pop();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.Loading e){
        if(e.getConfig().getSpec() == CONFIG_SPEC){
            CombatUtils.updateLists(CONFIG);
        }
    }
    @SubscribeEvent
    public static void reloadConfig(ModConfig.Reloading e){
        if(e.getConfig().getSpec() == CONFIG_SPEC){
            CombatUtils.updateLists(CONFIG);
        }
    }
}
