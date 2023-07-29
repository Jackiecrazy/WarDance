package jackiecrazy.wardance.capability.status;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.Optional;

public interface IMark {
    Optional<SkillData> getActiveMark(Skill s);

    void mark(SkillData d);

    void removeMark(Skill s);

    Map<Skill, SkillData> getActiveMarks();

    void clearMarks();

    boolean isMarked(Skill skill);

    boolean isMarked(SkillArchetype skill);

    CompoundTag write();

    void read(CompoundTag from);

    void update();

    void sync();
}
