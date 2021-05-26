package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateSkillSelectionPacket {
    private static final ResourceLocation DUMMY = new ResourceLocation("wardance:thisisadummy");
    private static final Comparator<Skill> comparator = (o1, o2) -> {
        if (o1 == o2 && o2 == null) return 0;
        if (o1 == null || o1.getRegistryName() == null) return -1;
        if (o2 == null || o2.getRegistryName() == null) return 1;
        return o1 == o2 ? 0 : o1.getRegistryName().compareTo(o2.getRegistryName());
    };
    private List<Skill> l;

    public UpdateSkillSelectionPacket(List<Skill> list) {
        l = list;
    }

    public static class UpdateSkillEncoder implements BiConsumer<UpdateSkillSelectionPacket, PacketBuffer> {

        @Override
        public void accept(UpdateSkillSelectionPacket updateSkillPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateSkillPacket.l.size());
            for (Skill s : updateSkillPacket.l)
                if (s != null)
                    packetBuffer.writeResourceLocation(s.getRegistryName());
                else packetBuffer.writeResourceLocation(DUMMY);
        }
    }

    public static class UpdateSkillDecoder implements Function<PacketBuffer, UpdateSkillSelectionPacket> {

        @Override
        public UpdateSkillSelectionPacket apply(PacketBuffer packetBuffer) {
            int size = packetBuffer.readInt();
            List<Skill> read = new ArrayList<>(size);
            for (int a = 0; a < size; a++) {
                read.add(a, Skill.getSkill(packetBuffer.readResourceLocation()));
            }
            return new UpdateSkillSelectionPacket(read);
        }
    }

    public static class UpdateSkillHandler implements BiConsumer<UpdateSkillSelectionPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateSkillSelectionPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayerEntity sender = contextSupplier.get().getSender();
                if (sender != null) {
                    final ISkillCapability cap = CasterData.getCap(sender);
                    List<Skill> prev = cap.getEquippedSkills();
                    List<Skill> now = new ArrayList<>(updateSkillPacket.l);
                    prev.sort(comparator);
                    now.sort(comparator);
                    if (!prev.equals(now)) {
                        for (Skill s : cap.getEquippedSkills())
                            if (s != null) {
                                s.onEffectEnd(sender, new SkillData(s, 0));
                            }
                        cap.clearActiveSkills();
                        cap.setEquippedSkills(updateSkillPacket.l);
                        for (Skill s : cap.getEquippedSkills())
                            if (s != null) {
                                s.onEffectEnd(sender, new SkillData(s, 0));
                            }
                    } else
                        cap.setEquippedSkills(updateSkillPacket.l);
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
