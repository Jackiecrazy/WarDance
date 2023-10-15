package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public interface ISkillCapability {
    boolean isSkillSelectable(Skill s);

    void setSkillSelectable(Skill s, boolean selectable);

    void setColorSelectable(SkillCategory s, boolean selectable);

    List<Skill> getSelectableList();

    Optional<SkillData> getSkillData(Skill s);

    Skill getHolsteredSkill();

    default void holsterSkill(Skill s){
        holsterSkill(getEquippedSkills().indexOf(s));
    }
    void holsterSkill(int index);

    boolean changeSkillState(Skill d, Skill.STATE to);

    Map<Skill, SkillData> getAllSkillData();

    Skill.STATE getSkillState(Skill skill);

    default Set<Skill> getEquippedVariations(SkillArchetype base) {
        return getEquippedSkills().stream().filter(a -> a != null && a.getArchetype() == base).collect(Collectors.toSet());
    }

    @Nullable
    SkillStyle getStyle();

    void setStyle(SkillStyle style);

    boolean isTagActive(String tag);

    void removeActiveTag(String tag);

    List<SkillCategory> getEquippedColors();

    default List<Skill> getEquippedSkillsAndStyle() {
        List<Skill> ret = new ArrayList<>(getEquippedSkills());
        ret.add(0, getStyle());
        return ret;
    }

    List<Skill> getEquippedSkills();

    void setEquippedSkills(List<Skill> skills);

    default boolean isSkillEquipped(Skill s) {
        return s != null && getEquippedSkillsAndStyle().contains(s);
    }

    boolean equipSkill(Skill skill);

    boolean replaceSkill(Skill from, Skill to);

    void setEquippedSkillsAndUpdate(SkillStyle style, List<Skill> skills);

    boolean isSkillUsable(Skill skill);

    CompoundTag write();

    void read(CompoundTag from);

    void update();

    Skill[] getPastCasts();
}
