package jackiecrazy.wardance.skill;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class SkillCategory {
    private final String category;
    private final Color color;

    public SkillCategory(String name, Color c) {
        category = name;
        color = c;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillCategory && ((SkillCategory) obj).category.equals(category);
    }

    public Component name() {
        return Component.translatable("wardance:category." + category + ".name").withStyle(ChatFormatting.BOLD);
    }

    public Component description() {
        return Component.translatable("wardance:category." + category + ".desc").withStyle(ChatFormatting.ITALIC);
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/categories/" + category + ".png");
    }

    public Color getColor() {
        return color;
    }
}
