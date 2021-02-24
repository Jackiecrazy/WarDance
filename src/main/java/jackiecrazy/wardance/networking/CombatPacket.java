package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CombatPacket {
    public CombatPacket() {
    }

    public static class CombatEncoder implements BiConsumer<CombatPacket, PacketBuffer> {

        @Override
        public void accept(CombatPacket updateClientPacket, PacketBuffer packetBuffer) {
        }
    }

    public static class CombatDecoder implements Function<PacketBuffer, CombatPacket> {

        @Override
        public CombatPacket apply(PacketBuffer packetBuffer) {
            return new CombatPacket();
        }
    }

    public static class CombatHandler implements BiConsumer<CombatPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(CombatPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ICombatCapability cap = CombatData.getCap(Objects.requireNonNull(contextSupplier.get().getSender()));
                cap.toggleCombatMode(!cap.isCombatMode());
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
