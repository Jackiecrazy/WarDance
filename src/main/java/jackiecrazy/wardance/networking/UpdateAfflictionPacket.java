package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.status.Marks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateAfflictionPacket {
    int e;
    CompoundNBT icc;

    public UpdateAfflictionPacket(int ent, CompoundNBT c) {
        e = ent;
        icc = c;
    }

    public static class UpdateClientEncoder implements BiConsumer<UpdateAfflictionPacket, PacketBuffer> {

        @Override
        public void accept(UpdateAfflictionPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeCompoundTag(updateClientPacket.icc);
        }
    }

    public static class UpdateClientDecoder implements Function<PacketBuffer, UpdateAfflictionPacket> {

        @Override
        public UpdateAfflictionPacket apply(PacketBuffer packetBuffer) {
            return new UpdateAfflictionPacket(packetBuffer.readInt(), packetBuffer.readCompoundTag());
        }
    }

    public static class UpdateClientHandler implements BiConsumer<UpdateAfflictionPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAfflictionPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWorld world = Minecraft.getInstance().world;
                if (world != null) {
                    Entity entity = world.getEntityByID(updateClientPacket.e);
                    if (entity instanceof LivingEntity){
                        Marks.getCap((LivingEntity) entity).read(updateClientPacket.icc);
                    }
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
