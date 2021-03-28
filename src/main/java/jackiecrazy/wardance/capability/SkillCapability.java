package jackiecrazy.wardance.capability;

import com.google.common.collect.Maps;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class SkillCapability implements ISkillCapability {
    private final Map<Skill, SkillData> activeSkills = Maps.newHashMap();
    private WeakReference<LivingEntity> dude = null;

    public SkillCapability(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    @Nullable
    @Override
    public SkillData getActiveSkill(Skill s) {
        return null;
    }

    @Override
    public void activateSkill(SkillData d) {
        if (activeSkills.containsKey(d.getSkill())) {
            WarDance.LOGGER.warn("skill " + d + " is already active, overwriting.");
            activeSkills.put(d.getSkill(), d);
        }
    }

    @Override
    public void removeActiveSkill(Skill s) {
        SkillData sd = activeSkills.get(s);
        if (sd != null && dude.get() != null) {
            sd.getSkill().onEffectEnd(dude.get(), sd);
        }
    }

    @Override
    public void addUsableSkill(Skill s) {

    }

    @Override
    public void removeUsableSkill(Skill s) {

    }

    @Override
    public List<Skill> getUsableSkills() {
        return null;
    }

    @Override
    public List<Skill> getEquippedSkills() {
        return null;
    }

    @Override
    public List<Skill> getSkillsForItem(Item i) {
        return null;
    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        return false;
    }

    @Override
    public CompoundNBT write(CompoundNBT to) {
        if (!this.activeSkills.isEmpty()) {
            ListNBT listnbt = new ListNBT();

            for (SkillData effectinstance : this.activeSkills.values()) {
                listnbt.add(effectinstance.write(new CompoundNBT()));
            }

            to.put("ActiveSkills", listnbt);
        }
        return to;
    }

    @Override
    public void read(CompoundNBT from) {
        if (from.contains("ActiveSkills", 9)) {
            activeSkills.clear();
            ListNBT listnbt = from.getList("ActiveSkills", 10);

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);
                SkillData effectinstance = SkillData.read(compoundnbt);
                if (effectinstance != null) {
                    this.activeSkills.put(effectinstance.getSkill(), effectinstance);
                }
            }
        }
    }
}
