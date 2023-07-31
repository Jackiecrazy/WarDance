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

public class UpdateClientResourcePacket {
    int e;
    CompoundTag icc;

    public UpdateClientResourcePacket(int ent, CompoundTag c) {
        e = ent;
        icc = c;
    }

    public static class UpdateClientEncoder implements BiConsumer<UpdateClientResourcePacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateClientResourcePacket updateClientResourcePacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientResourcePacket.e);
            packetBuffer.writeNbt(updateClientResourcePacket.icc);
        }
    }

    public static class UpdateClientDecoder implements Function<FriendlyByteBuf, UpdateClientResourcePacket> {

        @Override
        public UpdateClientResourcePacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateClientResourcePacket(packetBuffer.readInt(), packetBuffer.readNbt());
        }
    }

    public static class UpdateClientHandler implements BiConsumer<UpdateClientResourcePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateClientResourcePacket updateClientResourcePacket, Supplier<NetworkEvent.Context> contextSupplier) {
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
