package jackiecrazy.wardance.items;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.OpenScrollScreenPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScrollItem extends Item {
    private static final Item.Properties prop = new Properties().rarity(Rarity.UNCOMMON).stacksTo(1).tab(CreativeModeTab.TAB_COMBAT);

    public ScrollItem() {
        super(prop);
    }

    @Nonnull
    public static Skill[] getSkills(ItemStack stack) {
        if (stack.getOrCreateTag().getBoolean("skills")) {
            return WarSkills.SUPPLIER.get().getValues().toArray(new Skill[0]);
        }
        final List<Skill> skills = stack.getOrCreateTag().getCompound("skills").getAllKeys().stream().map(Skill::getSkill).toList();
        return skills.toArray(new Skill[0]);
    }

    /**
     * not setting this will allow either
     */
    public static void setStyle(ItemStack stack, boolean style) {
        stack.getOrCreateTag().putBoolean("style", style);
    }

    public static boolean isStyle(ItemStack stack) {
        return !stack.hasTag() || !stack.getOrCreateTag().contains("style") || stack.getOrCreateTag().getBoolean("style");
    }

    public static boolean isSkill(ItemStack stack) {
        return !stack.hasTag() || !stack.getOrCreateTag().contains("style") || !stack.getOrCreateTag().getBoolean("style");
    }

    public static void setRandom(ItemStack stack, boolean random) {
        stack.getOrCreateTag().putBoolean("randomPick", random);
    }

    public static boolean isRandom(ItemStack stack) {
        return !stack.hasTag() || stack.getOrCreateTag().getBoolean("randomPick");
    }

    public static void setRandomSize(ItemStack stack, int random) {
        stack.getOrCreateTag().putInt("randomSelection", random);
    }

    public static int getRandomSize(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains("randomSelection") ? stack.getOrCreateTag().getInt("randomSelection") : getSkills(stack).length;
    }

    public static void setSkills(ItemStack stack, Skill... list) {
        if (list[0] == null) {
            stack.getOrCreateTag().putBoolean("skills", true);
            return;
        }
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
    public static InteractionResultHolder<ItemStack> learnSkill(Player p, ItemStack stack, Skill skill) {
        if (!List.of(getSkills(stack)).contains(skill)) {
            p.displayClientMessage(Component.translatable("wardance.scroll.broken").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
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
    public InteractionResultHolder<ItemStack> use(Level leve, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        Skill[] skills = getSkills(stack);
        //TODO open up a screen that gives 3 choices if not a fixed scroll. Allow determining which 3 via nbt (skill/style discrimination)
        boolean trueRandom = noSkills(stack);
        if (trueRandom) {
            List<Skill> set = WarSkills.SUPPLIER.get().getValues().stream().filter(a -> a != null && !CasterData.getCap(p).isSkillSelectable(a)).toList();
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
        else if (p instanceof ServerPlayer sp) {
            //figure out how many skills go in the end
            int select = getRandomSize(stack);
            List<Skill> list = Stream.of(skills).filter(a -> a != null && !CasterData.getCap(p).isSkillSelectable(a) && ((a instanceof SkillStyle && isStyle(stack)) || (!(a instanceof SkillStyle) && isSkill(stack)))).collect(Collectors.toList());
            while (select < list.size()) {
                list.remove(WarDance.rand.nextInt(list.size()));
            }
            if (list.size() == 0) {
                p.displayClientMessage(Component.translatable("wardance.scroll.learned.all").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }
            skills = new Skill[list.size()];
            skills = list.toArray(skills);
            setSkills(stack, skills);
            if (skills.length == 1) {//only one thing to learn
                return learnSkill(p, stack, skills[0]);
            }
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new OpenScrollScreenPacket(hand == InteractionHand.OFF_HAND, skills));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level p_41422_, List<Component> component, TooltipFlag flag) {
        if (noSkills(stack))
            component.add(Component.translatable("wardance.scroll.random").withStyle(ChatFormatting.AQUA));
        else {
            final Skill[] skills = getSkills(stack);
            if (skills.length > getRandomSize(stack)) {
                if (isStyle(stack) && isSkill(stack))
                    component.add(Component.translatable("wardance.scroll.random").withStyle(ChatFormatting.WHITE));
                else {
                    if (isStyle(stack))
                        component.add(Component.translatable("wardance.scroll.style" + (isRandom(stack) ? ".random" : ".fixed")).withStyle(ChatFormatting.BLUE));
                    if (isSkill(stack))
                        component.add(Component.translatable("wardance.scroll.skill" + (isRandom(stack) ? ".random" : ".fixed")).withStyle(ChatFormatting.AQUA));
                }
            } else for (Skill s : skills) {
                if (s == null)
                    component.add(Component.translatable("wardance.scroll.skill.random").withStyle(ChatFormatting.WHITE));
                else
                    component.add(Component.translatable("wardance.scroll.skill", s.getDisplayName(null)).withStyle(ChatFormatting.GOLD));
            }
        }
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (tab == CreativeModeTab.TAB_COMBAT) {
            final Collection<Skill> values = WarSkills.SUPPLIER.get().getValues();
            ItemStack everything = makeScroll(false, (Skill) null);
            setRandomSize(everything, 4);
            list.add(everything);

            ItemStack random = everything.copy();
            setRandom(random, true);
            list.add(random);

            ItemStack styles = everything.copy();
            setStyle(styles, true);
            list.add(styles);

            ItemStack skill = everything.copy();
            setStyle(skill, false);
            list.add(skill);

            list.add(new ItemStack(WarItems.SCROLL.get()));
            values.forEach((a) -> {
                list.add(makeScroll(false, a));
            });
        }
    }
}
