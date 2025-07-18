package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DodgePacket {
    int direction;

    public DodgePacket(int dir) {
        direction = dir;
    }

    public static class DodgeEncoder implements BiConsumer<DodgePacket, FriendlyByteBuf> {

        @Override
        public void accept(DodgePacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.direction);
        }
    }

    public static class DodgeDecoder implements Function<FriendlyByteBuf, DodgePacket> {

        @Override
        public DodgePacket apply(FriendlyByteBuf packetBuffer) {
            return new DodgePacket(packetBuffer.readInt());
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
