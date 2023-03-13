package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.api.WarAttributes;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class ItemEventHandler {

    @SubscribeEvent
    public static void items(ItemAttributeModifierEvent e) {
        if (CombatUtils.armorStats.containsKey(e.getItemStack().getItem()) && (!e.getOriginalModifiers().isEmpty() || (!(e.getItemStack().getItem() instanceof ArmorItem) && e.getSlotType() == EquipmentSlot.OFFHAND))) {//presumably this is the correct equipment slot
            AttributeModifier[] a = CombatUtils.armorStats.get(e.getItemStack().getItem());
            e.addModifier(WarAttributes.ABSORPTION.get(), a[0]);
            e.addModifier(WarAttributes.DEFLECTION.get(), a[1]);
            e.addModifier(WarAttributes.SHATTER.get(), a[2]);
            e.addModifier(WarAttributes.STEALTH.get(), a[3]);
        }
        if (CombatUtils.shieldStat.containsKey(e.getItemStack().getItem()) && (e.getSlotType() == EquipmentSlot.OFFHAND || e.getSlotType() == EquipmentSlot.MAINHAND)) {
            int shift=e.getSlotType()==EquipmentSlot.OFFHAND?2:0;
            e.addModifier(WarAttributes.BARRIER_COOLDOWN.get(), CombatUtils.shieldStat.get(e.getItemStack().getItem())[shift]);
            e.addModifier(WarAttributes.BARRIER.get(), CombatUtils.shieldStat.get(e.getItemStack().getItem())[1+shift]);
        }
    }

}
