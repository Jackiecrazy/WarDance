package jackiecrazy.wardance.networking;

import jackiecrazy.footwork.capability.resources.CombatData;
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

public class UpdateClientPassengerPacket {
    int e;
    CompoundTag icc;

    public UpdateClientPassengerPacket(int ent, CompoundTag c) {
        e = ent;
        icc = c;
    }

    public static class UpdateClientEncoder implements BiConsumer<UpdateClientPassengerPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateClientPassengerPacket updateClientResourcePacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientResourcePacket.e);
            packetBuffer.writeNbt(updateClientResourcePacket.icc);
        }
    }

    public static class UpdateClientDecoder implements Function<FriendlyByteBuf, UpdateClientPassengerPacket> {

        @Override
        public UpdateClientPassengerPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateClientPassengerPacket(packetBuffer.readInt(), packetBuffer.readNbt());
        }
    }

    public static class UpdateClientHandler implements BiConsumer<UpdateClientPassengerPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateClientPassengerPacket updateClientResourcePacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(updateClientResourcePacket.e);
                    if (entity instanceof LivingEntity) CombatData.getCap((LivingEntity) entity).read(updateClientResourcePacket.icc);
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
