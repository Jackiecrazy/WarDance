package jackiecrazy.wardance.capability.status;

import com.google.common.collect.Maps;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateMarkPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.*;

public class Mark implements IMark {
    private final Map<Skill, SkillData> statuus = Maps.newHashMap();
    private final WeakReference<LivingEntity> dude;
    boolean sync = false;

    public Mark(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    @Override
    public Optional<SkillData> getActiveMark(Skill s) {
        return Optional.ofNullable(statuus.get(s));
    }

    @Override
    public void mark(SkillData d) {
        if (dude.get() == null) return;
        if (statuus.containsKey(d.getSkill())) {
            if (GeneralConfig.debug)
                WarDance.LOGGER.warn("status " + d + " is already active, merging according to rules.");
        }
        SkillData sd = d.getSkill().onMarked(d.getCaster(dude.get().level()), dude.get(), d, statuus.get(d.getSkill()));
        if (sd != null)
            statuus.put(d.getSkill(), sd);
        else statuus.remove(d.getSkill());
        sync = true;
    }

    @Override
    public void removeMark(Skill s) {
        SkillData sd = statuus.get(s);
        LivingEntity victim = dude.get();
        if (sd != null && victim != null) {
            sd.getSkill().onMarkEnd(sd.getCaster(victim.level()), victim, sd);
        }
        sync = true;
        statuus.remove(s);
    }

    @Override
    public Map<Skill, SkillData> getActiveMarks() {
        return statuus;
    }

    @Override
    public void clearMarks() {
        for (Skill s : new HashSet<>(statuus.keySet())) {
            removeMark(s);
        }
    }

    @Override
    public boolean isMarked(Skill skill) {
        return statuus.containsKey(skill);
    }

    @Override
    public boolean isMarked(SkillArchetype skill) {
        for (Skill s : statuus.keySet())
            if (s != null && s.getArchetype() == skill) return true;
        return false;
    }

    @Override
    public CompoundTag write() {
        CompoundTag to = new CompoundTag();
        if (!this.statuus.isEmpty()) {
            ListTag listnbt = new ListTag();

            for (SkillData effectinstance : this.statuus.values()) {
                listnbt.add(effectinstance.write(new CompoundTag()));
            }

            to.put("ActiveAfflictions", listnbt);
        }
        return to;
    }

    @Override
    public void read(CompoundTag from) {
        statuus.clear();
        if (from.contains("ActiveAfflictions", 9)) {
            ListTag listnbt = from.getList("ActiveAfflictions", 10);

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundTag compoundnbt = listnbt.getCompound(i);
                SkillData effectinstance = SkillData.read(compoundnbt);
                if (effectinstance != null) {
                    this.statuus.put(effectinstance.getSkill(), effectinstance);
                }
            }
        }
    }

    @Override
    public void update() {
        final LivingEntity ticker = dude.get();
        if (ticker == null) return;
        final Collection<SkillData> active = new ArrayList<>(getActiveMarks().values());
        for (SkillData cd : active) {
            final LivingEntity caster = cd.getCaster(ticker.level());
            if (cd.getSkill().markTick(caster, ticker, cd)) sync = true;
            if (cd._isDirty()) sync = true;
            if (cd.getDuration() <= 0) {
                removeMark(cd.getSkill());
                sync = true;
            }
        }
        if (sync)
            sync();
    }

    @Override
    public void sync() {
        final LivingEntity ticker = dude.get();
        if (ticker == null) return;
        if (ticker instanceof ServerPlayer) {
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) ticker), new UpdateMarkPacket(ticker.getId(), this.write()));
        }
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> ticker), new UpdateMarkPacket(ticker.getId(), this.write()));
        sync = false;
    }
}
