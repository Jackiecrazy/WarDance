package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.TwohandingStats;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.event.SuppressOffhandEvent;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class TwoHandingHandler {

    public static final Function<Tuple<AttributeModifier, AttributeModifier>, AttributeModifier> MAIN = Tuple::getA;
    public static final Function<Tuple<AttributeModifier, AttributeModifier>, AttributeModifier> OFF = Tuple::getB;
    private static final Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>> none = new HashMap<>();

    @SubscribeEvent
    public static void twohanding(LivingEquipmentChangeEvent e) {
        if (CombatUtils.suppress) return;//don't handle on offhand swap attack stuff
        final LivingEntity living = e.getEntity();
        if (e.getSlot() != EquipmentSlot.MAINHAND && e.getSlot() != EquipmentSlot.OFFHAND) return;
        updateTwoHanding(living, e.getFrom(), e.getTo());
    }

    public static void updateTwoHanding(LivingEntity e, ItemStack fromStack, ItemStack toStack) {
        for (InteractionHand hand : InteractionHand.values()) {
            getStats(fromStack, hand).forEach((k, v) -> (hand==InteractionHand.MAIN_HAND?v.getA():v.getB()).forEach(am -> SkillUtils.removeAttribute(e, k, am)));
            if (WeaponStats.twoHandBonus(e, hand)) {
                getStats(toStack, hand).forEach((k, v) -> (hand==InteractionHand.MAIN_HAND?v.getA():v.getB()).forEach(am -> SkillUtils.addAttribute(e, k, am)));
            }
        }
    }

    public static boolean suppressOffhand(LivingEntity living, ItemStack i) {
        boolean def = WeaponStats.isTwoHanded(i, living, InteractionHand.MAIN_HAND);
        SuppressOffhandEvent the = new SuppressOffhandEvent(living, i);
        MinecraftForge.EVENT_BUS.post(the);
        if (the.getResult() == Event.Result.DEFAULT)
            return def;
        return the.getResult() == Event.Result.ALLOW;
    }

    public static Map<Attribute, Tuple<List<AttributeModifier>,List<AttributeModifier>>> getStats(ItemStack i, InteractionHand h) {
        if (TwohandingStats.MAP.containsKey(i.getItem())) {
            return TwohandingStats.MAP.get(i.getItem());
        } else {
            for (Map.Entry<TagKey<Item>, Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> entry : TwohandingStats.ARCHETYPES.entrySet()) {
                if (i.is(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return none;
    }

    private static Map<Attribute, List<AttributeModifier>> readHand(Tuple<Map<Attribute, List<AttributeModifier>>, Map<Attribute, List<AttributeModifier>>> map, InteractionHand h) {
        return h == InteractionHand.MAIN_HAND ? map.getA() : map.getB();
    }

    @SubscribeEvent
    public static void no(PlayerInteractEvent.RightClickItem e) {
        if (e.getHand() == InteractionHand.OFF_HAND && WeaponStats.isTwoHanded(e.getEntity().getMainHandItem(), e.getEntity(), InteractionHand.MAIN_HAND)) {
            e.setResult(Event.Result.DENY);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void no(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() == InteractionHand.OFF_HAND && WeaponStats.isTwoHanded(e.getEntity().getMainHandItem(), e.getEntity(), InteractionHand.MAIN_HAND)) {
            e.setResult(Event.Result.DENY);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void no(PlayerInteractEvent.EntityInteract e) {
        if (e.getHand() == InteractionHand.OFF_HAND && WeaponStats.isTwoHanded(e.getEntity().getMainHandItem(), e.getEntity(), InteractionHand.MAIN_HAND)) {
            e.setResult(Event.Result.DENY);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void no(PlayerInteractEvent.EntityInteractSpecific e) {
        if (e.getHand() == InteractionHand.OFF_HAND && WeaponStats.isTwoHanded(e.getEntity().getMainHandItem(), e.getEntity(), InteractionHand.MAIN_HAND)) {
            e.setResult(Event.Result.DENY);
            e.setCanceled(true);
        }
    }
}
