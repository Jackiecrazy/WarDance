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

import javax.annotation.Nonnull;
import java.util.List;

public class ScrollItem extends Item {
    private static final Item.Properties prop = new Properties().rarity(Rarity.UNCOMMON).stacksTo(1).tab(CreativeModeTab.TAB_COMBAT);

    public ScrollItem() {
        super(prop);
    }

    @Nonnull
    public static Skill[] getSkills(ItemStack stack) {
        return stack.getOrCreateTag().getCompound("skills").getAllKeys().stream().map(Skill::getSkill).toList().toArray(new Skill[4]);
    }

    public static void setSkill(ItemStack stack, Skill s) {
        stack.getOrCreateTag().putString("skill", s.getRegistryName().toString());
    }

    public static void setRandom(ItemStack stack, boolean random) {
        stack.getOrCreateTag().putBoolean("randomPick", random);
    }

    public static boolean isRandom(ItemStack stack) {
        return !stack.hasTag() || stack.getOrCreateTag().getBoolean("randomPick");
    }

    public static void setSkills(ItemStack stack, Skill... list) {
        CompoundTag tag = new CompoundTag();
        for (Skill s : list)
            if (s != null)
                tag.putBoolean(s.getRegistryName().toString(), true);
            else tag.putBoolean("null", true);
        stack.getOrCreateTag().put("skills", tag);
    }

    public static ItemStack makeScroll(boolean random, Skill... s) {
        ItemStack itemstack = new ItemStack(WarItems.SCROLL.get());
        setSkills(itemstack, s);
        setRandom(itemstack, random);
        return itemstack;
    }

    private static boolean noSkills(ItemStack stack) {
        Skill[] skills = getSkills(stack);
        for (Skill s : skills)
            if (s != null) return false;
        return true;
    }

    @Nonnull
    private static InteractionResultHolder<ItemStack> learnSkill(Player p, ItemStack stack, Skill skill) {
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
    public InteractionResultHolder<ItemStack> use(Level leve, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        Skill[] skills = getSkills(stack);
        //TODO open up a screen that gives 3 choices if not a fixed scroll. Allow determining which 3 via nbt (skill/style discrimination)
        boolean trueRandom = noSkills(stack);
        if (trueRandom) {
            List<Skill> set = WarSkills.SUPPLIER.get().getValues().stream().filter(a -> !CasterData.getCap(p).isSkillSelectable(a)).toList();
            if (!set.isEmpty()) {
                Skill put = set.get(WarDance.rand.nextInt(set.size()));
                skills = new Skill[]{put};
            }
        }
        Skill skill = null;
        if (skills.length == 1) {//only one thing to learn
            skill = skills[0];
        } else if (isRandom(stack)) {//randomly gives you a skill
            skill = skills[WarDance.rand.nextInt(skills.length)];
        }
        if (skill != null)
            return learnSkill(p, stack, skill);
        else {
            //TODO open gui by sending packet
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level p_41422_, List<Component> component, TooltipFlag flag) {
        if (noSkills(stack))
            component.add(Component.translatable("wardance.scroll.random").withStyle(ChatFormatting.AQUA));
        else
            for (Skill s : getSkills(stack)) {
                if (s == null)
                    component.add(Component.translatable("wardance.scroll.skill.random").withStyle(ChatFormatting.WHITE));
                else
                    component.add(Component.translatable("wardance.scroll.skill", s.getDisplayName(null)).withStyle(ChatFormatting.GOLD));
            }
    }

    public void fillItemCategory(CreativeModeTab p_41151_, NonNullList<ItemStack> p_41152_) {
        if (p_41151_ == CreativeModeTab.TAB_COMBAT) {
            p_41152_.add(new ItemStack(WarItems.SCROLL.get()));
            WarSkills.SUPPLIER.get().getValues().forEach((a) -> p_41152_.add(makeScroll(false, a)));
        }
    }
}
