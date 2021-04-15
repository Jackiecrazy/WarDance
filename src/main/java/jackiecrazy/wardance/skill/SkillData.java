package jackiecrazy.wardance.skill;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

public class SkillData {
    private final Skill s;
    private Hand h;

    public SkillData setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    private float duration;

    public SkillData(Skill skill, float arbitraryDuration) {
        s = skill;
        duration = arbitraryDuration;
    }

    @Nullable
    public static SkillData read(CompoundNBT from) {
        if (!from.contains("skill") || from.getFloat("duration") == 0) return null;
        if (!GameRegistry.findRegistry(Skill.class).containsKey(new ResourceLocation(from.getString("skill"))))
            return null;
        return new SkillData(GameRegistry.findRegistry(Skill.class).getValue(new ResourceLocation(from.getString("skill"))), from.getFloat("duration"));
    }

    public float getDuration() {
        return duration;
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
        return to;
    }
}
