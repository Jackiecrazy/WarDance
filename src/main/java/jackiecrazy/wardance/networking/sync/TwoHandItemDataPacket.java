package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.config.TwohandingStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TwoHandItemDataPacket {
    private static final FriendlyByteBuf.Writer<Item> item = (f, item) -> f.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    private static final FriendlyByteBuf.Writer<Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> info = (f, info) -> f.writeMap(info, (ff, attribute) -> ff.writeResourceLocation(ForgeRegistries.ATTRIBUTES.getKey(attribute)), (ff, tuple) -> {
        ff.writeCollection(tuple.getA(), (fbb, am) -> {
            fbb.writeUUID(am.getId());
            fbb.writeDouble(am.getAmount());
            fbb.writeByte(am.getOperation().toValue());
        });
        ff.writeCollection(tuple.getB(), (fbb, am) -> {
            fbb.writeUUID(am.getId());
            fbb.writeDouble(am.getAmount());
            fbb.writeByte(am.getOperation().toValue());
        });
    });

    private static final FriendlyByteBuf.Reader<Item> ritem = friendlyByteBuf -> ForgeRegistries.ITEMS.getValue(friendlyByteBuf.readResourceLocation());
    private static final FriendlyByteBuf.Reader<Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> rinfo = (f) -> f.readMap((ff) -> ForgeRegistries.ATTRIBUTES.getValue(ff.readResourceLocation()), (ff) -> new Tuple<>(ff.readList((buf) -> new AttributeModifier(buf.readUUID(), "Two-Handed Tag Modifier", buf.readDouble(), AttributeModifier.Operation.fromValue(buf.readByte()))), ff.readList((buf) -> new AttributeModifier(buf.readUUID(), "Offhand Two-Handed Tag Modifier", buf.readDouble(), AttributeModifier.Operation.fromValue(buf.readByte())))));
    private final Map<Item, Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> map;

    public TwoHandItemDataPacket(Map<Item, Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> map) {
        this.map = map;
    }

    public static class Encoder implements BiConsumer<TwoHandItemDataPacket, FriendlyByteBuf> {

        @Override
        public void accept(TwoHandItemDataPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeMap(packet.map, item, info);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, TwoHandItemDataPacket> {

        @Override
        public TwoHandItemDataPacket apply(FriendlyByteBuf packetBuffer) {
            return new TwoHandItemDataPacket(packetBuffer.readMap(ritem, rinfo));
        }
    }

    public static class Handler implements BiConsumer<TwoHandItemDataPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(TwoHandItemDataPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {

            //prevent client overriding server
            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                contextSupplier.get().enqueueWork(() -> {
                    TwohandingStats.clientWeaponOverride(updateClientPacket.map);
                });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
