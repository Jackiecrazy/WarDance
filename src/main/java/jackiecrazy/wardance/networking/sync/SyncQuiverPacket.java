package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.hud.QuiverDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncQuiverPacket {
    CompoundTag icc;

    public SyncQuiverPacket(Player p) {

        icc=QuiverData.getData(p).serializeNBT();
    }

    public SyncQuiverPacket(CompoundTag p) {

        icc=p;
    }

    public static class Encoder implements BiConsumer<SyncQuiverPacket, FriendlyByteBuf> {

        @Override
        public void accept(SyncQuiverPacket pkt, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeNbt(pkt.icc);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, SyncQuiverPacket> {

        @Override
        public SyncQuiverPacket apply(FriendlyByteBuf packetBuffer) {
            return new SyncQuiverPacket(packetBuffer.readNbt());
        }
    }

    public static class Handler implements BiConsumer<SyncQuiverPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncQuiverPacket SyncSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
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
                    QuiverData.getData(player).deserializeNBT(icc);
                    //QuiverDisplay.refreshInventory(player, list);
                }
            };
        }
    }
}
