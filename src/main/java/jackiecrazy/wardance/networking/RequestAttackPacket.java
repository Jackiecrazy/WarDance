package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
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

public class RequestAttackPacket {
    boolean main;
    int id;
    float cd;

    public RequestAttackPacket(boolean isMainHand, int entityID, float cooldown) {
        main = isMainHand;
        id = entityID;
    }

    public RequestAttackPacket(boolean isMainHand, Entity entity, float cooldown) {
        main = isMainHand;
        if (entity == null) id = -1;
        else id = entity.getEntityId();
        cd=cooldown;
    }

    public static class RequestAttackEncoder implements BiConsumer<RequestAttackPacket, PacketBuffer> {

        @Override
        public void accept(RequestAttackPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.id);
            packetBuffer.writeFloat(updateClientPacket.cd);
        }
    }

    public static class RequestAttackDecoder implements Function<PacketBuffer, RequestAttackPacket> {

        @Override
        public RequestAttackPacket apply(PacketBuffer packetBuffer) {
            return new RequestAttackPacket(packetBuffer.readBoolean(), packetBuffer.readInt(), packetBuffer.readFloat());
        }
    }

    public static class RequestAttackHandler implements BiConsumer<RequestAttackPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestAttackPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayerEntity sender = contextSupplier.get().getSender();
                if (sender != null) {
                    Entity e = sender.world.getEntityByID(updateClientPacket.id);
                    if (e != null && GeneralUtils.getDistSqCompensated(sender, e) < GeneralUtils.getAttributeValueSafe(sender, ForgeMod.REACH_DISTANCE.get()) * GeneralUtils.getAttributeValueSafe(sender, ForgeMod.REACH_DISTANCE.get())) {
                        if (!updateClientPacket.main) CombatUtils.swapHeldItems(sender);
                        CombatUtils.setHandCooldown(sender, Hand.MAIN_HAND, updateClientPacket.cd, false);
                        sender.attackTargetEntityWithCurrentItem(e);
                        if (!updateClientPacket.main) CombatUtils.swapHeldItems(sender);
                    }
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
