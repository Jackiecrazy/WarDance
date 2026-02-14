package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RightClickPacket {
    boolean main;
    int id;

    public RightClickPacket(boolean isMainHand, int target) {
        main = isMainHand;
        id = target;
    }

    public RightClickPacket(boolean isMainHand, Entity target) {
        main = isMainHand;
        if (target == null) id = -1;
        else id = target.getId();
    }

    public static class Encoder implements BiConsumer<RightClickPacket, FriendlyByteBuf> {

        @Override
        public void accept(RightClickPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.id);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, RightClickPacket> {

        @Override
        public RightClickPacket apply(FriendlyByteBuf packetBuffer) {
            return new RightClickPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<RightClickPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RightClickPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = updateClientPacket.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                float cool = CombatUtils.getCooledAttackStrength(sender, h, 1f);

            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
