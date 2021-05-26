package jackiecrazy.wardance.skill;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

import javax.annotation.Nullable;

public class SkillData {
    private final Skill s;
    private Hand h;
    private float duration, var;
    private boolean condition;

    public SkillData(Skill skill, float arbitraryDuration) {
        s = skill;
        duration = arbitraryDuration;
    }

    @Nullable
    public static SkillData read(CompoundNBT from) {
        if (!from.contains("skill") || from.getFloat("duration") == 0) return null;
        if (Skill.getSkill(from.getString("skill")) == null)
            return null;
        return new SkillData(Skill.getSkill(from.getString("skill")), from.getFloat("duration")).flagCondition(from.getBoolean("condition")).setArbitraryFloat(from.getFloat("something"));
    }

    public float getDuration() {
        return duration;
    }

    public SkillData setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public float getArbitraryFloat() {
        return var;
    }

    public SkillData setArbitraryFloat(float variable) {
        this.var = variable;
        return this;
    }

    public boolean isCondition() {
        return condition;
    }

    public SkillData flagCondition(boolean success) {
        condition = success;
        return this;
    }

    public void decrementDuration() {
        duration--;
    }

    public Skill getSkill() {
        return s;
    }

    public CompoundNBT write(CompoundNBT to) {
        to.putString("skill", s.getRegistryName().toString());
        to.putFloat("duration", duration);
        to.putFloat("something", var);
        to.putBoolean("condition", condition);
        return to;
    }
}
