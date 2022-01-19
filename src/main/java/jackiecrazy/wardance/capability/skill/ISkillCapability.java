package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.*;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
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

    void changeSkillState(Skill d, Skill.STATE to);

    Map<Skill, SkillData> getAllSkillData();

    Skill.STATE getSkillState(Skill skill);

    Skill.STATE getCategoryState(SkillCategory s);

    @Nullable
    Skill getEquippedVariation(SkillCategory base);

    boolean isTagActive(String tag);

    void removeActiveTag(String tag);

    List<Skill> getEquippedSkills();

    void setEquippedSkills(List<Skill> skills);

    boolean isSkillUsable(Skill skill);

    CompoundNBT write();

    void read(CompoundNBT from);

    void update();

    Skill[] getPastCasts();
}
