package jackiecrazy.wardance.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateAttackPacket {
    int e;
    int icc;

    public UpdateAttackPacket(int ent, int c) {
        e = ent;
        icc = c;
    }

    public static class UpdateAttackEncoder implements BiConsumer<UpdateAttackPacket, PacketBuffer> {

        @Override
        public void accept(UpdateAttackPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeInt(updateClientPacket.icc);
        }
    }

    public static class UpdateAttackDecoder implements Function<PacketBuffer, UpdateAttackPacket> {

        @Override
        public UpdateAttackPacket apply(PacketBuffer packetBuffer) {
            return new UpdateAttackPacket(packetBuffer.readInt(), packetBuffer.readInt());
        }
    }

    public static class UpdateAttackHandler implements BiConsumer<UpdateAttackPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAttackPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getEntityByID(updateClientPacket.e) instanceof LivingEntity)
                    ((LivingEntity) (Minecraft.getInstance().world.getEntityByID(updateClientPacket.e))).ticksSinceLastSwing = updateClientPacket.icc;
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
