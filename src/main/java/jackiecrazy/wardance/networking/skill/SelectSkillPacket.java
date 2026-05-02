package jackiecrazy.wardance.networking.skill;

import jackiecrazy.wardance.capability.skill.CasterData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectSkillPacket {
    private final int sk;

    public SelectSkillPacket(int index) {
        sk = index;
    }

    public static class Encoder implements BiConsumer<SelectSkillPacket, FriendlyByteBuf> {

        @Override
        public void accept(SelectSkillPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.sk);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, SelectSkillPacket> {

        @Override
        public SelectSkillPacket apply(FriendlyByteBuf packetBuffer) {
            return new SelectSkillPacket(packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<SelectSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SelectSkillPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                CasterData.getCap(contextSupplier.get().getSender()).holsterSkill(packet.sk);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
