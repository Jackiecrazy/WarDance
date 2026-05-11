package jackiecrazy.wardance.networking.movement;

import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AerialModePacket {
    private final boolean toggle;

    public AerialModePacket(boolean on) {
        toggle = on;
    }

    public static class Encoder implements BiConsumer<AerialModePacket, FriendlyByteBuf> {

        @Override
        public void accept(AerialModePacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.toggle);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, AerialModePacket> {

        @Override
        public AerialModePacket apply(FriendlyByteBuf packetBuffer) {
            return new AerialModePacket(packetBuffer.readBoolean());
        }
    }

    public static class
    Handler implements BiConsumer<AerialModePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(AerialModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                IAerialMode cap = AerialModeData.getCap(Objects.requireNonNull(contextSupplier.get().getSender()));
                cap.setAerialMode(packet.toggle);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
