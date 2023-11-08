package jackiecrazy.wardance.networking.sync;

import jackiecrazy.wardance.config.TwohandingStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TwoHandTagDataPacket {
    private static final FriendlyByteBuf.Writer<TagKey<Item>> item = (f, item) -> f.writeResourceLocation(item.location());
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

    private static final FriendlyByteBuf.Reader<TagKey<Item>> ritem = f -> ItemTags.create(f.readResourceLocation());;
    private static final FriendlyByteBuf.Reader<Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> rinfo = (f) -> f.readMap((ff) -> ForgeRegistries.ATTRIBUTES.getValue(ff.readResourceLocation()), (ff) -> new Tuple<>(ff.readList((buf) -> new AttributeModifier(buf.readUUID(), "Two-Handed Tag Modifier", buf.readDouble(), AttributeModifier.Operation.fromValue(buf.readByte()))), ff.readList((buf) -> new AttributeModifier(buf.readUUID(), "Offhand Two-Handed Tag Modifier", buf.readDouble(), AttributeModifier.Operation.fromValue(buf.readByte())))));
    private final Map<TagKey<Item>,Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> map;

    public TwoHandTagDataPacket(Map<TagKey<Item>,Map<Attribute, Tuple<List<AttributeModifier>, List<AttributeModifier>>>> map) {
        this.map = map;
    }

    public static class Encoder implements BiConsumer<TwoHandTagDataPacket, FriendlyByteBuf> {

        @Override
        public void accept(TwoHandTagDataPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeMap(packet.map, item, info);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, TwoHandTagDataPacket> {

        @Override
        public TwoHandTagDataPacket apply(FriendlyByteBuf packetBuffer) {
            return new TwoHandTagDataPacket(packetBuffer.readMap(ritem, rinfo));
        }
    }

    public static class Handler implements BiConsumer<TwoHandTagDataPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(TwoHandTagDataPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {

            //prevent client overriding server
            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                contextSupplier.get().enqueueWork(() -> {
                    TwohandingStats.clientTagOverride(updateClientPacket.map);
                });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
