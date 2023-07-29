package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.status.Marks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateMarkPacket {
    int e;
    CompoundTag icc;

    public UpdateMarkPacket(int ent, CompoundTag c) {
        e = ent;
        icc = c;
    }

    public static class UpdateClientEncoder implements BiConsumer<UpdateMarkPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateMarkPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeNbt(updateClientPacket.icc);
        }
    }

    public static class UpdateClientDecoder implements Function<FriendlyByteBuf, UpdateMarkPacket> {

        @Override
        public UpdateMarkPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateMarkPacket(packetBuffer.readInt(), packetBuffer.readNbt());
        }
    }

    public static class UpdateClientHandler implements BiConsumer<UpdateMarkPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateMarkPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(updateClientPacket.e);
                    if (entity instanceof LivingEntity){
                        Marks.getCap((LivingEntity) entity).read(updateClientPacket.icc);
                    }
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
