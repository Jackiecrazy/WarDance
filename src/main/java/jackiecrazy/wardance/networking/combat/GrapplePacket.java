package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.flyingweapon.IFlyingWeapon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GrapplePacket {
    Vec3 destination;

    public GrapplePacket(Vec3 pos) {
        destination = pos;
    }

    public static class Encoder implements BiConsumer<GrapplePacket, FriendlyByteBuf> {

        @Override
        public void accept(GrapplePacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeVector3f(packet.destination.toVector3f());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, GrapplePacket> {

        @Override
        public GrapplePacket apply(FriendlyByteBuf packetBuffer) {
            return new GrapplePacket(new Vec3(packetBuffer.readVector3f()));
        }
    }

    public static class Handler implements BiConsumer<GrapplePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(GrapplePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender == null) return;
                //have a weapon, yeet!
                final IFlyingWeapon cap = FlyingWeaponData.getCap(sender);
                sender.resetFallDistance();
                if (cap.getGrapple() == null)
                    cap.launchGrapple(packet.destination);
                else {
                    //the player held q and pressed middle button. Pull.
                    cap.getGrapple().yank();
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
