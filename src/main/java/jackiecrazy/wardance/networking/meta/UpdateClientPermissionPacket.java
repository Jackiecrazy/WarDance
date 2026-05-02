package jackiecrazy.wardance.networking.meta;

import jackiecrazy.wardance.capability.permission.PermissionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateClientPermissionPacket {
    int e;
    CompoundTag icc;

    public UpdateClientPermissionPacket(int ent, CompoundTag c) {
        e = ent;
        icc = c;
    }

    public static class Encoder implements BiConsumer<UpdateClientPermissionPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateClientPermissionPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.e);
            packetBuffer.writeNbt(packet.icc);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateClientPermissionPacket> {

        @Override
        public UpdateClientPermissionPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateClientPermissionPacket(packetBuffer.readInt(), packetBuffer.readNbt());
        }
    }

    public static class Handler implements BiConsumer<UpdateClientPermissionPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateClientPermissionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(packet.e);
                    if (entity instanceof Player p) PermissionData.getCap(p).read(packet.icc);
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
