package jackiecrazy.wardance.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class SkillData {
    private final Skill s;
    private Hand h;
    private float duration, max, var;
    private boolean condition;
    private LivingEntity caster;
    private UUID casterID;

    //TODO allow skills to store custom data

    public SkillData(Skill skill, float arbitraryDuration) {
        s = skill;
        duration = arbitraryDuration;
        max = duration;
        var = 0;
        condition = false;
    }

    @Nullable
    public static SkillData read(CompoundNBT from) {
        if (!from.contains("skill") || !from.contains("duration")) return null;
        if (Skill.getSkill(from.getString("skill")) == null)
            return null;
        SkillData ret = new SkillData(Skill.getSkill(from.getString("skill")), from.getFloat("duration")).flagCondition(from.getBoolean("condition")).setArbitraryFloat(from.getFloat("something"));
        ret.max=from.getFloat("max");
        if (from.contains("caster"))
            ret.casterID = from.getUniqueId("caster");
        return ret;
    }

    @Nullable
    public LivingEntity getCaster(World world) {
        if (casterID == null) return null;
        if (caster != null) return caster;
        if (world.getPlayerByUuid(casterID) != null) {
            caster = world.getPlayerByUuid(casterID);
        }
        return caster;
    }

    public SkillData setCaster(LivingEntity caster) {
        casterID = caster.getUniqueID();
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

    public float getMaxDuration() {
        return max;
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
        to.putFloat("max", max);
        to.putFloat("something", var);
        to.putBoolean("condition", condition);
        if (casterID != null)
            to.putUniqueId("caster", casterID);
        return to;
    }
}
