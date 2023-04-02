package jackiecrazy.wardance.client;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistry {

    @SubscribeEvent
    public static void keys(final RegisterKeyMappingsEvent event) {
        event.register(Keybinds.COMBAT);
        event.register(Keybinds.CAST);
        event.register(Keybinds.SELECT);
        event.register(Keybinds.BINDCAST);
        event.register(Keybinds.PARRY);
    }

}
