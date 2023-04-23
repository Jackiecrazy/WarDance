package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

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
    private SkillStyle style;

    public UpdateSkillSelectionPacket(SkillStyle style, List<Skill> list) {
        l = list;
        this.style = style;
    }

    public static class UpdateSkillEncoder implements BiConsumer<UpdateSkillSelectionPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateSkillSelectionPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeResourceLocation(updateSkillPacket.style == null ? DUMMY : updateSkillPacket.style.getRegistryName());
            packetBuffer.writeInt(updateSkillPacket.l.size());
            for (Skill s : updateSkillPacket.l)
                if (s != null)
                    packetBuffer.writeResourceLocation(s.getRegistryName());
                else packetBuffer.writeResourceLocation(DUMMY);
        }
    }

    public static class UpdateSkillDecoder implements Function<FriendlyByteBuf, UpdateSkillSelectionPacket> {

        @Override
        public UpdateSkillSelectionPacket apply(FriendlyByteBuf packetBuffer) {
            SkillStyle ss = SkillStyle.getStyle(packetBuffer.readResourceLocation());
            int size = packetBuffer.readInt();
            List<Skill> read = new ArrayList<>(size);
            for (int a = 0; a < size; a++) {
                read.add(a, Skill.getSkill(packetBuffer.readResourceLocation()));
            }
            return new UpdateSkillSelectionPacket(ss, read);
        }
    }

    public static class UpdateSkillHandler implements BiConsumer<UpdateSkillSelectionPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateSkillSelectionPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null) {
                    final ISkillCapability cap = CasterData.getCap(sender);
                    List<Skill> prev = cap.getEquippedSkills();
                    List<Skill> now = new ArrayList<>(updateSkillPacket.l);
                    prev.sort(comparator);
                    now.sort(comparator);
                    if (!prev.equals(now) || updateSkillPacket.style != cap.getStyle()) {
                        for (Skill s : cap.getEquippedSkills())
                            if (s != null) {
                                s.onUnequip(sender, new SkillData(s, 0).setCaster(sender));
                            }
                        cap.setStyle(updateSkillPacket.style);
                        cap.getAllSkillData().clear();
                        cap.setEquippedSkills(updateSkillPacket.l);
                        for (Skill s : cap.getEquippedSkills())
                            if (s != null) {
                                s.onEquip(sender);
                            }
                    } else {
                        cap.setStyle(updateSkillPacket.style);
                        cap.setEquippedSkills(updateSkillPacket.l);
                    }
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
