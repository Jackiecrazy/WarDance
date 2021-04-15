package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestAttackPacket {
    boolean main;
    int id;

    public RequestAttackPacket(boolean isMainHand, int entityID) {
        main = isMainHand;
        id = entityID;
    }

    public RequestAttackPacket(boolean isMainHand, Entity entity) {
        main = isMainHand;
        if (entity == null) id = -1;
        else id = entity.getEntityId();
    }

    public static class RequestAttackEncoder implements BiConsumer<RequestAttackPacket, PacketBuffer> {

        @Override
        public void accept(RequestAttackPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.id);
        }
    }

    public static class RequestAttackDecoder implements Function<PacketBuffer, RequestAttackPacket> {

        @Override
        public RequestAttackPacket apply(PacketBuffer packetBuffer) {
            return new RequestAttackPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
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
                        if (!updateClientPacket.main) {
                            CombatUtils.swapHeldItems(sender);
                            CombatData.getCap(sender).setOffhandAttack(true);
                        }
                        if (sender.ticksSinceLastSwing > 0) {
                            int temp = sender.ticksSinceLastSwing;
                            sender.attackTargetEntityWithCurrentItem(e);
                            sender.ticksSinceLastSwing = temp;
                        } if (!updateClientPacket.main) {
                            CombatUtils.swapHeldItems(sender);
                            CombatData.getCap(sender).setOffhandAttack(false);
                        }
                    }
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
