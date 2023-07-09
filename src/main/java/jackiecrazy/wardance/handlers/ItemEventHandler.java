package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class ItemEventHandler {

    @SubscribeEvent
    public static void items(ItemAttributeModifierEvent e) {
    }

}
