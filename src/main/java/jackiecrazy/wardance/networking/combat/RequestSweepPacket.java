package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestSweepPacket {
    boolean main;
    int id;

    public RequestSweepPacket(boolean isMainHand, int ignoreID) {
        main = isMainHand;
        id = ignoreID;
    }

    public RequestSweepPacket(boolean isMainHand, Entity ignore) {
        main = isMainHand;
        if (ignore == null) id = -1;
        else id = ignore.getId();
    }

    public static class Encoder implements BiConsumer<RequestSweepPacket, FriendlyByteBuf> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.id);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, RequestSweepPacket> {

        @Override
        public RequestSweepPacket apply(FriendlyByteBuf packetBuffer) {
            return new RequestSweepPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<RequestSweepPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = updateClientPacket.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                float cool = CombatUtils.getCooledAttackStrength(sender, h, 1f);
                if ((GeneralConfig.dual || updateClientPacket.main)) {
                        CombatUtils.updateNormalAttackStatus(sender);
                        CombatUtils.processWeaponInteraction(sender, sender.level().getEntity(updateClientPacket.id), h, GeneralUtils.getAttributeValueSafe(sender, ForgeMod.ENTITY_REACH.get()));
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
