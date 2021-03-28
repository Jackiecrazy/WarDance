package jackiecrazy.wardance.capability;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.List;

public class DummySkillCap implements ISkillCapability{
    @Nullable
    @Override
    public SkillData getActiveSkill(Skill s) {
        return null;
    }

    @Override
    public void activateSkill(SkillData d) {

    }

    @Override
    public void removeActiveSkill(Skill s) {

    }

    @Override
    public void addUsableSkill(Skill s) {

    }

    @Override
    public void removeUsableSkill(Skill s) {

    }

    @Override
    public List<Skill> getUsableSkills() {
        return null;
    }

    @Override
    public List<Skill> getEquippedSkills() {
        return null;
    }

    @Override
    public List<Skill> getSkillsForItem(Item i) {
        return null;
    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        return false;
    }

    @Override
    public CompoundNBT write(CompoundNBT to) {
        return null;
    }

    @Override
    public void read(CompoundNBT from) {

    }
}
