package jackiecrazy.wardance.networking.movement;

import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.flyingweapon.IFlyingWeapon;
import jackiecrazy.wardance.entity.GrappleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UnhookPacket {
    private final GrappleEntity.ACTION act;

    public UnhookPacket(GrappleEntity.ACTION a) {
        act=a;
    }

    public static class Encoder implements BiConsumer<UnhookPacket, FriendlyByteBuf> {

        @Override
        public void accept(UnhookPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.act.ordinal());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UnhookPacket> {

        @Override
        public UnhookPacket apply(FriendlyByteBuf packetBuffer) {
            return new UnhookPacket(GrappleEntity.ACTION.values()[packetBuffer.readInt()]);
        }
    }

    public static class Handler implements BiConsumer<UnhookPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UnhookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender == null) return;
                final IFlyingWeapon cap = FlyingWeaponData.getCap(sender);
                sender.resetFallDistance();
                if (cap.getGrapple() != null) {
                    cap.getGrapple().retract(packet.act);
                    //AerialModeData.getCap(sender).setState(IAerialMode.WallState.NONE);
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
