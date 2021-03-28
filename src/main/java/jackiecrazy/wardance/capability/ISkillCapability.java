package jackiecrazy.wardance.capability;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.List;

public interface ISkillCapability {
    @Nullable
    SkillData getActiveSkill(Skill s);

    void activateSkill(SkillData d);

    void removeActiveSkill(Skill s);

    void addUsableSkill(Skill s);

    void removeUsableSkill(Skill s);

    List<Skill> getUsableSkills();

    List<Skill> getEquippedSkills();

    List<Skill> getSkillsForItem(Item i);

    boolean isSkillUsable(Skill skill);

    CompoundNBT write(CompoundNBT to);

    void read(CompoundNBT from);
}
