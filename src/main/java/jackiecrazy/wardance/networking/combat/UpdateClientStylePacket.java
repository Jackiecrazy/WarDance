package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.stylish.StylishData;
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

public class UpdateClientStylePacket {
    int e;
    CompoundTag icc;

    public UpdateClientStylePacket(int ent, CompoundTag c) {
        e = ent;
        icc = c;
    }

    public static class Encoder implements BiConsumer<UpdateClientStylePacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateClientStylePacket updateClientResourcePacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientResourcePacket.e);
            packetBuffer.writeNbt(updateClientResourcePacket.icc);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateClientStylePacket> {

        @Override
        public UpdateClientStylePacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateClientStylePacket(packetBuffer.readInt(), packetBuffer.readNbt());
        }
    }

    public static class Handler implements BiConsumer<UpdateClientStylePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateClientStylePacket updateClientResourcePacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(updateClientResourcePacket.e);
                    if (entity instanceof LivingEntity) StylishData.getCap((LivingEntity) entity).read(updateClientResourcePacket.icc);
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
