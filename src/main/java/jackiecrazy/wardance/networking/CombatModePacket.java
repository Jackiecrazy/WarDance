package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CombatModePacket {
    public CombatModePacket() {
    }

    public static class CombatEncoder implements BiConsumer<CombatModePacket, PacketBuffer> {

        @Override
        public void accept(CombatModePacket updateClientPacket, PacketBuffer packetBuffer) {
        }
    }

    public static class CombatDecoder implements Function<PacketBuffer, CombatModePacket> {

        @Override
        public CombatModePacket apply(PacketBuffer packetBuffer) {
            return new CombatModePacket();
        }
    }

    public static class CombatHandler implements BiConsumer<CombatModePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(CombatModePacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ICombatCapability cap = CombatData.getCap(Objects.requireNonNull(contextSupplier.get().getSender()));
                cap.toggleCombatMode(!cap.isCombatMode());
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
