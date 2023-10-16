package jackiecrazy.wardance.items;

import jackiecrazy.wardance.client.WarCustomItemRender;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class DummyItem extends ScrollItem{
    public static ItemStack makeScroll(boolean random, Skill... s) {
        ItemStack itemstack = new ItemStack(WarItems.DUMMY.get());
        setSkills(itemstack, s);
        setRandom(itemstack, random);
        return itemstack;
    }
    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level p_41422_, List<Component> component, TooltipFlag flag) {
        component.add(Component.translatable("wardance.scroll.dummy").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return WarCustomItemRender.INSTANCE;
            }
        });
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {

    }

    public static Skill getSkill(ItemStack stack){
        final Skill[] skills = ScrollItem.getSkills(stack);
        if(skills.length>0)
            return skills[0];
        return WarSkills.VITAL_STRIKE.get();
    }

    public static Color getColor(ItemStack stack){
        return getSkill(stack).getColor();
    }
}
