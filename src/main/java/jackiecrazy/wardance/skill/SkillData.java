package jackiecrazy.wardance.skill;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class SkillData {
    private Skill s;
    private int variation;
    private int duration;

    public SkillData(Skill skill, int variant, int ticks) {
        s = skill;
        variation = variant;
        duration = ticks;
    }

    @Nullable
    public static SkillData read(CompoundNBT from) {
        if (!from.contains("skill") || from.getInt("variation") == 0 || from.getInt("duration") == 0) return null;
        if (!GameRegistry.findRegistry(Skill.class).containsKey(new ResourceLocation(from.getString("skill"))))
            return null;
        return new SkillData(GameRegistry.findRegistry(Skill.class).getValue(new ResourceLocation(from.getString("skill"))), from.getInt("variation"), from.getInt("duration"));
    }

    public int getDuration() {
        return duration;
    }

    public void decrementDuration() {
        duration--;
    }

    public int getVariation() {
        return variation;
    }

    public Skill getSkill() {
        return s;
    }

    public CompoundNBT write(CompoundNBT to) {
        to.putString("skill", s.getRegistryName().toString());
        to.putInt("variation", variation);
        to.putInt("duration", duration);
        return to;
    }
}
