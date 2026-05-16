package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
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

public class UpdateFlyingWeaponPacket {
    CompoundTag icc;

    public UpdateFlyingWeaponPacket(Player p) {
        icc=FlyingWeaponData.getCap(p).write();
    }

    public UpdateFlyingWeaponPacket(CompoundTag p) {

        icc=p;
    }

    public static class Encoder implements BiConsumer<UpdateFlyingWeaponPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateFlyingWeaponPacket pkt, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeNbt(pkt.icc);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateFlyingWeaponPacket> {

        @Override
        public UpdateFlyingWeaponPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateFlyingWeaponPacket(packetBuffer.readNbt());
        }
    }

    public static class Handler implements BiConsumer<UpdateFlyingWeaponPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateFlyingWeaponPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Handle.handleClient(packet.icc));
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }

    public static class Handle {
        public static DistExecutor.SafeRunnable handleClient(CompoundTag icc) {
            return new DistExecutor.SafeRunnable() {
                @Override
                public void run() {
                    Player player = Minecraft.getInstance().player;
                    if (player == null) return;
                    FlyingWeaponData.getCap(player).read(player.level(), icc);
                }
            };
        }
    }
}
