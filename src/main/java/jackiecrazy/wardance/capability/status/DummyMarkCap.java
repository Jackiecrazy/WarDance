package jackiecrazy.wardance.capability.status;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DummyMarkCap implements IMark {
    private static final Map<Skill, SkillData> dummy = new HashMap<>();

    @Nullable
    @Override
    public Optional<SkillData> getActiveMark(Skill s) {
        return Optional.empty();
    }

    @Override
    public void mark(SkillData d) {

    }

    @Override
    public void removeMark(Skill s) {

    }

    @Override
    public Map<Skill, SkillData> getActiveMarks() {
        return dummy;
    }

    @Override
    public void clearMarks() {

    }

    @Override
    public boolean isMarked(Skill skill) {
        return false;
    }

    @Override
    public boolean isMarked(SkillArchetype skill) {
        return false;
    }

    @Override
    public CompoundTag write() {
        return null;
    }

    @Override
    public void read(CompoundTag from) {

    }

    @Override
    public void update() {

    }


}
