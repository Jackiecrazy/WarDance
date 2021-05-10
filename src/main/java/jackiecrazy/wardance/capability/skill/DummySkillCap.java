package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.*;

public class DummySkillCap implements ISkillCapability{
    private static final Map<Skill, SkillData> dummy=new HashMap<>();
    private static final Map<Skill, Float> otherDummy=new HashMap<>();
    private static final List<Skill> moreDummies=new ArrayList<>();
    @Nullable
    @Override
    public Optional<SkillData> getActiveSkill(Skill s) {
        return Optional.empty();
    }

    @Override
    public void activateSkill(SkillData d) {

    }

    @Override
    public void removeActiveSkill(Skill s) {

    }

    @Override
    public Map<Skill, SkillData> getActiveSkills() {
        return dummy;
    }

    @Override
    public void clearActiveSkills() {

    }

    @Override
    public boolean isSkillActive(Skill skill) {
        return false;
    }

    @Override
    public boolean isTagActive(String tag) {
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {

    }

    @Override
    public void markSkillUsed(Skill s) {

    }

    @Override
    public void setSkillCooldown(Skill s, float amount) {

    }

    @Override
    public boolean isSkillCoolingDown(Skill s) {
        return false;
    }

    @Override
    public void decrementSkillCooldown(Skill s, float amount) {

    }

    @Override
    public void coolSkill(Skill s) {

    }

    @Override
    public float getSkillCooldown(Skill s) {
        return 0;
    }

    @Override
    public Map<Skill, Float> getSkillCooldowns() {
        return otherDummy;
    }

    @Override
    public void clearSkillCooldowns() {

    }

    @Override
    public List<Skill> getEquippedSkills() {
        return moreDummies;
    }

    @Override
    public void setEquippedSkills(List<Skill> skills) {

    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        return false;
    }

    @Override
    public CompoundNBT write(CompoundNBT to) {
        return new CompoundNBT();
    }

    @Override
    public void read(CompoundNBT from) {

    }

    @Override
    public void update() {

    }

    @Override
    public Skill[] getPastCasts() {
        return new Skill[5];
    }
}
