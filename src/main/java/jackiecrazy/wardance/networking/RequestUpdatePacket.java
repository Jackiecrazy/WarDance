package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.CombatData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestUpdatePacket {
    int e;

    public RequestUpdatePacket(int ent) {
        e = ent;
    }

    public static class RequestUpdateEncoder implements BiConsumer<RequestUpdatePacket, PacketBuffer> {

        @Override
        public void accept(RequestUpdatePacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
        }
    }

    public static class RequestUpdateDecoder implements Function<PacketBuffer, RequestUpdatePacket> {

        @Override
        public RequestUpdatePacket apply(PacketBuffer packetBuffer) {
            return new RequestUpdatePacket(packetBuffer.readInt());
        }
    }

    public static class RequestUpdateHandler implements BiConsumer<RequestUpdatePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestUpdatePacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                CombatData.getCap(((LivingEntity) (Objects.requireNonNull(Objects.requireNonNull(contextSupplier.get().getSender()).world.getEntityByID(updateClientPacket.e))))).update();
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
