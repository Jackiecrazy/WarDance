package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent()
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
