package jackiecrazy.wardance.networking;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public static class RequestSweepEncoder implements BiConsumer<RequestSweepPacket, PacketBuffer> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.id);
        }
    }

    public static class RequestSweepDecoder implements Function<PacketBuffer, RequestSweepPacket> {

        @Override
        public RequestSweepPacket apply(PacketBuffer packetBuffer) {
            return new RequestSweepPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
        }
    }

    public static class RequestSweepHandler implements BiConsumer<RequestSweepPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayerEntity sender = contextSupplier.get().getSender();
                Hand h = updateClientPacket.main ? Hand.MAIN_HAND : Hand.OFF_HAND;
                if (sender != null && (GeneralConfig.dual || updateClientPacket.main) && CombatUtils.getCooledAttackStrength(sender, h, 1f) >= 0.9f) {
                    //TODO throw weapon
                    double d0 = sender.walkDist - sender.walkDistO;
                    if (!(sender.fallDistance > 0.0F && !sender.onClimbable() && !sender.isInWater() && !sender.hasEffect(Effects.BLINDNESS) && !sender.isPassenger()) && !sender.isSprinting() && sender.isOnGround() && d0 < (double) sender.getSpeed())
                        CombatUtils.sweep(sender, sender.level.getEntity(updateClientPacket.id), h, GeneralUtils.getAttributeValueSafe(sender, ForgeMod.REACH_DISTANCE.get()));
                }
                if (h == Hand.OFF_HAND)
                    CombatUtils.setHandCooldown(sender, h, 0, false);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
