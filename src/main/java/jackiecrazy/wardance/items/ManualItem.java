package jackiecrazy.wardance.items;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ManualItem extends Item {
    private static final Item.Properties prop = new Properties().rarity(Rarity.RARE).stacksTo(1).tab(WarDance.WARTAB);

    public ManualItem() {
        super(prop);
    }

    public List<Skill> getSkills(ItemStack stack) {
        //style, active12345, passive12345
        List<Skill> ret = new ArrayList<>();
        final CompoundTag tag = stack.getOrCreateTag();
        ret.add(Skill.getSkill(tag.getString("style")));
        for (int a = 1; a < 6; a++) {
            ret.add(Skill.getSkill(tag.getString("active" + a)));
        }
        for (int a = 1; a < 6; a++) {
            ret.add(Skill.getSkill(tag.getString("passive" + a)));
        }
        return ret;
    }

    public void setSkill(ItemStack stack, List<Skill> s) {
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

    public boolean autoLearn(ItemStack stack) {
        return !stack.getOrCreateTag().getBoolean("noUnlock");
    }

    public void setAutoLearn(ItemStack stack, boolean s) {
        stack.getOrCreateTag().putBoolean("noUnlock", !s);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        //TODO open up a screen that shows the skill wheel after equipping and an nbt-defined blurb on what the build does
        final List<Skill> skills = getSkills(stack);
        boolean autoLearn = autoLearn(stack);
        final ISkillCapability cap = CasterData.getCap(p);
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
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level p_41422_, List<Component> component, TooltipFlag flag) {
        if (autoLearn(stack))
            component.add(Component.translatable("wardance.manual.autolearn").withStyle(ChatFormatting.GOLD));
        else component.add(Component.translatable("wardance.manual.nolearn").withStyle(ChatFormatting.GRAY));
        for (Skill s : getSkills(stack)) {
            if (s != null)
                component.add(s.getDisplayName(null).withStyle(s.getCategory().getFormattings()));
        }
    }
}
