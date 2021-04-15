package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.Skill;
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

    void markSkillUsed(Skill s);

    void setSkillCooldown(Skill s, float amount);

    void decrementSkillCooldown(Skill s, float amount);

    float getSkillCooldown(Skill s);

    Map<Skill, Float> getSkillCooldowns();

    void clearSkillCooldowns();

    List<Skill> getEquippedSkills();

    boolean isSkillUsable(Skill skill);

    CompoundNBT write(CompoundNBT to);

    void read(CompoundNBT from);

    Skill[] getPastCasts();
}
