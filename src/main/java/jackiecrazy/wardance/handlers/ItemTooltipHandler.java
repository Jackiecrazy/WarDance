package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent()
    public static void tooltip(ItemTooltipEvent e) {
        final ItemStack stack = e.getItemStack();
        if (CombatUtils.isWeapon(e.getEntity(), stack) || CombatUtils.isShield(e.getEntity(), stack)) {
            float atk = CombatUtils.getPostureAtk(null, null, null, 0, stack);
            if(stack.is(CombatUtils.PIERCE_PARRY)){
                e.getToolTip().add(Component.translatable("wardance.tooltip.ignoreParry").withStyle(ChatFormatting.GREEN));
            }
            if(stack.is(CombatUtils.PIERCE_SHIELD)){
                e.getToolTip().add(Component.translatable("wardance.tooltip.ignoreShield").withStyle(ChatFormatting.DARK_GREEN));
            }
            e.getToolTip().add(Component.translatable("wardance.tooltip.postureAttack", atk));
            final float def = CombatUtils.getPostureDef(null, null, stack, 0);
            if(stack.is(CombatUtils.CANNOT_PARRY))
                e.getToolTip().add(Component.translatable("wardance.tooltip.noParry").withStyle(ChatFormatting.DARK_RED));
            else
                e.getToolTip().add(Component.translatable("wardance.tooltip.postureDefend", def));
        }
        if(CombatUtils.isUnarmed(stack, e.getEntity())){
            e.getToolTip().add(Component.translatable("wardance.tooltip.unarmed").withStyle(ChatFormatting.GOLD));
        }
        if(CombatUtils.isShield(e.getEntity(), stack)){
            e.getToolTip().add(Component.translatable("wardance.tooltip.shield").withStyle(ChatFormatting.GOLD));
        }
        if(CombatUtils.isTwoHanded(stack, e.getEntity())){
            e.getToolTip().add(Component.translatable("wardance.tooltip.twoHanded").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
