package jackiecrazy.wardance.skill;

import net.minecraft.resources.ResourceLocation;

public class SkillArchetype {
    private final String archetype;

    public SkillArchetype(String name) {
        archetype = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkillArchetype && ((SkillArchetype) obj).archetype.equals(archetype);
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/" + archetype + ".png");
    }
}
