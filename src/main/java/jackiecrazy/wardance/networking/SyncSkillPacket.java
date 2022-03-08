package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
            packetBuffer.writeNbt(SyncSkillPacket.icc);
        }
    }

    public static class SyncSkillDecoder implements Function<PacketBuffer, SyncSkillPacket> {

        @Override
        public SyncSkillPacket apply(PacketBuffer packetBuffer) {
            return new SyncSkillPacket(packetBuffer.readNbt());
        }
    }

    public static class SyncSkillHandler implements BiConsumer<SyncSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncSkillPacket SyncSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Handle.handleClient(SyncSkillPacket.icc));

            });
            contextSupplier.get().setPacketHandled(true);
        }
    }

    public static class Handle {
        public static DistExecutor.SafeRunnable handleClient(CompoundNBT icc) {
            return new DistExecutor.SafeRunnable() {
                @Override
                public void run() {
                    PlayerEntity player = (PlayerEntity) Minecraft.getInstance().player;
                    if (player == null) return;
                    CasterData.getCap(player).read(icc);
                }
            };
        }
    }
}
