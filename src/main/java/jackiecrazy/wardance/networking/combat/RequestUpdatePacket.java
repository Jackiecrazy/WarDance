package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.handlers.EntityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestUpdatePacket {
    int e;

    public RequestUpdatePacket(int ent) {
        e = ent;
    }

    public static class Encoder implements BiConsumer<RequestUpdatePacket, FriendlyByteBuf> {

        @Override
        public void accept(RequestUpdatePacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.e);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, RequestUpdatePacket> {

        @Override
        public RequestUpdatePacket apply(FriendlyByteBuf packetBuffer) {
            return new RequestUpdatePacket(packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<RequestUpdatePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestUpdatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null && sender.level().getEntity(packet.e) instanceof LivingEntity entity) {
                    EntityHandler.mustUpdate.put(sender, entity);
                    CombatData.getCap(entity).serverTick();
                    Marks.getCap(entity).sync();
                } else EntityHandler.mustUpdate.put(sender, null);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
