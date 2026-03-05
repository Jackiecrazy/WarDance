package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.capability.aerial.AerialModeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateAirPacket {
    int e, time;
    double spd;

    public UpdateAirPacket(int ent, double c, int ticks) {
        e = ent;
        spd = c;
        time = ticks;
    }

    public static class Encoder implements BiConsumer<UpdateAirPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateAirPacket updateClientResourcePacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientResourcePacket.e);
            packetBuffer.writeDouble(updateClientResourcePacket.spd);
            packetBuffer.writeInt(updateClientResourcePacket.time);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateAirPacket> {

        @Override
        public UpdateAirPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateAirPacket(packetBuffer.readInt(), packetBuffer.readDouble(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<UpdateAirPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAirPacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(pkt.e);
                    if (entity != null) {
                        AerialModeData.getCap(entity).alterGravity(pkt.time, pkt.spd);
                    }
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
