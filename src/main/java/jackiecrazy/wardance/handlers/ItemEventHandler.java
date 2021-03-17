package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class ItemEventHandler {
    @SubscribeEvent
    public static void items(ItemAttributeModifierEvent e) {
        if (CombatUtils.armorStats.containsKey(e.getItemStack().getItem()) && (!e.getOriginalModifiers().isEmpty() || (!(e.getItemStack().getItem() instanceof ArmorItem) && e.getSlotType() == EquipmentSlotType.OFFHAND))) {//presumably this is the correct equipment slot
            AttributeModifier[] a = CombatUtils.armorStats.get(e.getItemStack().getItem());
            e.addModifier(WarAttributes.ABSORPTION.get(), a[0]);
            e.addModifier(WarAttributes.DEFLECTION.get(), a[1]);
            e.addModifier(WarAttributes.SHATTER.get(), a[2]);
        }
        if(e.getItemStack().getItem() == Items.WOODEN_SWORD){
            e.addModifier(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier("test", 30, AttributeModifier.Operation.ADDITION));
        }
    }

}
