package jackiecrazy.wardance.advancement;

import jackiecrazy.wardance.WarDance;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WarAdvancements {
    public static final SkillCastTrigger SKILL_CAST_TRIGGER = new SkillCastTrigger();

    @SubscribeEvent
    public static void advance(FMLCommonSetupEvent e) {
        //criteria
        e.enqueueWork(() -> CriteriaTriggers.register(SKILL_CAST_TRIGGER));
    }
}
