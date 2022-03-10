package jackiecrazy.wardance.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateTargetPacket {
    int m, t;

    public UpdateTargetPacket(int mob, int target) {
        m = mob;
        t = target;
    }

    public static class UpdateTargetEncoder implements BiConsumer<UpdateTargetPacket, PacketBuffer> {

        @Override
        public void accept(UpdateTargetPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.m);
            packetBuffer.writeInt(updateClientPacket.t);
        }
    }

    public static class UpdateTargetDecoder implements Function<PacketBuffer, UpdateTargetPacket> {

        @Override
        public UpdateTargetPacket apply(PacketBuffer packetBuffer) {
            return new UpdateTargetPacket(packetBuffer.readInt(), packetBuffer.readInt());
        }
    }

    public static class UpdateTargetHandler implements BiConsumer<UpdateTargetPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateTargetPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWorld world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity mob = world.getEntity(updateClientPacket.m);
                    Entity target = world.getEntity(updateClientPacket.t);
                    if (mob instanceof MobEntity) {
                        if (target instanceof LivingEntity)
                            ((MobEntity) mob).setTarget((LivingEntity) target);
                        else if(updateClientPacket.t==-1)
                            ((MobEntity) mob).setTarget(null);
                    }
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
