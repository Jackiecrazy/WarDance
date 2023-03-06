package jackiecrazy.wardance.networking;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManualParryPacket {

    public ManualParryPacket() {
    }

    public static class ParryEncoder implements BiConsumer<ManualParryPacket, PacketBuffer> {

        @Override
        public void accept(ManualParryPacket updateClientPacket, PacketBuffer packetBuffer) {
        }
    }

    public static class ParryDecoder implements Function<PacketBuffer, ManualParryPacket> {

        @Override
        public ManualParryPacket apply(PacketBuffer packetBuffer) {
            return new ManualParryPacket();
        }
    }

    public static class ParryHandler implements BiConsumer<ManualParryPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ManualParryPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                final ServerPlayerEntity le = Objects.requireNonNull(contextSupplier.get().getSender());
                ICombatCapability cap = CombatData.getCap(le);
                if (CombatConfig.parryTime < 0) {
                    cap.setParryingTick(cap.getParryingTick()==-1?0:-1);
                }
                else if (cap.getParryingTick() < le.tickCount + CombatConfig.parryTime * 2)
                    cap.setParryingTick(le.tickCount);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
