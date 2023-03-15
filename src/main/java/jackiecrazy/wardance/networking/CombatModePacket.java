package jackiecrazy.wardance.networking;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CombatModePacket {
    public CombatModePacket() {
    }

    public static class CombatEncoder implements BiConsumer<CombatModePacket, FriendlyByteBuf> {

        @Override
        public void accept(CombatModePacket updateClientPacket, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class CombatDecoder implements Function<FriendlyByteBuf, CombatModePacket> {

        @Override
        public CombatModePacket apply(FriendlyByteBuf packetBuffer) {
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
