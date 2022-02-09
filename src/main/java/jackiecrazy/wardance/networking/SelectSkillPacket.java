package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectSkillPacket {
    private final int sk;

    public SelectSkillPacket(int index) {
        sk = index;
    }

    public static class CombatEncoder implements BiConsumer<SelectSkillPacket, PacketBuffer> {

        @Override
        public void accept(SelectSkillPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.sk);
        }
    }

    public static class CombatDecoder implements Function<PacketBuffer, SelectSkillPacket> {

        @Override
        public SelectSkillPacket apply(PacketBuffer packetBuffer) {
            return new SelectSkillPacket(packetBuffer.readInt());
        }
    }

    public static class CombatHandler implements BiConsumer<SelectSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SelectSkillPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                CasterData.getCap(contextSupplier.get().getSender()).holsterSkill(updateClientPacket.sk);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
