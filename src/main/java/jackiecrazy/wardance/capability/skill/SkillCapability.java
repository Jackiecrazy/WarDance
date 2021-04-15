package jackiecrazy.wardance.capability.skill;

import com.google.common.collect.Maps;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

public class SkillCapability implements ISkillCapability {
    private final Map<Skill, SkillData> activeSkills = Maps.newHashMap();
    private final Map<Skill, Float> coolingSkills = Maps.newHashMap();
    private final Map<ItemStack, Skill> stackBoundSkills = Maps.newHashMap();
    private final List<Skill> equippedSkill = new ArrayList<>(12);//4 innate, 4 empty mainhand, 4 empty offhand
    //weapon bound skills are added to their NBT instead of being handled here.
    //^not anymore. All skills are now self bound.
    private final WeakReference<LivingEntity> dude;
    private Queue<Skill> lastCast = new LinkedList<>();

    public SkillCapability(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    @Nullable
    @Override
    public Optional<SkillData> getActiveSkill(Skill s) {
        return Optional.of(activeSkills.get(s));
    }

    @Override
    public void activateSkill(SkillData d) {
        if (activeSkills.containsKey(d.getSkill())) {
            WarDance.LOGGER.warn("skill " + d + " is already active, overwriting.");
        }
        activeSkills.put(d.getSkill(), d);
    }

    @Override
    public void removeActiveSkill(Skill s) {
        SkillData sd = activeSkills.get(s);
        if (sd != null && dude.get() != null) {
            sd.getSkill().onEffectEnd(dude.get(), sd);
        }
        lastCast.add(s);
        while (lastCast.size() > 5)
            lastCast.remove();
        activeSkills.remove(s);
    }

    @Override
    public Map<Skill, SkillData> getActiveSkills() {
        return activeSkills;
    }

    @Override
    public void clearActiveSkills() {
        activeSkills.clear();
    }

    @Override
    public boolean isSkillActive(Skill skill) {
        return activeSkills.containsKey(skill);
    }

    @Override
    public boolean isTagActive(String tag) {
        for (Map.Entry<Skill, SkillData> e : activeSkills.entrySet()) {
            if (e.getKey().getTags(dude.get(), e.getValue()).contains(tag))
                return true;
        }
        return false;
    }

    @Override
    public void markSkillUsed(Skill s) {
        if (activeSkills.containsKey(s))
            activeSkills.get(s).setDuration(-1);
    }

    @Override
    public void setSkillCooldown(Skill s, float amount) {
        coolingSkills.put(s, amount);
    }

    @Override
    public void decrementSkillCooldown(Skill s, float amount) {
        if (!coolingSkills.containsKey(s)) return;
        float now = coolingSkills.get(s) - amount;
        if (now < 0) {
            coolingSkills.remove(s);
            s.onCooledDown(dude.get(), now);
        }
    }

    @Override
    public float getSkillCooldown(Skill s) {
        return coolingSkills.get(s);
    }

    @Override
    public Map<Skill, Float> getSkillCooldowns() {
        return coolingSkills;
    }

    @Override
    public void clearSkillCooldowns() {
        coolingSkills.clear();
    }

    @Override
    public List<Skill> getEquippedSkills() {
        return null;
    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        return skill.canCast(dude.get());
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

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }
}
