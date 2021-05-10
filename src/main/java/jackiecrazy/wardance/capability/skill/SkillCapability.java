package jackiecrazy.wardance.capability.skill;

import com.google.common.collect.Maps;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

public class SkillCapability implements ISkillCapability {
    private final Map<Skill, SkillData> activeSkills = Maps.newHashMap();
    private final Map<Skill, Float> coolingSkills = Maps.newHashMap();
    private final List<Skill> equippedSkill = new ArrayList<>(12);
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
            if (dude.get() instanceof PlayerEntity)
                ((PlayerEntity) dude.get()).sendStatusMessage(new StringTextComponent(s.getRegistryName() + " has entered cooldown"), true);
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
            if (e.getKey().getTags(dude.get()).contains(tag))
                return true;
        }
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {
        for (Map.Entry<Skill, SkillData> e : activeSkills.entrySet()) {
            if (e.getKey().getTags(dude.get()).contains(tag))
                markSkillUsed(e.getKey());
        }
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
    public boolean isSkillCoolingDown(Skill s) {
        return coolingSkills.containsKey(s);
    }

    @Override
    public void decrementSkillCooldown(Skill s, float amount) {
        if (!coolingSkills.containsKey(s)) return;
        float old = coolingSkills.get(s);
        coolingSkills.put(s, old - amount);
    }

    @Override
    public void coolSkill(Skill s) {
        s.onCooledDown(dude.get(), coolingSkills.remove(s));
        if (dude.get() instanceof PlayerEntity)
            ((PlayerEntity) dude.get()).sendStatusMessage(new StringTextComponent(s.getRegistryName() + " has cooled down"), true);
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
        return equippedSkill;
    }

    @Override
    public void setEquippedSkills(List<Skill> skills) {
        equippedSkill.clear();
        equippedSkill.addAll(skills);
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
        for (int a = 0; a < equippedSkill.size(); a++)
            if (equippedSkill.get(a) != null)
                to.putString("equippedSkill" + a, equippedSkill.get(a).getRegistryName().toString());
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
        ArrayList<Skill> als = new ArrayList<>();
        for (int a = 0; a < equippedSkill.size(); a++)
            if (from.contains("equippedSkill" + a))
                als.add(a, Skill.getSkill("equippedSkill" + a));
        setEquippedSkills(als);
    }

    @Override
    public void update() {
        getActiveSkills().replaceAll((k, v) -> v.setDuration(v.getDuration() - (k.getTags(dude.get()).contains(ProcPoint.countdown) ? 1 : 0)));
        getSkillCooldowns().replaceAll((k, v) -> v -= k.getTags(dude.get()).contains(ProcPoint.recharge_time) ? 1 : 0);
        HashSet<Skill> finish = new HashSet<>();
        for (Map.Entry<Skill, SkillData> cd : getActiveSkills().entrySet()) {
            if (cd.getValue().getDuration() < 0) finish.add(cd.getKey());
        }
        for (Skill s : finish)
            removeActiveSkill(s);
        finish.clear();
        for (Map.Entry<Skill, Float> cd : getSkillCooldowns().entrySet()) {
            if (cd.getValue() < 0) finish.add(cd.getKey());
        }
        for (Skill s : finish)
            coolSkill(s);
    }

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }
}
