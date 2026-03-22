package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateAerialPacket {
    IAerialMode.WallState st;

    public UpdateAerialPacket(IAerialMode.WallState state) {
        st=state;
    }

    public static class Encoder implements BiConsumer<UpdateAerialPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateAerialPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.st.ordinal());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateAerialPacket> {

        @Override
        public UpdateAerialPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateAerialPacket(IAerialMode.WallState.values()[packetBuffer.readInt()]);
        }
    }

    public static class Handler implements BiConsumer<UpdateAerialPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateAerialPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender == null) return;
                sender.resetFallDistance();
                AerialModeData.getCap(sender).alterGravity(40, 0.3);
                //AerialModeData.getCap(sender).setState(packet.st);
                sender.setDeltaMovement(Vec3.ZERO);
                AerialModeData.getCap(sender).noOffFor(10);
                //CombatData.getCap(sender).consumeSpirit(packet.amount);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
