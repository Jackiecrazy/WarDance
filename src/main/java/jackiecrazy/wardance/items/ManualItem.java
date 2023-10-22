package jackiecrazy.wardance.items;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.OpenManualScreenPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ManualItem extends Item {
    private static final Item.Properties prop = new Properties().rarity(Rarity.RARE).stacksTo(1).tab(WarDance.WARTAB);
    private static final ResourceLocation BOOKS = new ResourceLocation(WarDance.MODID, "manuals");


    public ManualItem() {
        super(prop);
    }

    public static List<Skill> getSkills(ItemStack stack) {
        //style, active12345, passive12345
        List<Skill> ret = new ArrayList<>();
        final CompoundTag tag = stack.getOrCreateTag();
        for (int a = 1; a < 6; a++) {
            ret.add(Skill.getSkill(tag.getString("active" + a)));
        }
        for (int a = 1; a < 6; a++) {
            ret.add(Skill.getSkill(tag.getString("passive" + a)));
        }
        return ret;
    }

    public static SkillStyle getStyle(ItemStack stack) {
        //style, active12345, passive12345
        final CompoundTag tag = stack.getOrCreateTag();
        return SkillStyle.getStyle(tag.getString("style"));
    }

    public static void setSkill(ItemStack stack, List<Skill> s) {
        final CompoundTag tag = stack.getOrCreateTag();
        if (s.get(0) != null)
            tag.putString("style", s.get(0).getRegistryName().toString());
        for (int a = 1; a < 6; a++) {
            if (s.get(a) != null)
                tag.putString("active" + a, s.get(a).getRegistryName().toString());
        }
        for (int a = 1; a < 6; a++) {
            if (s.get(a + 5) != null)
                tag.putString("passive" + a, s.get(a + 5).getRegistryName().toString());
        }
        stack.setTag(tag);
    }

    public static boolean autoLearn(ItemStack stack) {
        return !stack.getOrCreateTag().getBoolean("noUnlock");
    }

    public static void setAutoLearn(ItemStack stack, boolean s) {
        stack.getOrCreateTag().putBoolean("noUnlock", !s);
    }

    public static InteractionResultHolder<ItemStack> learn(Player p, ItemStack stack) {
        if (GeneralConfig.debug)
            System.out.println(stack.save(new CompoundTag()).getAsString());
        final List<Skill> skills = getSkills(stack);
        boolean autoLearn = autoLearn(stack);
        final ISkillCapability cap = CasterData.getCap(p);
        final SkillStyle ss = getStyle(stack);
        skills.add(0, ss);
        for (Skill s : skills) {
            if (autoLearn)
                cap.setSkillSelectable(s, true);
            else if (s == null || !cap.isSkillSelectable(s)) {
                if (s instanceof SkillStyle) {
                    //cannot learn the style, auto-fail
                    p.displayClientMessage(Component.translatable("wardance.manual.style").withStyle(ChatFormatting.RED), true);
                    return InteractionResultHolder.fail(stack);
                }
            }
        }
        try {
            cap.setEquippedSkillsAndUpdate((SkillStyle) skills.get(0), skills.subList(1, skills.size()));
            if (!new HashSet<>(cap.getEquippedSkillsAndStyle()).containsAll(skills)) {
                //could not learn everything
                p.displayClientMessage(Component.translatable("wardance.manual.partiallearn", stack.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
            } else
                p.displayClientMessage(Component.translatable("wardance.manual.learn", stack.getDisplayName()).withStyle(ChatFormatting.GREEN), true);
            return InteractionResultHolder.success(stack);
        } catch (Exception ignored) {
            p.displayClientMessage(Component.translatable("wardance.manual.invalid").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        if (p instanceof ServerPlayer sp && l instanceof ServerLevel sl) {
            int random = stack.getOrCreateTag().getInt("rollRandom");
            if (random > 0) {
                if (!p.getAbilities().instabuild)
                    stack.shrink(1);
                LootContext lootcontext = (new LootContext.Builder(sl)).withParameter(LootContextParams.THIS_ENTITY, p).withParameter(LootContextParams.ORIGIN, p.position()).withRandom(p.getRandom()).withLuck(p.getLuck()).create(LootContextParamSets.ADVANCEMENT_REWARD); // FORGE: luck to LootContext
                LootTable lt = l.getServer().getLootTables().get(BOOKS);
                lt.getRandomItems(lootcontext).forEach(sp::addItem);
                return InteractionResultHolder.success(stack);
            }
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new OpenManualScreenPacket(hand == InteractionHand.OFF_HAND));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.success(stack);//learn(p, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level p_41422_, List<Component> component, TooltipFlag flag) {
        CompoundTag compoundtag = stack.getOrCreateTag();
        if (compoundtag.getInt("rollRandom") != 0) {
            component.add(Component.translatable("wardance.manual.random").withStyle(ChatFormatting.GRAY));
            return;
        }
        String author = compoundtag.getString("author");
        if (!StringUtil.isNullOrEmpty(author)) {
            component.add(Component.literal(author).withStyle(ChatFormatting.GRAY));
        }
        if (autoLearn(stack))
            component.add(Component.translatable("wardance.manual.autolearn").withStyle(ChatFormatting.GOLD));
        else component.add(Component.translatable("wardance.manual.nolearn").withStyle(ChatFormatting.GRAY));
        final SkillStyle style = getStyle(stack);
        if (style != null)
            component.add(style.getDisplayName(null).withStyle(ChatFormatting.UNDERLINE));
        for (Skill s : getSkills(stack)) {
            if (s != null)
                component.add(s.getDisplayName(null).withStyle(s.getCategory().getFormattings()));
        }
    }

    public Component getName(ItemStack p_43480_) {
        CompoundTag compoundtag = p_43480_.getTag();
        if (compoundtag != null) {
            String s = compoundtag.getString("title");
            if (!StringUtil.isNullOrEmpty(s)) {
                return Component.literal(s);
            }
        }

        return super.getName(p_43480_);
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (tab == WarDance.WARTAB) {
//            ItemStack chest=new ItemStack(WarItems.MANUAL.get());
//            chest.setHoverName(Component.literal("Library of Alexandria"));
//            chest.getOrCreateTag().putInt("rollRandom", 2);
//            list.add(chest);
            ItemStack chest = new ItemStack(WarItems.MANUAL.get());
            chest.setHoverName(Component.literal("Dusty Tattered Tome"));
            chest.getOrCreateTag().putInt("rollRandom", 1);
            list.add(chest);
        }
    }
}
