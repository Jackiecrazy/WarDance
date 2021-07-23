package jackiecrazy.wardance.capability.status;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundNBT;

import java.util.Map;
import java.util.Optional;

public interface IStatus {
    Optional<SkillData> getActiveStatus(Skill s);

    void addStatus(SkillData d);

    void removeStatus(Skill s);

    Map<Skill, SkillData> getActiveStatuses();

    void clearStatuses();

    boolean isStatusActive(Skill skill);

    CompoundNBT write();

    void read(CompoundNBT from);

    void update();
}
