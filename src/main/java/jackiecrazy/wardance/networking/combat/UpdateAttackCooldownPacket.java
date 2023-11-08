package jackiecrazy.wardance.networking.combat;

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

    public static class UpdateAttackEncoder implements BiConsumer<UpdateAttackCooldownPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateAttackCooldownPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeInt(updateClientPacket.icc);
        }
    }

    public static class UpdateAttackDecoder implements Function<FriendlyByteBuf, UpdateAttackCooldownPacket> {

        @Override
        public UpdateAttackCooldownPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateAttackCooldownPacket(packetBuffer.readInt(), packetBuffer.readInt());
        }
    }

    public static class UpdateAttackHandler implements BiConsumer<UpdateAttackCooldownPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAttackCooldownPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(updateClientPacket.e) instanceof LivingEntity e)
                       e.attackStrengthTicker = updateClientPacket.icc;
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
