package jackiecrazy.wardance.networking;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateClientPacket {
    int e;
    CompoundNBT icc;

    public UpdateClientPacket(int ent, CompoundNBT c) {
        e = ent;
        icc = c;
    }

    public static class UpdateClientEncoder implements BiConsumer<UpdateClientPacket, PacketBuffer> {

        @Override
        public void accept(UpdateClientPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeCompoundTag(updateClientPacket.icc);
        }
    }

    public static class UpdateClientDecoder implements Function<PacketBuffer, UpdateClientPacket> {

        @Override
        public UpdateClientPacket apply(PacketBuffer packetBuffer) {
            return new UpdateClientPacket(packetBuffer.readInt(), packetBuffer.readCompoundTag());
        }
    }

    public static class UpdateClientHandler implements BiConsumer<UpdateClientPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateClientPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                if (Minecraft.getInstance().world != null)
                    CombatData.getCap((LivingEntity) (Objects.requireNonNull(Minecraft.getInstance().world.getEntityByID(updateClientPacket.e)))).read(updateClientPacket.icc);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
