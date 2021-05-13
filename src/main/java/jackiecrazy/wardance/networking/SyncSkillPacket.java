package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncSkillPacket {
    CompoundNBT icc;

    public SyncSkillPacket(CompoundNBT c) {
        icc = c;
    }

    public static class SyncSkillEncoder implements BiConsumer<SyncSkillPacket, PacketBuffer> {

        @Override
        public void accept(SyncSkillPacket SyncSkillPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeCompoundTag(SyncSkillPacket.icc);
        }
    }

    public static class SyncSkillDecoder implements Function<PacketBuffer, SyncSkillPacket> {

        @Override
        public SyncSkillPacket apply(PacketBuffer packetBuffer) {
            return new SyncSkillPacket(packetBuffer.readCompoundTag());
        }
    }

    public static class SyncSkillHandler implements BiConsumer<SyncSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncSkillPacket SyncSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ClientPlayerEntity p = Minecraft.getInstance().player;
                if (p != null) {
                    CasterData.getCap(p).read(SyncSkillPacket.icc);

                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
