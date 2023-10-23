package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.ManualItem;
import jackiecrazy.wardance.items.ScrollItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TabHandler {
    @SubscribeEvent()
    public static void tooltip(BuildCreativeModeTabContentsEvent e) {
        if(e.getTab() == WarDance.WARTAB.get()){
            ScrollItem.fillItemCategory(e.getTab()).forEach(e::accept);
            ManualItem.fillItemCategory(e.getTab()).forEach(e::accept);
        }
    }
}
