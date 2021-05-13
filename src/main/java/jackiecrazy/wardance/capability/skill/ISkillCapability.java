package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCooldownData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ISkillCapability {
    Optional<SkillData> getActiveSkill(Skill s);

    void activateSkill(SkillData d);

    void removeActiveSkill(Skill s);

    Map<Skill, SkillData> getActiveSkills();

    void clearActiveSkills();

    boolean isSkillActive(Skill skill);

    boolean isTagActive(String tag);

    void removeActiveTag(String tag);

    void markSkillUsed(Skill s);

    void setSkillCooldown(Skill s, float amount);

    boolean isSkillCoolingDown(Skill s);

    void decrementSkillCooldown(Skill s, float amount);

    void coolSkill(Skill s);

    float getSkillCooldown(Skill s);

    float getMaxSkillCooldown(Skill s);

    Map<Skill, SkillCooldownData> getSkillCooldowns();

    void clearSkillCooldowns();

    List<Skill> getEquippedSkills();

    void setEquippedSkills(List<Skill> skills);

    boolean isSkillUsable(Skill skill);

    CompoundNBT write();

    void read(CompoundNBT from);

    void update();

    Skill[] getPastCasts();
}
