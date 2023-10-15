package jackiecrazy.wardance.capability.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
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
    private final List<SkillCategory> categories = new ArrayList<>();
    private final WeakReference<LivingEntity> dude;
    private final Queue<Skill> lastCast = new LinkedList<>();
    boolean sync = false, gatedSkills = false;
    int index = -1;
    private SkillStyle style;

    public SkillCapability(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    private SkillData nonNullGet(Skill d) {
        //if data doesn't have the skill tag, re-initialize the skill data.
        SkillData data = this.data.get(d);
        if (data == null) {
            this.data.put(d, new SkillData(d, 0, 0).setCaster(dude.get()));
            data = this.data.get(d);
        }
        return data;
    }

    @Override
    public boolean isSkillSelectable(Skill s) {
        final LivingEntity bruv = dude.get();
        if (bruv != null && s != null) {
            if (!s.isLearnable(bruv)) return false;
            return skillList.contains(s) == gatedSkills;
        }
        return true;
    }

    @Override
    public void setSkillSelectable(Skill s, boolean selectable) {
        if (s == null) return;
        if (selectable == gatedSkills) {
            if (!skillList.contains(s))
                skillList.add(s);
        } else skillList.remove(s);
        sync = true;
    }

    @Override
    public void setColorSelectable(SkillCategory s, boolean selectable) {

    }

    @Override
    public List<Skill> getSelectableList() {
        return skillList;
    }

    @Override
    public Optional<SkillData> getSkillData(Skill s) {
        if (s == null) return Optional.empty();//this occasionally happens and should not happen
        if (!isSkillEquipped(s)) return Optional.empty();//todo does this break anything?
        return Optional.of(nonNullGet(s));
    }

    @Override
    @Nullable
    public Skill getHolsteredSkill() {
        if (index < 0) return null;
        Skill ret = equippedSkill.get(index % equippedSkill.size());
        if (getSkillState(ret) != Skill.STATE.HOLSTERED) {//check to make sure you're not repeatedly casting a skill that is already active
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
        }
        sync = true;
    }

    @Override
    public boolean changeSkillState(Skill d, Skill.STATE to) {
        SkillData data = nonNullGet(d);
        if (data != null)
            if (to == Skill.STATE.ACTIVE && data.getState() == Skill.STATE.ACTIVE) {
                if (GeneralConfig.debug)
                    WarDance.LOGGER.debug("skill " + d + " is already active, overwriting.");
            }
        boolean update = d.onStateChange(dude.get(), data, data.getState(), to);
        data.markDirty();
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

    @Nullable
    @Override
    public SkillStyle getStyle() {
        return style;
    }

    @Override
    public void setStyle(SkillStyle style) {
        if (!isSkillSelectable(style)) return;
        if (!isSkillEquippable(style)) return;
        if (this.style != null) this.style.onUnequip(dude.get(), nonNullGet(style));
        this.style = style;
        if (style != null) style.onEquip(dude.get());
        sync = true;
    }

    @Override
    public boolean isTagActive(String tag) {
        for (SkillData e : new ArrayList<>(data.values())) {
            if (e.getState() == Skill.STATE.ACTIVE && e.getSkill().getTags().contains(tag))
                return true;
        }
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {
        for (Map.Entry<Skill, SkillData> e : data.entrySet()) {
            if (e.getKey().getTags().contains(tag))
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
    public boolean equipSkill(Skill skill) {
        if (!basicSanityCheck(skill)) return false;
        int index = 0, end = 5;
        if (skill.isPassive(dude.get())) {
            //magic number
            index = 5;
            end = 10;
        }
        List<Skill> section = equippedSkill.subList(index, end);
        if (section.contains(null)) {
            int nil = section.indexOf(null);
            section.set(nil, skill);
            skill.onEquip(dude.get());
            sync = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean replaceSkill(Skill from, Skill to) {
        if (equippedSkill.contains(from)) {
            int nil = equippedSkill.indexOf(from);
            equippedSkill.set(nil, null);
            if (!basicSanityCheck(to)) {
                equippedSkill.set(nil, from);
                return false;
            }
            from.onUnequip(dude.get(), nonNullGet(from));
            equippedSkill.set(nil, to);
            to.onEquip(dude.get());
            sync = true;
            return true;
        }
        return false;
    }

    @Override
    public void setEquippedSkillsAndUpdate(SkillStyle style, List<Skill> skills) {
        final LivingEntity caster = dude.get();
        for (Skill s : getEquippedSkills())
            if (s != null) {
                s.onUnequip(caster, nonNullGet(s));
            }
        setStyle(style);
        getAllSkillData().clear();
        setEquippedSkills(skills);
        for (Skill s : getEquippedSkills())
            if (s != null) {
                s.onEquip(caster);
            }
    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        if (skill == null) return false;
        if (!isSkillSelectable(skill)) return false;
        if (!isSkillEquippable(skill)) return false;
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
        for (Skill s : getEquippedSkillsAndStyle()) {
            if (s == null) {
                continue;
            }
            SkillData d = nonNullGet(s);
            if (d.getSkill().equippedTick(caster, d)) {
                d.markDirty();
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
            CompoundTag written = writeDirtyOnly();
            if (!written.isEmpty())
                CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) caster), new SyncSkillPacket(written));
        }
        sync = false;
    }

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }

    /**
     * split from isSkillSelectable to prevent bugs
     *
     * @param s
     * @return
     */
    private boolean isSkillEquippable(Skill s) {
        final LivingEntity bruv = dude.get();
        if (bruv != null && s != null) {
            for (Skill skill : getEquippedSkills()) {
                if (skill != null && !skill.isEquippableWith(s, bruv)) return false;
            }
        }
        return true;
    }

    private boolean basicSanityCheck(Skill insert) {
        if (insert == null) return true;
        if (equippedSkill.contains(insert)) return false;
        if (style != null && !style.isEquippableWith(insert, dude.get())) return false;
        if (!isSkillSelectable(insert)) return false;
        if (!isSkillEquippable(insert)) return false;
        return true;
    }

    private CompoundTag writeDirtyOnly() {
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
