package jackiecrazy.wardance.client;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.DummyItem;
import jackiecrazy.wardance.items.WarItems;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistry {

    @SubscribeEvent
    public static void keys(final RegisterKeyMappingsEvent event) {
        event.register(Keybinds.COMBAT);
        event.register(Keybinds.CAST);
        event.register(Keybinds.BINDCAST);
        event.register(Keybinds.PARRY);
        for(KeyMapping km: Keybinds.SKILL){
            event.register(km);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerSpawnEggColors(RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, layer) -> DummyItem.getColor(stack).getRGB(), WarItems.DUMMY.get());
    }

}
