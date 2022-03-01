package jackiecrazy.wardance.api;

import jackiecrazy.wardance.WarDance;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.UUID;

public class WarAttributes {
    public static final UUID[] MODIFIERS = {
            UUID.fromString("a516026a-bee2-4014-bcb6-b6a5775553da"),
            UUID.fromString("a516026a-bee2-4014-bcb6-b6a5775553db"),
            UUID.fromString("a516026a-bee2-4014-bcb6-b6a5775553dc"),
            UUID.fromString("a516026a-bee2-4014-bcb6-b6a5775553dd")
    };
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister
            .create(Attribute.class, WarDance.MODID);

    public static final RegistryObject<Attribute> ABSORPTION = ATTRIBUTES.register("absorption", () -> new RangedAttribute(WarDance.MODID + ".absorption", 0d, 0, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> DEFLECTION = ATTRIBUTES.register("deflection", () -> new RangedAttribute(WarDance.MODID + ".deflection", 0d, 0, 60).setShouldWatch(true));
    public static final RegistryObject<Attribute> SHATTER = ATTRIBUTES.register("shatter", () -> new RangedAttribute(WarDance.MODID + ".shatter", 0d, 0, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> STEALTH = ATTRIBUTES.register("stealth", () -> new RangedAttribute(WarDance.MODID + ".stealth", 20d, 0, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> MAX_POSTURE = ATTRIBUTES.register("max_posture", () -> new RangedAttribute(WarDance.MODID + ".maxPosture", 0d, 0, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> MAX_SPIRIT = ATTRIBUTES.register("max_spirit", () -> new RangedAttribute(WarDance.MODID + ".maxSpirit", 10d, 0, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> MAX_MIGHT = ATTRIBUTES.register("max_might", () -> new RangedAttribute(WarDance.MODID + ".maxMight", 10d, 0, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> POSTURE_REGEN = ATTRIBUTES.register("posture_regen", () -> new RangedAttribute(WarDance.MODID + ".postureGen", 0d, -Double.MAX_VALUE, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> SPIRIT_REGEN = ATTRIBUTES.register("spirit_regen", () -> new RangedAttribute(WarDance.MODID + ".spiritGen", 10d, -Double.MAX_VALUE, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> MIGHT_GEN = ATTRIBUTES.register("might_gen", () -> new RangedAttribute(WarDance.MODID + ".mightGen", 10d, -Double.MAX_VALUE, Double.MAX_VALUE).setShouldWatch(true));
    public static final RegistryObject<Attribute> BARRIER = ATTRIBUTES.register("barrier", () -> new RangedAttribute(WarDance.MODID + ".barrier", 0.0d, 0, 1).setShouldWatch(true));
    public static final RegistryObject<Attribute> BARRIER_COOLDOWN = ATTRIBUTES.register("barrier_cooldown", () -> new RangedAttribute(WarDance.MODID + ".barrierCooldown", 0d, 0, Integer.MAX_VALUE).setShouldWatch(true));
}
