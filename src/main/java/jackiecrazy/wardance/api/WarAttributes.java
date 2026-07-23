package jackiecrazy.wardance.api;

import jackiecrazy.wardance.WarDance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class WarAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, WarDance.MODID);

    //resource attributes
    public static final RegistryObject<Attribute> MAX_POSTURE = register("max_posture", 0d, 0, 1024);
    public static final RegistryObject<Attribute> SPIRIT_REFUND = register("spirit_refund", 0d, 0, 1);
    public static final RegistryObject<Attribute> MAX_RALLY = register("max_rally", 0.3d, 0, 1);
    public static final RegistryObject<Attribute> RALLY_REGEN = register("rally_regen", 1d, 0, 1024);
    public static final RegistryObject<Attribute> RALLY_GUARD = register("rally_regen_guard", 1, 0, 1024);
    public static final RegistryObject<Attribute> RALLY_DMG = register("internal_damage_rally", 0.15, 0, 1);
    public static final RegistryObject<Attribute> ADRE_BON = register("adrenaline_gain", 1, 0, 1024);

    //defense attributes
    public static final RegistryObject<Attribute> DARKTIDE = register("darktide_power", 1d, 0, 1024);
    public static final RegistryObject<Attribute> DDOOR_TIME = register("deaths_door_time", 1d, 0, 1024);
    public static final RegistryObject<Attribute> DODGE_EXTEND = register("dodge_window", 1d, 0, 1024);
    public static final RegistryObject<Attribute> PARRY_EXTEND = register("parry_window", 1d, 0, 1024);
    public static final RegistryObject<Attribute> BLOCK_EXTEND = register("block_window", 1d, 0, 1024);
    public static final RegistryObject<Attribute> IFRAME_EXTEND = register("iframe_window", 1d, 0, 1024);

    //mobility attributes
    public static final RegistryObject<Attribute> AIR_GRAVITY = register("aerial_gravity", 1d, 0, 10);
    public static final RegistryObject<Attribute> AIR_JUMPS = register("air_jumps", 1d, 0, 10);
    //public static final RegistryObject<Attribute> DODGE_EFFICIENCY = register("dodge_efficiency", 1d, 0, 2);
    public static final RegistryObject<Attribute> KNOCK_TIME = register("knockdown_time", 1d, 0, 1024);

    //offensive attributes
    public static final RegistryObject<Attribute> SKILL_EFFECTIVENESS = register("skill_effectiveness", 1d, 0, 100);
    public static final RegistryObject<Attribute> TWO_HANDING = register("two_handing", 0d, -1, 4);
    public static final RegistryObject<Attribute> KICK_DAMAGE = register("kick_damage", 1d, 0, 1024);
    public static final RegistryObject<Attribute> CRIT_DAMAGE = register("crit_damage", 1d, 0, 1024);
    public static final RegistryObject<Attribute> COUNTERSTRIKE = register("counterstrike_power", 1.15d, 0, 1024);
    public static final RegistryObject<Attribute> PARRY = register("parry_power", 1d, 0, 1024);

    public static RegistryObject<Attribute> register(String name, double defaultt, double min, double max){
        return ATTRIBUTES.register(name, () -> new RangedAttribute(WarDance.MODID + "."+name, defaultt, min, max).setSyncable(true));
    }
}
