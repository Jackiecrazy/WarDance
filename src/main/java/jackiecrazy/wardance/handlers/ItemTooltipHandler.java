package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.StealthUtils;
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
        if (CombatUtils.isWeapon(e.getEntityLiving(), e.getItemStack()) || CombatUtils.isShield(e.getEntityLiving(), e.getItemStack())) {
            float atk = CombatUtils.getPostureAtk(null, null, null, 0, e.getItemStack());
            if(atk<0){
                e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.ignoreParry"));
                atk=-atk;
            }
            e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.postureAttack", atk));
            final float def = CombatUtils.getPostureDef(null, null, e.getItemStack(), 0);
            if(def<0)
                e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.noParry"));
            else
                e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.postureDefend", def));
            if (CombatUtils.isShield(null, e.getItemStack())) {
                Tuple<Integer, Integer> rerorero = CombatUtils.getShieldStats(e.getItemStack());
                e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.parry", rerorero.getB() + 1, rerorero.getA() / 20f));
            } else if (CombatUtils.isWeapon(null, e.getItemStack())) {
                e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.attack", CombatUtils.getDamageMultiplier(StealthUtils.Awareness.DISTRACTED, e.getItemStack()) + "x", CombatUtils.getDamageMultiplier(StealthUtils.Awareness.UNAWARE, e.getItemStack()) + "x"));
            }
        }
        if(CombatUtils.isUnarmed(e.getItemStack(), e.getEntityLiving())){
            e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.unarmed"));
        }
    }
}
