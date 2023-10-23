package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.HashMap;

public class SkillCategory {

    private static final HashMap<String, SkillCategory> colors = new HashMap<>();
    private final String category;
    private final Color color;
    private final ChatFormatting[] formattings;

    public SkillCategory(String name, Color c, ChatFormatting... ca) {
        category = name;
        color = c;
        formattings = ca;
        colors.put(name, this);
    }

    public static SkillCategory fromString(String key) {
        return colors.get(key);
    }

    public ChatFormatting[] getFormattings() {
        return formattings;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillCategory && ((SkillCategory) obj).category.equals(category);
    }

    public String rawName() {
        return category;
    }

    public ResourceLocation baseName() {
        return new ResourceLocation(WarDance.MODID, category);
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
