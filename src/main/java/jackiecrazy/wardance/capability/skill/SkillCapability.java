package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.skill.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

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
    private SkillStyle style;

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
            for (Skill skill : skillList) {
                if (!skill.isEquippableWith(s, bruv)) return false;
            }
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
    @Nullable
    public Skill getHolsteredSkill() {
        if (index < 0) return null;
        Skill ret = equippedSkill.get(index % equippedSkill.size());
        if (getSkillState(ret) != Skill.STATE.HOLSTERED) {
            ret = null;
        }
        return ret;
    }

    @Override
    public void holsterSkill(int index) {
        Skill to = equippedSkill.get(index % equippedSkill.size());
        if (to != null && to.castingCheck(dude.get()) != Skill.CastStatus.ALLOWED) return;
        for (Skill s : equippedSkill)
            if (s != null && getSkillState(s) == Skill.STATE.HOLSTERED)
                changeSkillState(s, Skill.STATE.INACTIVE);
        this.index = index;
        if (to != null) {
            changeSkillState(to, Skill.STATE.HOLSTERED);
            nonNullGet(to).markDirty();
            fastSync = true;
        }
        sync = true;
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
    public Skill.STATE getSkillState(Skill s) {
        if (!data.containsKey(s)) return Skill.STATE.INACTIVE;
        return data.get(s).getState();
    }

    @Override
    public Skill.STATE getArchetypeState(SkillArchetype skill) {
        for (Skill s : new ArrayList<>(data.keySet())) {
            if (s != null && s.getArchetype().equals(skill)) return getSkillState(s);
        }
        return Skill.STATE.INACTIVE;
    }

    @Override
    public Skill getEquippedVariation(SkillArchetype other) {
        for (Skill k : new ArrayList<>(equippedSkill))
            if (k != null && k.getArchetype() == other) return k;
        return null;
    }

    @Nullable
    @Override
    public SkillStyle getStyle() {
        return style;
    }

    @Override
    public void setStyle(SkillStyle style) {
        this.style = style;
        equippedSkill.clear();
        sync = true;
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
    public List<SkillCategory> getEquippedColors() {
        ArrayList<SkillCategory> ret = new ArrayList<>();
        for (Skill k : new ArrayList<>(equippedSkill)) {
            if (k != null && !ret.contains(k.getCategory())) ret.add(k.getCategory());
        }
        return ret;
    }

    @Override
    public List<Skill> getEquippedSkills() {
        return equippedSkill;
    }

    @Override
    public void setEquippedSkills(List<Skill> skills) {
        equippedSkill.clear();
        for (Skill ski : skills) {
            if (basicSanityCheck(ski)) equippedSkill.add(ski);
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
    public CompoundTag write() {
        CompoundTag to = new CompoundTag();
        to.putInt("holster", index);
        to.putBoolean("gamerule", gatedSkills);
        if (style != null)
            to.putString("style", style.getRegistryName().toString());
        if (!this.data.isEmpty()) {
            ListTag listnbt = new ListTag();

            for (SkillData effectinstance : this.data.values()) {
                listnbt.add(effectinstance.write(new CompoundTag()));
            }

            to.put("skillData", listnbt);
        }
        for (int a = 0; a < equippedSkill.size(); a++)
            if (equippedSkill.get(a) != null)
                to.putString("equippedSkill" + a, equippedSkill.get(a).getRegistryName().toString());
        ListTag str = new ListTag();
        for (Skill add : skillList) {
            str.add(StringTag.valueOf(add.getRegistryName().toString()));
        }
        to.put("randomList", str);
        return to;
    }

    @Override
    public void read(CompoundTag from) {
        if (!from.getBoolean("fast")) {
            data.clear();
            index = from.getInt("holster");
            gatedSkills = from.getBoolean("gamerule");//it's the easy way out...
            if (Skill.getSkill(from.getString("style")) instanceof SkillStyle ss)
                style = ss;
            Skill[] als = new Skill[10];
            for (int a = 0; a < als.length; a++)
                if (from.contains("equippedSkill" + a))
                    als[a] = (Skill.getSkill(from.getString("equippedSkill" + a)));
            //if (from.getBoolean("skillListDirty")) {
            skillList.clear();
            if (from.contains("randomList", Tag.TAG_LIST)) {
                ListTag list = from.getList("randomList", Tag.TAG_STRING);
                for (Object s : list.toArray()) {
                    if (s instanceof StringTag && Skill.getSkill(((StringTag) s).getAsString()) != null)
                        skillList.add(Skill.getSkill(((StringTag) s).getAsString()));
                }
            }
            //}
            equippedSkill.clear();
            equippedSkill.addAll(Arrays.asList(als));
        }
        if (from.contains("skillData", 9)) {
            ListTag listnbt = from.getList("skillData", 10);
            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundTag compoundnbt = listnbt.getCompound(i);
                SkillData data = SkillData.read(compoundnbt);
                if (data != null) {
                    this.data.put(data.getSkill(), data);
                }
            }
        }
    }

    @Override
    public void update() {
        final LivingEntity caster = dude.get();
        if (caster == null) return;
        final boolean gate = caster.level.getGameRules().getBoolean(WarDance.GATED_SKILLS);
        sync |= gatedSkills != gate;
        gatedSkills = gate;
        for (SkillData d : data.values()) {
            if (d == null || d.getSkill() == null) {
                continue;
            }
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
        if (sync && caster instanceof ServerPlayer)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) caster), new SyncSkillPacket(this.write()));
        else if (caster instanceof ServerPlayer) {
            CompoundTag written = fastWrite();
            if (!written.isEmpty())
                CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) caster), new SyncSkillPacket(written));
        }
        sync = false;
    }

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }

    private boolean basicSanityCheck(Skill insert) {
        if (skillList.contains(insert)) return false;
        if (style != null && !style.isEquippableWith(insert, dude.get())) return false;
        return true;
    }

    private CompoundTag fastWrite() {
        CompoundTag to = new CompoundTag();
        if (!this.data.isEmpty()) {
            ListTag listnbt = new ListTag();
            for (SkillData effectinstance : this.data.values()) {
                if (effectinstance._isDirty()) {
                    listnbt.add(effectinstance.write(new CompoundTag()));
                }
            }
            if (!listnbt.isEmpty()) {
                to.put("skillData", listnbt);
                to.putBoolean("fast", true);
            }
        }
        return to;
    }
}
