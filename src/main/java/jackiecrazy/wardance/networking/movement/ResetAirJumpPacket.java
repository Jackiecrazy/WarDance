package jackiecrazy.wardance.networking.movement;

import jackiecrazy.wardance.capability.aerial.ClientAerialHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResetAirJumpPacket {

    public ResetAirJumpPacket() {
    }

    public static class Encoder implements BiConsumer<ResetAirJumpPacket, FriendlyByteBuf> {

        @Override
        public void accept(ResetAirJumpPacket packet, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, ResetAirJumpPacket> {

        @Override
        public ResetAirJumpPacket apply(FriendlyByteBuf packetBuffer) {
            return new ResetAirJumpPacket();
        }
    }

    public static class Handler implements BiConsumer<ResetAirJumpPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ResetAirJumpPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, Handle::handleClient);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }

    public static class Handle {
        public static DistExecutor.SafeRunnable handleClient() {
            return new DistExecutor.SafeRunnable() {
                @Override
                public void run() {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player == null) return;
                    ClientAerialHandler.resetMultiJumps(player);
                }
            };
        }
    }
}
