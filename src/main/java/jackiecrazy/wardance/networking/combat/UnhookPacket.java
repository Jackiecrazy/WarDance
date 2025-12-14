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

public class UnhookPacket {

    public UnhookPacket() {
    }

    public static class Encoder implements BiConsumer<UnhookPacket, FriendlyByteBuf> {

        @Override
        public void accept(UnhookPacket packet, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UnhookPacket> {

        @Override
        public UnhookPacket apply(FriendlyByteBuf packetBuffer) {
            return new UnhookPacket();
        }
    }

    public static class Handler implements BiConsumer<UnhookPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UnhookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender == null) return;
                //have a weapon, yeet!
                final IFlyingWeapon cap = FlyingWeaponData.getCap(sender);
                sender.resetFallDistance();
                if (cap.getGrapple() != null) {
                    cap.getGrapple().rappel();
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
