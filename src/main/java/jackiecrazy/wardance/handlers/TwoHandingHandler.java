package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.TwohandingStats;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class TwoHandingHandler {

    private static final Map<Attribute, List<AttributeModifier>> none = new HashMap<>();

    @SubscribeEvent
    public static void twohanding(LivingEquipmentChangeEvent e) {
        final LivingEntity living = e.getEntity();
        if (e.getSlot() != EquipmentSlot.MAINHAND && e.getSlot() != EquipmentSlot.OFFHAND) return;
        ItemStack from, to;
        if (e.getSlot() == EquipmentSlot.MAINHAND) {
            //main hand change
            from = e.getFrom();
            to = e.getTo();
            getStats(from).forEach((k, v) -> v.forEach(am -> SkillUtils.removeAttribute(living, k, am)));
            if (isTwoHanding(living, to))
                getStats(to).forEach((k, v) -> v.forEach(am -> SkillUtils.addAttribute(living, k, am)));
        } else {
            //offhand change
            from = to = e.getTo();
            if (WeaponStats.isWeapon(living, to) || WeaponStats.isShield(living, to)) {
                //remove modifiers
                getStats(living.getMainHandItem()).forEach((k, v) -> v.forEach(am -> SkillUtils.removeAttribute(living, k, am)));
            } else {//apply modifiers
                getStats(living.getMainHandItem()).forEach((k, v) -> v.forEach(am -> SkillUtils.addAttribute(living, k, am)));
            }
        }
    }

    public static void updateTwoHanding(LivingEntity e, ItemStack main){
        if(isTwoHanding(e, main)){
            getStats(main).forEach((k, v) -> v.forEach(am -> SkillUtils.addAttribute(e, k, am)));
        }else{
            getStats(main).forEach((k, v) -> v.forEach(am -> SkillUtils.removeAttribute(e, k, am)));
        }
    }

    public static boolean isTwoHanding(LivingEntity living, ItemStack i) {
        return i.is(WeaponStats.TWO_HANDED) || CombatData.getCap(living).getHandBind(InteractionHand.OFF_HAND) > 0 || (!WeaponStats.isWeapon(living, living.getOffhandItem()) && !WeaponStats.isShield(living, living.getOffhandItem()));
    }

    public static Map<Attribute, List<AttributeModifier>> getStats(ItemStack i) {
        if (TwohandingStats.MAP.containsKey(i.getItem())) {
            return TwohandingStats.MAP.get(i.getItem());
        } else {
            for (Map.Entry<TagKey<Item>, Map<Attribute, List<AttributeModifier>>> entry : TwohandingStats.ARCHETYPES.entrySet()) {
                if (i.is(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return none;
    }
}
