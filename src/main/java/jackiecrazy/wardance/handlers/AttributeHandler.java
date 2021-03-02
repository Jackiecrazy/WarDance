package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeHandler {

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void attribute(EntityAttributeModificationEvent e) {
        for (EntityType<?> t : e.getTypes()) {
            e.add((EntityType<? extends LivingEntity>) t, Attributes.ATTACK_SPEED);
            e.add((EntityType<? extends LivingEntity>) t, WarAttributes.ABSORPTION.get());
            e.add((EntityType<? extends LivingEntity>) t, WarAttributes.DEFLECTION.get());
            e.add((EntityType<? extends LivingEntity>) t, WarAttributes.SHATTER.get());
        }
    }
}
