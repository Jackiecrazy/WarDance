package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.config.WeaponStats;
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
import java.util.Locale;
import java.util.Map;

import static jackiecrazy.wardance.client.RenderUtils.formatter;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(modid = WarDance.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent()
    public static void tooltip(ItemTooltipEvent e) {
        final ItemStack stack = e.getItemStack();
        if (WeaponStats.isWeapon(e.getEntity(), stack) || WeaponStats.isShield(e.getEntity(), stack)) {
            if (Screen.hasShiftDown()) {
                if (PermissionData.getCap(e.getEntity()).canDealPostureDamage()) {
                    float atk = CombatUtils.getPostureAtk(null, null, null, 0, stack);
                    e.getToolTip().add(Component.translatable("wardance.tooltip.postureAttack", Component.literal(formatter.format(atk)).withStyle(ChatFormatting.RED)));
                }
                final float def = CombatUtils.getPostureDef(null, null, stack, 0);
                if (PermissionData.getCap(e.getEntity()).canParry()) {
                    if (stack.is(WeaponStats.CANNOT_PARRY))
                        e.getToolTip().add(Component.translatable("wardance.tooltip.noParry").withStyle(ChatFormatting.DARK_RED));
                    else
                        e.getToolTip().add(Component.translatable("wardance.tooltip.postureDefend", Component.literal(formatter.format(def)).withStyle(ChatFormatting.DARK_GREEN)));
                }
                if (PermissionData.getCap(e.getEntity()).canSweep()) {
                    for (WeaponStats.SWEEPSTATE s : WeaponStats.SWEEPSTATE.values())
                        if (s == WeaponStats.SWEEPSTATE.STANDING || !WeaponStats.getSweepInfo(stack, s).equals(WeaponStats.getSweepInfo(stack, WeaponStats.SWEEPSTATE.STANDING))) {
                            final Component toolTip = WeaponStats.getSweepInfo(e.getItemStack(), s).getToolTip(e.getItemStack(), e.getFlags().isAdvanced());
                            e.getToolTip().add(Component.translatable("wardance.tooltip.sweep." + s.name().toLowerCase(Locale.ROOT), toolTip).withStyle(ChatFormatting.DARK_AQUA));
                        }
                }
            } else {
                e.getToolTip().add(Component.translatable("wardance.tooltip.shift").withStyle(ChatFormatting.GREEN));
            }

            List<Component> tips = new ArrayList<>();

            if (stack.canDisableShield(stack, e.getEntity(), e.getEntity())) {
                tips.add(Component.translatable("wardance.tooltip.disableShield").withStyle(ChatFormatting.GREEN));
            }
            if (stack.is(WeaponStats.PIERCE_PARRY)) {
                tips.add(Component.translatable("wardance.tooltip.ignoreParry").withStyle(ChatFormatting.GREEN));
            }
            if (stack.is(WeaponStats.PIERCE_SHIELD)) {
                tips.add(Component.translatable("wardance.tooltip.ignoreShield").withStyle(ChatFormatting.GREEN));
            }
            if (WeaponStats.isUnarmed(stack, e.getEntity())) {
                tips.add(Component.translatable("wardance.tooltip.unarmed").withStyle(ChatFormatting.GOLD));
            }
            if (WeaponStats.isShield(e.getEntity(), stack)) {
                tips.add(Component.translatable("wardance.tooltip.shield").withStyle(ChatFormatting.GOLD));
            }
            if (WeaponStats.isTwoHanded(stack, e.getEntity())) {
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
