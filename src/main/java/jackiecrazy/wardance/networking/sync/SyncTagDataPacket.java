package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncTagDataPacket {
    private static final FriendlyByteBuf.Writer<TagKey<Item>> item = (f, item) -> f.writeResourceLocation(item.location());
    private static final FriendlyByteBuf.Writer<WeaponStats.WeaponInfo> info = (f, info) -> info.write(f);

    private static final FriendlyByteBuf.Reader<TagKey<Item>> ritem = f -> ItemTags.create(f.readResourceLocation());;
    private static final FriendlyByteBuf.Reader<WeaponStats.WeaponInfo> rinfo = WeaponStats.WeaponInfo::read;
    private final Map<TagKey<Item>, WeaponStats.WeaponInfo> map;

    public SyncTagDataPacket(Map<TagKey<Item>, WeaponStats.WeaponInfo> map) {
        this.map = map;
    }

    public static class Encoder implements BiConsumer<SyncTagDataPacket, FriendlyByteBuf> {

        @Override
        public void accept(SyncTagDataPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeMap(packet.map, item, info);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, SyncTagDataPacket> {

        @Override
        public SyncTagDataPacket apply(FriendlyByteBuf packetBuffer) {
            return new SyncTagDataPacket(packetBuffer.readMap(ritem, rinfo));
        }
    }

    public static class Handler implements BiConsumer<SyncTagDataPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncTagDataPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {

            //prevent client overriding server
            if (contextSupplier.get().getDirection() == NetworkDirection.LOGIN_TO_CLIENT||contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                contextSupplier.get().enqueueWork(() -> {
                    WeaponStats.clientTagOverride(updateClientPacket.map);
                });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
