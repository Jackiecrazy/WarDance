package jackiecrazy.wardance.networking.meta;

import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.client.RenderUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenManualScreenPacket {
    private final boolean off;

    public OpenManualScreenPacket(boolean offhand) {
        off = offhand;
    }

    public static class Encoder implements BiConsumer<OpenManualScreenPacket, FriendlyByteBuf> {

        @Override
        public void accept(OpenManualScreenPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateSkillPacket.off);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, OpenManualScreenPacket> {

        @Override
        public OpenManualScreenPacket apply(FriendlyByteBuf packetBuffer) {
            boolean buffer = packetBuffer.readBoolean();
            return new OpenManualScreenPacket(buffer);
        }
    }

    public static class Handler implements BiConsumer<OpenManualScreenPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(OpenManualScreenPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();

                //hard no go
                if (!PermissionData.getCap(sender).canEnterCombatMode()) {
                    return;
                }
                RenderUtils.openManualScreen(updateSkillPacket.off);
            });
            contextSupplier.get().setPacketHandled(true);
        }

    }
}
