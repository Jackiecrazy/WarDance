package jackiecrazy.wardance.client;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.DummyItem;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.stream.Collectors;

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

    @SubscribeEvent
    public static void skillRender(ModelEvent.RegisterAdditional e){
        final Set<Skill> set = WarSkills.SKILLS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet());
        for (Skill s : set)
            e.register(new ResourceLocation(WarDance.MODID, "skill/"+s.getRegistryName().getPath()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerSpawnEggColors(RegisterColorHandlersEvent.Item event)
    {
        event.getItemColors().register((stack, layer) -> DummyItem.getColor(stack).getRGB(), WarItems.DUMMY.get());
    }

}
