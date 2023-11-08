package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManualParryPacket {

    public ManualParryPacket() {
    }

    public static class ParryEncoder implements BiConsumer<ManualParryPacket, FriendlyByteBuf> {

        @Override
        public void accept(ManualParryPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class ParryDecoder implements Function<FriendlyByteBuf, ManualParryPacket> {

        @Override
        public ManualParryPacket apply(FriendlyByteBuf packetBuffer) {
            return new ManualParryPacket();
        }
    }

    public static class ParryHandler implements BiConsumer<ManualParryPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ManualParryPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                final ServerPlayer le = Objects.requireNonNull(contextSupplier.get().getSender());
                ICombatCapability cap = CombatData.getCap(le);
                if (CombatConfig.parryTime < 0) {
                    cap.setParryingTick(cap.getParryingTick()==-1?0:-1);
                }
                else if (cap.getParryingTick() + CombatConfig.parryTime < le.tickCount)
                    cap.setParryingTick(le.tickCount);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
