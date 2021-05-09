package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DodgePacket {
    int direction;
    boolean isRoll;

    public DodgePacket(int dir, boolean roll) {
        direction = dir;
        isRoll = roll;
    }

    public static class DodgeEncoder implements BiConsumer<DodgePacket, PacketBuffer> {

        @Override
        public void accept(DodgePacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.direction);
            packetBuffer.writeBoolean(updateClientPacket.isRoll);
        }
    }

    public static class DodgeDecoder implements Function<PacketBuffer, DodgePacket> {

        @Override
        public DodgePacket apply(PacketBuffer packetBuffer) {
            return new DodgePacket(packetBuffer.readInt(), packetBuffer.readBoolean());
        }
    }

    public static class DodgeHandler implements BiConsumer<DodgePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(DodgePacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                MovementUtils.attemptDodge(Objects.requireNonNull(contextSupplier.get().getSender()), updateClientPacket.direction);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
