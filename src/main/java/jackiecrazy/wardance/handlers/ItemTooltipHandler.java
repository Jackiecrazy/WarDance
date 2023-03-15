package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent()
    public static void tooltip(ItemTooltipEvent e) {
        if (CombatUtils.isWeapon(e.getEntity(), e.getItemStack()) || CombatUtils.isShield(e.getEntity(), e.getItemStack())) {
            float atk = CombatUtils.getPostureAtk(null, null, null, 0, e.getItemStack());
            if(atk<0){
                e.getToolTip().add(Component.translatable("wardance.tooltip.ignoreParry").withStyle(ChatFormatting.DARK_GREEN));
                atk=-atk;
            }
            e.getToolTip().add(Component.translatable("wardance.tooltip.postureAttack", atk));
            final float def = CombatUtils.getPostureDef(null, null, e.getItemStack(), 0);
            if(def<0)
                e.getToolTip().add(Component.translatable("wardance.tooltip.noParry").withStyle(ChatFormatting.DARK_RED));
            else
                e.getToolTip().add(Component.translatable("wardance.tooltip.postureDefend", def));
        }
        if(CombatUtils.isUnarmed(e.getItemStack(), e.getEntity())){
            e.getToolTip().add(Component.translatable("wardance.tooltip.unarmed"));
        }
    }
}
