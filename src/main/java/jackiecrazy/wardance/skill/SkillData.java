package jackiecrazy.wardance.skill;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class SkillData {
    public static final SkillData DUMMY = new SkillData(WarSkills.VITAL_STRIKE.get(), 0);
    private final Skill s;
    private float duration, max, var;
    private boolean condition, dirty;
    private Skill.STATE state = Skill.STATE.INACTIVE;
    private LivingEntity caster;
    private UUID casterID;

    //TODO allow skills to store custom data

    public SkillData(Skill skill, float arbitraryDuration, float max) {
        s = skill;
        duration = arbitraryDuration;
        this.max = max;
        var = 0;
        condition = false;
    }

    public SkillData(Skill skill, float arbitraryDuration) {
        this(skill, arbitraryDuration, arbitraryDuration);
        state = Skill.STATE.ACTIVE;
    }

    @Nullable
    public static SkillData read(CompoundTag from) {
        if (!from.contains("skill") || !from.contains("duration")) return null;
        if (Skill.getSkill(from.getString("skill")) == null)
            return null;
        SkillData ret = new SkillData(Skill.getSkill(from.getString("skill")), from.getFloat("duration")).flagCondition(from.getBoolean("condition")).setArbitraryFloat(from.getFloat("something"));
        ret.max = from.getFloat("max");
        ret.state = Skill.STATE.values()[from.getInt("state")];
        if (from.contains("caster"))
            ret.casterID = from.getUUID("caster");
        return ret;
    }

    @Nullable
    public LivingEntity getCaster(Level world) {
        if (casterID == null) return null;
        if (caster != null) return caster;
        if (world.getPlayerByUUID(casterID) != null) {
            caster = world.getPlayerByUUID(casterID);
        }
        return caster;
    }

    public SkillData setCaster(LivingEntity caster) {
        casterID = caster.getUUID();
        this.caster = caster;
        return this;
    }

    public float getDuration() {
        return duration;
    }

    public SkillData setDuration(float duration) {
        this.duration = duration;
        if (duration > max) max = duration;
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

    /**
     * This should only be used by the skill capability.
     */
    public boolean _isDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }

    public void markDirty() {
        dirty = true;
    }

    public float getMaxDuration() {
        return max;
    }

    public SkillData setMaxDuration(float max) {
        this.max = max;
        return this;
    }

    public void decrementDuration() {
        duration--;
        markDirty();
    }

    public void decrementDuration(float amount) {
        duration -= amount;
    }

    public Skill getSkill() {
        return s;
    }

    public CompoundTag write(CompoundTag to) {
        if (s == null) return to;
        to.putString("skill", s.getRegistryName().toString());
        to.putInt("state", state.ordinal());
        to.putFloat("duration", duration);
        to.putFloat("max", max);
        to.putFloat("something", var);
        to.putBoolean("condition", condition);
        if (casterID != null)
            to.putUUID("caster", casterID);
        return to;
    }

    public Skill.STATE getState() {
        return state;
    }

    public SkillData setState(Skill.STATE state) {
        this.state = state;
        return this;
    }
}
