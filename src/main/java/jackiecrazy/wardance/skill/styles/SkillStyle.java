package jackiecrazy.wardance.skill.styles;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class SkillStyle extends Skill {
    public static final List<SkillStyle> styleList = new ArrayList<>();
    private final int maxColors;
    private Color colour = Color.WHITE;

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
    public Skill setCategory(SkillCategory sc) {
        return this;//cannot set category
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

    @Override
    public Color getColor() {
        return colour;
    }

    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return passive;
    }

    public boolean canCast(LivingEntity caster, Skill s){
        return true;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    public SkillStyle setColor(Color sc) {
        colour = sc;
        return this;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return passive(prev, from, to);
    }
}
