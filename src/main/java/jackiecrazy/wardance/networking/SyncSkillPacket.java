package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncSkillPacket {
    CompoundTag icc;

    public SyncSkillPacket(CompoundTag c) {
        icc = c;
    }

    public static class SyncSkillEncoder implements BiConsumer<SyncSkillPacket, FriendlyByteBuf> {

        @Override
        public void accept(SyncSkillPacket SyncSkillPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeNbt(SyncSkillPacket.icc);
        }
    }

    public static class SyncSkillDecoder implements Function<FriendlyByteBuf, SyncSkillPacket> {

        @Override
        public SyncSkillPacket apply(FriendlyByteBuf packetBuffer) {
            return new SyncSkillPacket(packetBuffer.readNbt());
        }
    }

    public static class SyncSkillHandler implements BiConsumer<SyncSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncSkillPacket SyncSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Handle.handleClient(SyncSkillPacket.icc));

            });
            contextSupplier.get().setPacketHandled(true);
        }
    }

    public static class Handle {
        public static DistExecutor.SafeRunnable handleClient(CompoundTag icc) {
            return new DistExecutor.SafeRunnable() {
                @Override
                public void run() {
                    Player player = (Player) Minecraft.getInstance().player;
                    if (player == null) return;
                    CasterData.getCap(player).read(icc);
                }
            };
        }
    }
}
