package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.CombatData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateAttackPacket {
    int e;
    float icc;

    public UpdateAttackPacket(int ent, float c) {
        e = ent;
        icc = c;
    }

    public static class UpdateAttackEncoder implements BiConsumer<UpdateAttackPacket, PacketBuffer> {

        @Override
        public void accept(UpdateAttackPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeFloat(updateClientPacket.icc);
        }
    }

    public static class UpdateAttackDecoder implements Function<PacketBuffer, UpdateAttackPacket> {

        @Override
        public UpdateAttackPacket apply(PacketBuffer packetBuffer) {
            return new UpdateAttackPacket(packetBuffer.readInt(), packetBuffer.readFloat());
        }
    }

    public static class UpdateAttackHandler implements BiConsumer<UpdateAttackPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAttackPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                if (Minecraft.getInstance().world != null)
                    ((LivingEntity) (Objects.requireNonNull(Minecraft.getInstance().world.getEntityByID(updateClientPacket.e)))).ticksSinceLastSwing= updateClientPacket.e;
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
