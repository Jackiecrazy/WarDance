package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.*;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ISkillCapability {
    boolean isSkillSelectable(Skill s);

    void setSkillSelectable(Skill s, boolean selectable);

    List<Skill> getSelectableList();

    Optional<SkillData> getSkillData(Skill s);

    Skill getHolsteredSkill();

    void holsterSkill(int index);

    boolean changeSkillState(Skill d, Skill.STATE to);

    Map<Skill, SkillData> getAllSkillData();

    Skill.STATE getSkillState(Skill skill);

    Skill.STATE getArchetypeState(SkillArchetype s);

    @Nullable
    Skill getEquippedVariation(SkillArchetype base);

    @Nullable
    SkillStyle getStyle();

    void setStyle(SkillStyle style);

    boolean isTagActive(String tag);

    void removeActiveTag(String tag);

    List<SkillCategory> getEquippedColors();

    default List<Skill> getEquippedSkillsAndStyle(){
        List<Skill> ret = new ArrayList<>(getEquippedSkills());
        ret.add(getStyle());
        return ret;
    }

    List<Skill> getEquippedSkills();

    void setEquippedSkills(List<Skill> skills);

    boolean isSkillUsable(Skill skill);

    CompoundTag write();

    void read(CompoundTag from);

    void update();

    Skill[] getPastCasts();
}
