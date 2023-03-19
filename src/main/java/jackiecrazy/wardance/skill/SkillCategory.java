package jackiecrazy.wardance.skill;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class SkillCategory {
    private final String category;
    private final Color color;
    private final ChatFormatting[] formattings;

    public SkillCategory(String name, Color c, ChatFormatting... ca) {
        category = name;
        color = c;
        formattings=ca;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillCategory && ((SkillCategory) obj).category.equals(category);
    }

    public Component name() {
        return Component.translatable("wardance.category." + category + ".name").withStyle(formattings);//.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("tooltip"))));
    }

    public Component description() {
        return Component.translatable("wardance.category." + category + ".desc").withStyle(ChatFormatting.ITALIC);
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/categories/" + category + ".png");
    }

    public Color getColor() {
        return color;
    }
}
