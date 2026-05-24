package jackiecrazy.wardance.networking.meta;

import jackiecrazy.wardance.capability.permission.PermissionData;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import jackiecrazy.wardance.client.RenderUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenQuiverScreenPacket {

    public OpenQuiverScreenPacket() {
    }

    public static class Encoder implements BiConsumer<OpenQuiverScreenPacket, FriendlyByteBuf> {

        @Override
        public void accept(OpenQuiverScreenPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, OpenQuiverScreenPacket> {

        @Override
        public OpenQuiverScreenPacket apply(FriendlyByteBuf packetBuffer) {
            return new OpenQuiverScreenPacket();
        }
    }

    public static class Handler implements BiConsumer<OpenQuiverScreenPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(OpenQuiverScreenPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sp = contextSupplier.get().getSender();
                sp.openMenu(new SimpleMenuProvider((id, inv, p) ->
                                                           new QuiverMenu(id, inv, QuiverData.getData(sp)),
                                                   Component.empty()));
            });
            contextSupplier.get().setPacketHandled(true);
        }

    }
}
