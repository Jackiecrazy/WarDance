package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

public class SkillCapability implements ISkillCapability {
    private final HashMap<Skill, SkillData> data = new HashMap<>();
    private final List<Skill> equippedSkill = new ArrayList<>(10);
    private final List<Skill> skillList = new ArrayList<>();
    private final WeakReference<LivingEntity> dude;
    private final Queue<Skill> lastCast = new LinkedList<>();
    boolean sync = false, fastSync = false, gatedSkills = false;
    int index = -1;

    public SkillCapability(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    private SkillData nonNullGet(Skill d) {
        //if data doesn't have the skill tag, re-initialize the skill data.
        SkillData data = this.data.get(d);
        if (data == null) {
            this.data.put(d, new SkillData(d, 0, 0));
            data = this.data.get(d);
        }
        return data;
    }

    @Override
    public boolean isSkillSelectable(Skill s) {
        final LivingEntity bruv = dude.get();
        if (bruv != null) {
            if (!s.isSelectable(bruv)) return false;
            return skillList.contains(s) == gatedSkills;
        }
        return true;
    }

    @Override
    public void setSkillSelectable(Skill s, boolean selectable) {
        if (selectable == gatedSkills) {
            if (!skillList.contains(s))
                skillList.add(s);
        } else skillList.remove(s);
        sync = true;
    }

    @Override
    public List<Skill> getSelectableList() {
        return skillList;
    }

    @Override
    public Optional<SkillData> getSkillData(Skill s) {
        return Optional.ofNullable(data.get(s));
    }

    @Override
    public Skill.STATE getSkillState(Skill s) {
        if (!data.containsKey(s)) return Skill.STATE.INACTIVE;
        return data.get(s).getState();
    }

    @Override
    @Nullable
    public Skill getHolsteredSkill() {
        if (index < 0) return null;
        Skill ret= equippedSkill.get(index % equippedSkill.size());
        if(getSkillState(ret)!= Skill.STATE.HOLSTERED){
            ret=null;
        }
        return ret;
    }

    @Override
    public void holsterSkill(int index) {
        for (Skill s : equippedSkill)
            if (s != null && getSkillState(s) == Skill.STATE.HOLSTERED)
                changeSkillState(s, Skill.STATE.INACTIVE);
        this.index = index;
        Skill to = equippedSkill.get(index % equippedSkill.size());
        if (to != null) {
            changeSkillState(to, Skill.STATE.HOLSTERED);
            nonNullGet(to).markDirty();
            fastSync = true;
        }
        sync=true;
    }

    @Override
    public boolean changeSkillState(Skill d, Skill.STATE to) {
        SkillData data = nonNullGet(d);
        if (data != null)
            if (data.getState() == Skill.STATE.ACTIVE) {
                if (GeneralConfig.debug)
                    WarDance.LOGGER.debug("skill " + d + " is already active, overwriting.");
            }
        boolean update = d.onStateChange(dude.get(), data, data.getState(), to);
        data.markDirty();
        fastSync = true;
        return update;
    }

    @Override
    public Map<Skill, SkillData> getAllSkillData() {
        return data;
    }

    @Override
    public Skill.STATE getCategoryState(SkillCategory skill) {
        for (Skill s : new ArrayList<>(data.keySet())) {
            if (s != null && s.getParentCategory().equals(skill)) return getSkillState(s);
        }
        return Skill.STATE.INACTIVE;
    }

    @Override
    public boolean isTagActive(String tag) {
        for (SkillData e : new ArrayList<>(data.values())) {
            if (e.getState() == Skill.STATE.ACTIVE && e.getSkill().getTags(dude.get()).contains(tag))
                return true;
        }
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {
        for (Map.Entry<Skill, SkillData> e : data.entrySet()) {
            if (e.getKey().getTags(dude.get()).contains(tag))
                changeSkillState(e.getKey(), Skill.STATE.INACTIVE);
        }
    }

    @Override
    public Skill getEquippedVariation(SkillCategory other) {
        for (Skill k : new ArrayList<>(equippedSkill))
            if (k != null && k.getParentCategory() == other) return k;
        return null;
    }

    @Override
    public List<Skill> getEquippedSkills() {
        return equippedSkill;
    }

    @Override
    public void setEquippedSkills(List<Skill> skills) {
        equippedSkill.clear();
        equippedSkill.addAll(skills);
        sync = true;
    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        if (skill == null) return false;
        if (!isSkillSelectable(skill)) return false;
        if (!equippedSkill.contains(skill)) return false;
        return skill.castingCheck(dude.get()) == Skill.CastStatus.ALLOWED || skill.castingCheck(dude.get()) == Skill.CastStatus.ACTIVE;
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT to = new CompoundNBT();
        to.putInt("holster", index);
        to.putBoolean("gamerule", gatedSkills);
        if (!this.data.isEmpty()) {
            ListNBT listnbt = new ListNBT();

            for (SkillData effectinstance : this.data.values()) {
                listnbt.add(effectinstance.write(new CompoundNBT()));
            }

            to.put("skillData", listnbt);
        }
        for (int a = 0; a < equippedSkill.size(); a++)
            if (equippedSkill.get(a) != null)
                to.putString("equippedSkill" + a, equippedSkill.get(a).getRegistryName().toString());
        ListNBT str = new ListNBT();
        for (Skill add : skillList) {
            str.add(StringNBT.valueOf(add.getRegistryName().toString()));
        }
        to.put("randomList", str);
        return to;
    }

    @Override
    public void read(CompoundNBT from) {
        if (!from.getBoolean("fast")) {
            data.clear();
            index = from.getInt("holster");
            gatedSkills = from.getBoolean("gamerule");//it's the easy way out...
            Skill[] als = new Skill[10];
            for (int a = 0; a < als.length; a++)
                if (from.contains("equippedSkill" + a))
                    als[a] = (Skill.getSkill(from.getString("equippedSkill" + a)));
            //if (from.getBoolean("skillListDirty")) {
            skillList.clear();
            if (from.contains("randomList", Constants.NBT.TAG_LIST)) {
                ListNBT list = from.getList("randomList", Constants.NBT.TAG_STRING);
                for (Object s : list.toArray()) {
                    if (s instanceof StringNBT && Skill.getSkill(((StringNBT) s).getString()) != null)
                        skillList.add(Skill.getSkill(((StringNBT) s).getString()));
                }
            }
            //}
            equippedSkill.clear();
            equippedSkill.addAll(Arrays.asList(als));
        }
        if (from.contains("skillData", 9)) {
            ListNBT listnbt = from.getList("skillData", 10);
            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);
                SkillData data = SkillData.read(compoundnbt);
                if (data != null) {
                    this.data.put(data.getSkill(), data);
                }
            }
        }
    }

    private CompoundNBT fastWrite() {
        CompoundNBT to = new CompoundNBT();
        if (!this.data.isEmpty()) {
            ListNBT listnbt = new ListNBT();
            for (SkillData effectinstance : this.data.values()) {
                if (effectinstance._isDirty()) {
                    listnbt.add(effectinstance.write(new CompoundNBT()));
                }
            }
            if (!listnbt.isEmpty()) {
                to.put("skillData", listnbt);
                to.putBoolean("fast", true);
            }
        }
        return to;
    }

    @Override
    public void update() {
        final LivingEntity caster = dude.get();
        if (caster == null) return;
        final boolean gate = caster.world.getGameRules().getBoolean(WarDance.GATED_SKILLS);
        sync |= gatedSkills != gate;
        gatedSkills = gate;
        for (SkillData d : data.values()) {
            if (d == null) continue;
            if (d.getSkill().equippedTick(caster, d)) {
                d.markDirty();
                fastSync = true;
            }
        }
        for (SkillData s : getAllSkillData().values()) {
            //active ends. Add to cast history and forward to skill for individual handling.
            if (s.getDuration() < 0 && s.getState() == Skill.STATE.ACTIVE) {
                lastCast.add(s.getSkill());
                while (lastCast.size() > 5)
                    lastCast.remove();
                changeSkillState(s.getSkill(), Skill.STATE.COOLING);
                //cooldown ends. Forward to skill for individual handling.
            } else if (s.getDuration() <= 0 && s.getState() == Skill.STATE.COOLING) {
                changeSkillState(s.getSkill(), Skill.STATE.INACTIVE);

            }
        }
        if (sync && caster instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) caster), new SyncSkillPacket(this.write()));
        else if (caster instanceof ServerPlayerEntity) {
            CompoundNBT written = fastWrite();
            if (!written.isEmpty())
                CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) caster), new SyncSkillPacket(written));
        }
        sync = false;
    }

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }
}
