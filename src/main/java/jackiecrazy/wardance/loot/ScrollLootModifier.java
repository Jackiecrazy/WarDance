package jackiecrazy.wardance.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import jackiecrazy.wardance.config.LootConfig;
import jackiecrazy.wardance.items.ScrollItem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * credits to Shadows-of-Fire
 */
public class ScrollLootModifier extends LootModifier {

    public static final Codec<ScrollLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, ScrollLootModifier::new));

    protected ScrollLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (LootConfig.scrollChances.stream().anyMatch(a -> a.matches(context.getQueriedLootTableId()) && context.getRandom().nextFloat() <= a.chance())) {
            ItemStack scroll = ScrollItem.makeScroll(false, (Skill) null);
            ScrollItem.setRandom(scroll, false);
            ScrollItem.setRandomSize(scroll, 4);
            ScrollItem.setStyle(scroll, false);
            generatedLoot.add(scroll);
        }
        if (LootConfig.styleChances.stream().anyMatch(a -> a.matches(context.getQueriedLootTableId()) && context.getRandom().nextFloat() <= a.chance())) {
            ItemStack scroll = ScrollItem.makeScroll(false, (Skill) null);
            ScrollItem.setRandom(scroll, false);
            ScrollItem.setRandomSize(scroll, 3);
            ScrollItem.setStyle(scroll, true);
            generatedLoot.add(scroll);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
