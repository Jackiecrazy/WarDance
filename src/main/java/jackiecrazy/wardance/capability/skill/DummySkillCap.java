package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.*;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.*;

public class DummySkillCap implements ISkillCapability {
    private static final Map<Skill, SkillData> dummy = new HashMap<>();
    private static final List<Skill> otherDummy = new ArrayList<>();


    @Override
    public boolean isSkillSelectable(Skill s) {
        return false;
    }

    @Override
    public void setSkillSelectable(Skill s, boolean selectable) {

    }

    @Override
    public List<Skill> getSelectableList() {
        return otherDummy;
    }

    @Override
    public Optional<SkillData> getSkillData(Skill s) {
        return Optional.empty();
    }

    @Override
    public Skill getHolsteredSkill() {
        return null;
    }

    @Override
    public void holsterSkill(int index) {

    }

    @Override
    public void changeSkillState(Skill d, Skill.STATE to) {

    }

    @Override
    public Map<Skill, SkillData> getAllSkillData() {
        return dummy;
    }

    @Override
    public Skill.STATE getSkillState(Skill skill) {
        return Skill.STATE.INACTIVE;
    }

    @Override
    public Skill.STATE getCategoryState(SkillCategory s) {
        return Skill.STATE.INACTIVE;
    }

    @Nullable
    @Override
    public Skill getEquippedVariation(SkillCategory base) {
        return null;
    }

    @Override
    public boolean isTagActive(String tag) {
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {

    }

    @Override
    public List<Skill> getEquippedSkills() {
        return otherDummy;
    }

    @Override
    public void setEquippedSkills(List<Skill> skills) {

    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        return false;
    }

    @Override
    public CompoundNBT write() {
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
