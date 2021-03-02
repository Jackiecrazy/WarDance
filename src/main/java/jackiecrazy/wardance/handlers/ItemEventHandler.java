package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent e) {
        if (CombatUtils.isWeapon(null, e.getItemStack()) || CombatUtils.isShield(null, e.getItemStack())) {
            e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.postureAttack", CombatUtils.getPostureAtk(null, null, 0, e.getItemStack())));
            e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.postureDefend", CombatUtils.getPostureDef(null, e.getItemStack())));
            if (CombatUtils.isShield(null, e.getItemStack())) {
                Tuple<Integer, Integer> rerorero = CombatUtils.getShieldStats(e.getItemStack());
                e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.parry", rerorero.getB(), rerorero.getA() / 20f));
            }
        }
    }
}
