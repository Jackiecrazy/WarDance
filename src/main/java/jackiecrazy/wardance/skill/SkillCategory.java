package jackiecrazy.wardance.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SkillCategory {
    private final String category;

    public SkillCategory(String name) {
        category = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillCategory && ((SkillCategory) obj).category.equals(category);
    }

    public Component name() {
        return new TranslatableComponent("category." + category + ".name");
    }

    public Component description() {
        return new TranslatableComponent("category." + category + ".desc");
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/" + category + ".png");
    }
}
