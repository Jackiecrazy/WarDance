package jackiecrazy.wardance.networking;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.handlers.EntityHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
                ServerPlayerEntity sender = contextSupplier.get().getSender();
                if (sender != null && sender.level.getEntity(updateClientPacket.e) instanceof LivingEntity) {
                    EntityHandler.mustUpdate.put(sender, sender.level.getEntity(updateClientPacket.e));
                    CombatData.getCap(((LivingEntity) (sender.level.getEntity(updateClientPacket.e)))).serverTick();
                } else EntityHandler.mustUpdate.put(sender, null);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
