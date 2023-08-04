package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.WeaponStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
    private static final FriendlyByteBuf.Writer<WeaponStats.MeleeInfo> info = (f, info) -> info.write(f);

    private static final FriendlyByteBuf.Reader<TagKey<Item>> ritem = f -> ItemTags.create(new ResourceLocation(WarDance.MODID, f.readResourceLocation().getPath()));;
    private static final FriendlyByteBuf.Reader<WeaponStats.MeleeInfo> rinfo = WeaponStats.MeleeInfo::read;
    private final Map<TagKey<Item>, WeaponStats.MeleeInfo> map;

    public SyncTagDataPacket(Map<TagKey<Item>, WeaponStats.MeleeInfo> map) {
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
            if (contextSupplier.get().getDirection() == NetworkDirection.LOGIN_TO_CLIENT)
                contextSupplier.get().enqueueWork(() -> {
                    WeaponStats.clientTagOverride(updateClientPacket.map);
                });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
