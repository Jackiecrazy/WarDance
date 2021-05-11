package jackiecrazy.wardance.skill;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

import javax.annotation.Nullable;

public class SkillCooldownData {
    private final Skill s;
    private float duration;
    private float maxDuration;

    public SkillCooldownData(Skill skill, float arbitraryDuration) {
        s = skill;
        duration = maxDuration = arbitraryDuration;
    }

    public SkillCooldownData(Skill skill, float maxDuration, float duration) {
        s = skill;
        this.duration = duration;
        this.maxDuration = maxDuration;
    }

    @Nullable
    public static SkillCooldownData read(CompoundNBT from) {
        if (!from.contains("skill") || from.getFloat("duration") == 0 || from.getFloat("maxDuration") == 0) return null;
        if (Skill.getSkill(from.getString("skill")) == null)
            return null;
        return new SkillCooldownData(Skill.getSkill(from.getString("skill")), from.getFloat("maxDuration"), from.getFloat("duration"));
    }

    public float getDuration() {
        return duration;
    }

    public SkillCooldownData setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public float getMaxDuration() {
        return maxDuration;
    }

    public SkillCooldownData setMaxDuration(float duration) {
        this.maxDuration = duration;
        return this;
    }

    public void decrementDuration(float amount) {
        duration-=amount;
    }

    public Skill getSkill() {
        return s;
    }

    public CompoundNBT write(CompoundNBT to) {
        to.putString("skill", s.getRegistryName().toString());
        to.putFloat("duration", duration);
        to.putFloat("maxDuration", maxDuration);
        return to;
    }
}
