package jackiecrazy.wardance.capability.status;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundNBT;

import java.util.Map;
import java.util.Optional;

public interface IMark {
    Optional<SkillData> getActiveMark(Skill s);

    void mark(SkillData d);

    void removeMark(Skill s);

    Map<Skill, SkillData> getActiveMarks();

    void clearMarks();

    boolean isMarked(Skill skill);

    boolean isMarked(SkillCategory skill);

    CompoundNBT write();

    void read(CompoundNBT from);

    void update();
}
