package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.capability.skill.CasterData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class SkillStyle extends Skill {
    public static final List<SkillStyle> styleList = new ArrayList<>();

    private final int maxColors;

    public SkillStyle(int max) {
        maxColors = max;
        styleList.add(this);
    }

    public static SkillStyle getStyle(ResourceLocation rl) {
        return getSkill(rl) instanceof SkillStyle ss ? ss : null;
    }

    public int getMaxColors() {
        return maxColors;
    }

    @Override
    public boolean isEquippableWith(Skill s, LivingEntity caster) {
        if (s == null) return true;
        List<SkillCategory> equipped = CasterData.getCap(caster).getEquippedColors();
        return equipped.contains(s.getCategory()) || equipped.size() < maxColors;
    }

    public ResourceLocation icon() {
        return new ResourceLocation("wardance:textures/skill/" + getRegistryName().getPath() + ".png");
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }
}
