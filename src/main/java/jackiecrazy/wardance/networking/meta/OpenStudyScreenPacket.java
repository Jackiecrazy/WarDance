package jackiecrazy.wardance.networking.meta;

import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import jackiecrazy.wardance.client.screen.ponder.StudyTheBlade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenStudyScreenPacket {

    public OpenStudyScreenPacket() {
    }

    public static class Encoder implements BiConsumer<OpenStudyScreenPacket, FriendlyByteBuf> {

        @Override
        public void accept(OpenStudyScreenPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, OpenStudyScreenPacket> {

        @Override
        public OpenStudyScreenPacket apply(FriendlyByteBuf packetBuffer) {
            return new OpenStudyScreenPacket();
        }
    }

    public static class Handler implements BiConsumer<OpenStudyScreenPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(OpenStudyScreenPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sp = contextSupplier.get().getSender();
                sp.openMenu(new SimpleMenuProvider((id, inv, p) ->
                                                           new StudyTheBlade(id, inv, null),
                                                   Component.empty()));
            });
            contextSupplier.get().setPacketHandled(true);
        }

    }
}
