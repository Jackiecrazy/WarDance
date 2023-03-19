package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.skill.*;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.*;

public class DummySkillCap implements ISkillCapability {
    private static final Map<Skill, SkillData> dummy = new HashMap<>();
    private static final List<Skill> otherDummy = new ArrayList<>();
    private static final List<SkillCategory> dum = new ArrayList<>();


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
    public boolean changeSkillState(Skill d, Skill.STATE to) {
        return false;
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
    public Skill.STATE getArchetypeState(SkillArchetype s) {
        return Skill.STATE.INACTIVE;
    }

    @Nullable
    @Override
    public Skill getEquippedVariation(SkillArchetype base) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public SkillStyle getStyle() {
        return null;
    }

    @Override
    public void setStyle(SkillStyle style) {

    }

    @Override
    public boolean isTagActive(String tag) {
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {

    }

    @Override
    public List<SkillCategory> getEquippedColors() {
        return dum;
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
    public CompoundTag write() {
        return new CompoundTag();
    }

    @Override
    public void read(CompoundTag from) {

    }

    @Override
    public void update() {

    }

    @Override
    public Skill[] getPastCasts() {
        return new Skill[5];
    }
}
