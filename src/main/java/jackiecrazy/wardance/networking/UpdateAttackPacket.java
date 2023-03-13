package jackiecrazy.wardance.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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

    public static class UpdateAttackEncoder implements BiConsumer<UpdateAttackPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateAttackPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeInt(updateClientPacket.icc);
        }
    }

    public static class UpdateAttackDecoder implements Function<FriendlyByteBuf, UpdateAttackPacket> {

        @Override
        public UpdateAttackPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateAttackPacket(packetBuffer.readInt(), packetBuffer.readInt());
        }
    }

    public static class UpdateAttackHandler implements BiConsumer<UpdateAttackPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAttackPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(updateClientPacket.e) instanceof LivingEntity)
                        ((LivingEntity) (Minecraft.getInstance().level.getEntity(updateClientPacket.e))).attackStrengthTicker = updateClientPacket.icc;
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
