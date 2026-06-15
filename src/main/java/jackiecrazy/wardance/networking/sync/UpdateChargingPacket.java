package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.capability.charging.ChargingData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateChargingPacket {
    int id;
    double spd;
    ItemStack i;

    public UpdateChargingPacket(int id, ItemStack is, double speed) {
        this.id=id;
        i=is;
        spd=speed;
    }

    public static class Encoder implements BiConsumer<UpdateChargingPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateChargingPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.id);
            packetBuffer.writeItemStack(packet.i, true);
            packetBuffer.writeDouble(packet.spd);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateChargingPacket> {

        @Override
        public UpdateChargingPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateChargingPacket(packetBuffer.readInt(),packetBuffer.readItem(),packetBuffer.readDouble());
        }
    }

    public static class Handler implements BiConsumer<UpdateChargingPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateChargingPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(packet.id) instanceof LivingEntity e)
                        ChargingData.getCap(e).alterSpeed(packet.i, packet.spd);
                });
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
