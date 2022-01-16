package jackiecrazy.wardance.skill;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SkillCategory {
    private final String category;

    public SkillCategory(String name) {
        category = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillCategory && ((SkillCategory) obj).category.equals(category);
    }

    public ITextComponent name() {
        return new TranslationTextComponent("category." + category + ".name");
    }

    public ITextComponent description() {
        return new TranslationTextComponent("category." + category + ".desc");
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/" + category + ".png");
    }
}
