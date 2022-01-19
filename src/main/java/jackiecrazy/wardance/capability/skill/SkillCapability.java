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
    boolean sync = false, gatedSkills = false;
    int index = -1;

    private SkillData nonNullGet(Skill s){
        //if data doesn't have the skill tag, re-initialize the skill data. TODO onEquip should ship with a default SkillData addition
        if(!data.containsKey(s))
            s.onEquip(dude.get());
        return data.get(s);
    }

    public SkillCapability(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
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
        return Optional.of(data.get(s));
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
        return equippedSkill.get(index % equippedSkill.size());
    }

    @Override
    public void holsterSkill(int index) {
        this.index = index;
        sync = true;
    }

    @Override
    public void changeSkillState(Skill d, Skill.STATE to) {
        final SkillData data = this.data.get(d);
        if(data==null)
        if (data.getState()== Skill.STATE.ACTIVE) {
            if (GeneralConfig.debug)
                WarDance.LOGGER.debug("skill " + d + " is already active, overwriting.");
        }
        d.onStateChange(dude.get(), data, data.getState(), to);
        data.setState(to);
        sync = true;
    }

    @Override
    public Map<Skill, SkillData> getAllSkillData() {
        return data;
    }

    @Override
    public Skill.STATE getCategoryState(SkillCategory skill) {
        for (Skill s : new ArrayList<>(data.keySet())) {
            if (s.getParentCategory().equals(skill)) return getSkillState(s);
        }
        return Skill.STATE.INACTIVE;
    }

    @Override
    public boolean isTagActive(String tag) {
        for (Skill e : new ArrayList<>(data.keySet())) {
            if (e.getTags(dude.get()).contains(tag))
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
        for (Skill s : skills) {
            if (s != null && s.isPassive(dude.get())) {
                s.checkAndCast(dude.get());
            }
        }
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
        data.clear();
        index = from.getInt("holster");
        gatedSkills = from.getBoolean("gamerule");//it's the easy way out...
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

    @Override
    public void update() {
        final LivingEntity caster = dude.get();
        if (caster == null) return;
        final boolean gate = caster.world.getGameRules().getBoolean(WarDance.GATED_SKILLS);
        sync |= gatedSkills != gate;
        gatedSkills = gate;
        for (SkillData d : data.values()) {
            if (d == null) continue;
            if (d.getSkill().equippedTick(caster, d.getState())) {
                sync = true;
            }
        }
        HashSet<SkillData> finish = new HashSet(getAllSkillData().values());
        for (SkillData s : finish) {
            if (s.getDuration() <= 0) {
                switch (s.getState()) {
                    //active ends. Add to cast history and forward to skill for individual handling.
                    case ACTIVE:
                        lastCast.add(s.getSkill());
                        while(lastCast.size()>5)
                            lastCast.remove();
                        changeSkillState(s.getSkill(), Skill.STATE.COOLING);
                        break;
                    //cooldown ends. Forward to skill for individual handling.
                    case COOLING:
                        changeSkillState(s.getSkill(), Skill.STATE.INACTIVE);
                        break;
                        //the other two cases shouldn't be necessary...?
                }
            }
        }
        finish.clear();
        if (sync && caster instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) caster), new SyncSkillPacket(this.write()));
        sync = false;
    }

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }
}
