package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.resources.CombatData;
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
        else id = entity.getId();
    }

    public static class Encoder implements BiConsumer<RequestAttackPacket, FriendlyByteBuf> {

        @Override
        public void accept(RequestAttackPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);
            packetBuffer.writeInt(packet.id);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, RequestAttackPacket> {

        @Override
        public RequestAttackPacket apply(FriendlyByteBuf packetBuffer) {
            return new RequestAttackPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<RequestAttackPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestAttackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null) {
                    Entity e = sender.level().getEntity(packet.id);
                    if (e != null && (GeneralConfig.dual || packet.main)) {
                        final double reach = GeneralUtils.getAttributeValueSafe(sender, ForgeMod.ENTITY_REACH.get());
                        if (GeneralUtils.getDistSqCompensated(sender, e) < reach * reach) {
                            if (!packet.main) {
                                if (CombatData.getCap(sender).getHandBind(InteractionHand.OFF_HAND) > 0)//no go
                                    return;
                                CombatUtils.swapHeldItems(sender);
                                CombatData.getCap(sender).setOffhandAttack(true);
                            }
                            if (sender.attackStrengthTicker > 0) {
                                int temp = sender.attackStrengthTicker;
                                CombatUtils.updateNormalAttackStatus(sender);
                                sender.attack(e);
                                sender.attackStrengthTicker = temp;
                            }
                            if (!packet.main) {
                                CombatUtils.swapHeldItems(sender);
                                CombatData.getCap(sender).setOffhandAttack(false);
                            }
                        }
                    }
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
