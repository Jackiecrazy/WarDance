package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(modid = WarDance.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent()
    public static void tooltip(ItemTooltipEvent e) {
        final ItemStack stack = e.getItemStack();
        if (CombatUtils.isWeapon(e.getEntity(), stack) || CombatUtils.isShield(e.getEntity(), stack)) {
            if (Screen.hasShiftDown()) {
                if (PermissionData.getCap(e.getEntity()).canDealPostureDamage()) {
                    float atk = CombatUtils.getPostureAtk(null, null, null, 0, stack);
                    e.getToolTip().add(Component.translatable("wardance.tooltip.postureAttack", atk).withStyle(ChatFormatting.RED));
                }
                final float def = CombatUtils.getPostureDef(null, null, stack, 0);
                if (PermissionData.getCap(e.getEntity()).canParry()) {
                    if (stack.is(CombatUtils.CANNOT_PARRY))
                        e.getToolTip().add(Component.translatable("wardance.tooltip.noParry").withStyle(ChatFormatting.DARK_RED));
                    else
                        e.getToolTip().add(Component.translatable("wardance.tooltip.postureDefend", def).withStyle(ChatFormatting.DARK_GREEN));
                }
                if (PermissionData.getCap(e.getEntity()).canSweep()) {
                    e.getToolTip().add(Component.translatable("wardance.tooltip.sweep." + CombatUtils.getSweepType(e.getEntity(), e.getItemStack()), CombatUtils.getSweepBase(e.getItemStack()), CombatUtils.getSweepScale(e.getItemStack())).withStyle(ChatFormatting.AQUA));
                }
            } else {
                e.getToolTip().add(Component.translatable("wardance.tooltip.shift").withStyle(ChatFormatting.GREEN));
            }

            List<Component> tips = new ArrayList<>();

            if (stack.is(CombatUtils.PIERCE_PARRY)) {
                tips.add(Component.translatable("wardance.tooltip.ignoreParry").withStyle(ChatFormatting.GREEN));
            }
            if (stack.is(CombatUtils.PIERCE_SHIELD)) {
                tips.add(Component.translatable("wardance.tooltip.ignoreShield").withStyle(ChatFormatting.DARK_GREEN));
            }
            if (CombatUtils.isUnarmed(stack, e.getEntity())) {
                tips.add(Component.translatable("wardance.tooltip.unarmed").withStyle(ChatFormatting.GOLD));
            }
            if (CombatUtils.isShield(e.getEntity(), stack)) {
                tips.add(Component.translatable("wardance.tooltip.shield").withStyle(ChatFormatting.GOLD));
            }
            if (CombatUtils.isTwoHanded(stack, e.getEntity())) {
                tips.add(Component.translatable("wardance.tooltip.twoHanded").withStyle(ChatFormatting.DARK_RED));
            }
            boolean hasBuffs = !tips.isEmpty();
            if (hasBuffs) {
                if (Screen.hasAltDown()) {
                    e.getToolTip().addAll(tips);
                } else {
                    e.getToolTip().add(Component.translatable("wardance.tooltip.alt").withStyle(ChatFormatting.BLUE));
                }
            }
            tips.clear();
            final Map<Attribute, List<AttributeModifier>> stats = TwoHandingHandler.getStats(e.getItemStack());
            if (!stats.isEmpty()) {
                if (Screen.hasControlDown()) {
                    tips.add(Component.translatable("wardance.tooltip.twohanding").withStyle(ChatFormatting.GOLD));
                    stats.forEach((attr, lis) -> lis.forEach(am -> {
                        double amount = am.getAmount();
                        switch (am.getOperation()) {
                            case MULTIPLY_TOTAL, MULTIPLY_BASE -> amount *= 100;
                        }
                        if (amount > 0.0D) {
                            tips.add((Component.translatable(
                                    "wardance.twohanding.modifier.positive." +
                                            am.getOperation().toValue(),
                                    ATTRIBUTE_MODIFIER_FORMAT.format(amount),
                                    Component.translatable(attr.getDescriptionId())))
                                    .withStyle(ChatFormatting.BLUE));
                        } else {
                            ;
                            tips.add((Component.translatable(
                                    "wardance.twohanding.modifier.negative." +
                                            am.getOperation().toValue(),
                                    ATTRIBUTE_MODIFIER_FORMAT.format(-amount),
                                    Component.translatable(attr.getDescriptionId())))
                                    .withStyle(ChatFormatting.RED));
                        }
                    }));
                    e.getToolTip().addAll(tips);
                } else {
                    e.getToolTip().add(Component.translatable("wardance.tooltip.ctrl").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

    }
}
