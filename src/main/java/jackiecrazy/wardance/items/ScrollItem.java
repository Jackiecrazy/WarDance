package jackiecrazy.wardance.items;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ScrollItem extends Item {
    private static final Item.Properties prop = new Properties().rarity(Rarity.UNCOMMON).stacksTo(1).tab(CreativeModeTab.TAB_COMBAT);

    public ScrollItem() {
        super(prop);
    }

    @Nullable
    public static Skill getSkill(ItemStack stack) {
        return Skill.getSkill(stack.getOrCreateTag().getString("skill"));
    }

    public static void setSkill(ItemStack stack, Skill s) {
        stack.getOrCreateTag().putString("skill", s.getRegistryName().toString());
    }

    public static void setSkills(ItemStack stack, Skill... list) {
        CompoundTag tag = new CompoundTag();
        for (Skill s : list)
            tag.putBoolean(s.getRegistryName().toString(), true);
        stack.getOrCreateTag().put("skills", tag);
    }

    public static ItemStack makeScroll(Skill s) {
        ItemStack itemstack = new ItemStack(WarItems.SCROLL.get());
        setSkill(itemstack, s);
        return itemstack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level leve, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        Skill skill = getSkill(stack);
        //TODO open up a screen that gives 3 choices if not a fixed scroll. Allow determining which 3 via nbt (skill/style discrimination)
        if (skill == null) {
            List<Skill> set = WarSkills.SUPPLIER.get().getValues().stream().filter(a -> !CasterData.getCap(p).isSkillSelectable(a)).toList();
            if (!set.isEmpty()) {
                skill = set.get(WarDance.rand.nextInt(set.size()));
                setSkill(stack, skill);
            }
        }
        if (CasterData.getCap(p).isSkillSelectable(skill)) {
            p.displayClientMessage(Component.translatable("wardance.scroll.learned").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        CasterData.getCap(p).setSkillSelectable(skill, true);
        p.displayClientMessage(Component.translatable("wardance.scroll.learn", skill.getDisplayName(p)), true);
        if (!p.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level p_41422_, List<Component> component, TooltipFlag flag) {
        if (getSkill(stack) != null)
            component.add(Component.translatable("wardance.scroll.skill", getSkill(stack).getDisplayName(null)).withStyle(ChatFormatting.GOLD));
        else
            component.add(Component.translatable("wardance.scroll.random").withStyle(ChatFormatting.AQUA));
    }

    public void fillItemCategory(CreativeModeTab p_41151_, NonNullList<ItemStack> p_41152_) {
        if(p_41151_==CreativeModeTab.TAB_COMBAT) {
            p_41152_.add(new ItemStack(WarItems.SCROLL.get()));
            WarSkills.SUPPLIER.get().getValues().forEach((a) -> p_41152_.add(makeScroll(a)));
        }
    }
}
