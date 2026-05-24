package jackiecrazy.wardance;

import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WarContainers {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WarDance.MODID);

    public static final RegistryObject<MenuType<QuiverMenu>> QUIVER_MENU = MENUS.register("quiver_menu",
                                                                                          () -> IForgeMenuType.create(QuiverMenu::new));
}
