package jackiecrazy.wardance.skill;

import net.minecraft.resources.ResourceLocation;

public class SkillArchetype {
    private final String category;

    public SkillArchetype(String name) {
        category = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillArchetype && ((SkillArchetype) obj).category.equals(category);
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/" + category + ".png");
    }
}
