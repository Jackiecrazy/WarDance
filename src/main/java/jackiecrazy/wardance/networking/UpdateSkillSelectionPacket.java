package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.handlers.EntityHandler;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateSkillSelectionPacket {
    private static final ResourceLocation DUMMY = new ResourceLocation("wardance:thisisadummy");
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
                    CasterData.getCap(sender).clearActiveSkills();
                    CasterData.getCap(sender).setEquippedSkills(updateSkillPacket.l);
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
