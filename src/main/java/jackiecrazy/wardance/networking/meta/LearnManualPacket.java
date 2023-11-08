package jackiecrazy.wardance.networking.meta;

import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.items.ManualItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LearnManualPacket {
    private final boolean offhand;

    public LearnManualPacket(boolean offhand) {
        this.offhand = offhand;
    }

    public static class Encoder implements BiConsumer<LearnManualPacket, FriendlyByteBuf> {

        @Override
        public void accept(LearnManualPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateSkillPacket.offhand);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, LearnManualPacket> {

        @Override
        public LearnManualPacket apply(FriendlyByteBuf packetBuffer) {
            return new LearnManualPacket(packetBuffer.readBoolean());
        }
    }

    public static class Handler implements BiConsumer<LearnManualPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(LearnManualPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                //hard no go
                if (!PermissionData.getCap(sender).canEnterCombatMode()) {
                    return;
                }
                if (sender != null) {
                    ManualItem.learn(sender, sender.getItemInHand(updateSkillPacket.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
