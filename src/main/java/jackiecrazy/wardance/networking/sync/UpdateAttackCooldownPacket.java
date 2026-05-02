package jackiecrazy.wardance.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateAttackCooldownPacket {
    int e;
    int icc;

    public UpdateAttackCooldownPacket(int ent, int c) {
        e = ent;
        icc = c;
    }

    public static class Encoder implements BiConsumer<UpdateAttackCooldownPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateAttackCooldownPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.e);
            packetBuffer.writeInt(packet.icc);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateAttackCooldownPacket> {

        @Override
        public UpdateAttackCooldownPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateAttackCooldownPacket(packetBuffer.readInt(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<UpdateAttackCooldownPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAttackCooldownPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(packet.e) instanceof LivingEntity e)
                       e.attackStrengthTicker = packet.icc;
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
