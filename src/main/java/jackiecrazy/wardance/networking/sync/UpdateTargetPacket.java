package jackiecrazy.wardance.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateTargetPacket {
    int m, t;

    public UpdateTargetPacket(int mob, int target) {
        m = mob;
        t = target;
    }

    public static class Encoder implements BiConsumer<UpdateTargetPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateTargetPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.m);
            packetBuffer.writeInt(packet.t);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateTargetPacket> {

        @Override
        public UpdateTargetPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateTargetPacket(packetBuffer.readInt(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<UpdateTargetPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateTargetPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity mob = world.getEntity(packet.m);
                    Entity target = world.getEntity(packet.t);
                    if (mob instanceof Mob) {
                        if (target instanceof LivingEntity)
                            ((Mob) mob).setTarget((LivingEntity) target);
                        else if(packet.t==-1)
                            ((Mob) mob).setTarget(null);
                    }
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
