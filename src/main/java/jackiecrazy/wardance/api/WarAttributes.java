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
}
