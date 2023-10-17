package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.items.ManualItem;
import jackiecrazy.wardance.items.WarItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManualizePacket {
    public ManualizePacket() {
    }

    public static class Encoder implements BiConsumer<ManualizePacket, FriendlyByteBuf> {

        @Override
        public void accept(ManualizePacket updateClientPacket, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, ManualizePacket> {

        @Override
        public ManualizePacket apply(FriendlyByteBuf packetBuffer) {
            return new ManualizePacket();
        }
    }

    public static class Handler implements BiConsumer<ManualizePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ManualizePacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                Player player = contextSupplier.get().getSender();
                if (player.getMainHandItem().getItem() != Items.WRITTEN_BOOK)
                    return;
                ItemStack give = new ItemStack(WarItems.MANUAL.get());
                give.setTag(player.getMainHandItem().getOrCreateTag().copy());
                ManualItem.setSkill(give, CasterData.getCap(player).getEquippedSkillsAndStyle());
                ManualItem.setAutoLearn(give, true);
                if (!player.getAbilities().instabuild)
                    player.getMainHandItem().shrink(1);
                player.addItem(give);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
