package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.permission.PermissionData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
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
        final Player entity = e.getEntity();
        if (entity == null || !StylishData.getCap(entity).isCombatMode()) return;
        if (WeaponStats.isWeapon(entity, stack) || WeaponStats.isShield(entity, stack)) {
            if (Screen.hasShiftDown()) {
                if (PermissionData.getCap(entity).canDealPostureDamage()) {
                    float atk = CombatUtils.getPostureAtk(null, null, null, null, 0, stack);
                    e.getToolTip().add(Component.translatable("wardance.tooltip.postureAttack", Component.literal(formatter.format(atk)).withStyle(ChatFormatting.RED)));
                }
                final float def = CombatUtils.getRallyPercentage(null, null, stack, 0);
                if (PermissionData.getCap(entity).canParry()) {
                    if (stack.is(WeaponStats.CANNOT_BLOCK))
                        e.getToolTip().add(Component.translatable("wardance.tooltip.noBlock").withStyle(ChatFormatting.DARK_RED));
                    else
                        e.getToolTip().add(Component.translatable("wardance.tooltip.postureDefend", Component.literal(formatter.format(def*100)+"%").withStyle(ChatFormatting.DARK_GREEN)));
                }
                if (PermissionData.getCap(entity).canSweep()) {
                    //final Component standing = WeaponStats.getSweepInfo(stack, entity, WeaponStats.AttackType.STANDING, false).getToolTip(e.getItemStack(), e.getFlags().isAdvanced());
                    for (WeaponStats.AttackType s : WeaponStats.AttackType.values()) {
                        if (s == WeaponStats.AttackType.UNDEFINED) continue;
                        final Component sweepGroup = WeaponStats.getSweepInfo(stack, entity, s, true, null).getDescription(e.getItemStack(), e.getFlags().isAdvanced());
                        if(sweepGroup==null) e.getToolTip().add(Component.literal("no sweep!?"));
//                        if (s == WeaponStats.AttackType.STANDING || !sweepGroup.equals(standing))
                        else e.getToolTip().add(Component.translatable("wardance.tooltip.sweep." + s.name().toLowerCase(Locale.ROOT), sweepGroup.copy().withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                    }
                }
            } else {
                e.getToolTip().add(Component.translatable("wardance.tooltip.shift").withStyle(ChatFormatting.GREEN));
            }

            List<Component> tips = new ArrayList<>();

//            if (stack.canDisableShield(stack, e.getEntity(), e.getEntity())) {
//                tips.add(Component.translatable("wardance.tooltip.disableShield").withStyle(ChatFormatting.GREEN));
//            }
            if (stack.is(WeaponStats.PIERCE_SHIELD)) {
                tips.add(Component.translatable("wardance.tooltip.ignoreShield").withStyle(ChatFormatting.GREEN));
            }
            if (WeaponStats.isUnarmed(stack, entity)) {
                tips.add(Component.translatable("wardance.tooltip.unarmed").withStyle(ChatFormatting.GOLD));
            }
            if (WeaponStats.isShield(entity, stack)) {
                tips.add(Component.translatable("wardance.tooltip.shield").withStyle(ChatFormatting.GOLD));
            }
            if (WeaponStats.isTwoHanded(stack, entity, null)) {
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
            final Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>> stats = TwoHandingHandler.getStats(e.getItemStack(), InteractionHand.MAIN_HAND);
            if (!stats.isEmpty()) {
                if (Screen.hasControlDown()) {
                    tips.add(Component.translatable("wardance.tooltip.twohanding").withStyle(ChatFormatting.GOLD));
                    stats.forEach((attr, lis) -> lis.getA().forEach(am -> {
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
