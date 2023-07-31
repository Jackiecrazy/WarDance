package jackiecrazy.wardance.items;

import jackiecrazy.wardance.WarDance;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WarItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WarDance.MODID);

    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", ScrollItem::new);
    public static final RegistryObject<Item> MANUAL = ITEMS.register("manual", ManualItem::new);

}
