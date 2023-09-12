package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.WeaponStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncItemDataPacket {
    private static final FriendlyByteBuf.Writer<Item> item = (f, item) -> f.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    private static final FriendlyByteBuf.Writer<WeaponStats.MeleeInfo> info = (f, info) -> info.write(f);

    private static final FriendlyByteBuf.Reader<Item> ritem = friendlyByteBuf -> ForgeRegistries.ITEMS.getValue(friendlyByteBuf.readResourceLocation());
    private static final FriendlyByteBuf.Reader<WeaponStats.MeleeInfo> rinfo = WeaponStats.MeleeInfo::read;
    private final Map<Item, WeaponStats.MeleeInfo> map;

    public SyncItemDataPacket(Map<Item, WeaponStats.MeleeInfo> map) {
        this.map = map;
    }

    public static class Encoder implements BiConsumer<SyncItemDataPacket, FriendlyByteBuf> {

        @Override
        public void accept(SyncItemDataPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeMap(packet.map, item, info);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, SyncItemDataPacket> {

        @Override
        public SyncItemDataPacket apply(FriendlyByteBuf packetBuffer) {
            final Map<Item, WeaponStats.MeleeInfo> huh = packetBuffer.readMap(ritem, rinfo);
            return new SyncItemDataPacket(huh);
        }
    }

    public static class Handler implements BiConsumer<SyncItemDataPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncItemDataPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {

            //prevent client overriding server
            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT || contextSupplier.get().getDirection() == NetworkDirection.LOGIN_TO_CLIENT) {
                if (!updateClientPacket.map.keySet().isEmpty())
                    WarDance.LOGGER.debug("synced namespace with example ID: " + updateClientPacket.map.entrySet().stream().findFirst().get().getKey().getDescriptionId());
                contextSupplier.get().enqueueWork(() -> {
                    WeaponStats.clientWeaponOverride(updateClientPacket.map);
                });
            }
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
