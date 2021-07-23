package jackiecrazy.wardance.capability.status;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DummyStatusCap implements IStatus {
    private static final Map<Skill, SkillData> dummy = new HashMap<>();

    @Nullable
    @Override
    public Optional<SkillData> getActiveStatus(Skill s) {
        return Optional.empty();
    }

    @Override
    public void addStatus(SkillData d) {

    }

    @Override
    public void removeStatus(Skill s) {

    }

    @Override
    public Map<Skill, SkillData> getActiveStatuses() {
        return dummy;
    }

    @Override
    public void clearStatuses() {

    }

    @Override
    public boolean isStatusActive(Skill skill) {
        return false;
    }

    @Override
    public CompoundNBT write() {
        return null;
    }

    @Override
    public void read(CompoundNBT from) {

    }

    @Override
    public void update() {

    }


}
