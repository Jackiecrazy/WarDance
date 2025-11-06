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

public class FinisherPacket {
    boolean main;

    public FinisherPacket(boolean isMainHand) {
        main = isMainHand;
    }

    public static class Encoder implements BiConsumer<FinisherPacket, FriendlyByteBuf> {

        @Override
        public void accept(FinisherPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, FinisherPacket> {

        @Override
        public FinisherPacket apply(FriendlyByteBuf packetBuffer) {
            return new FinisherPacket(packetBuffer.readBoolean());
        }
    }

    public static class Handler implements BiConsumer<FinisherPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(FinisherPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = updateClientPacket.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                CombatUtils.setHandCooldown(sender, h, 2, false);
                CombatUtils.scheduleFinisher(sender, h);
                CombatUtils.setHandCooldown(sender, h, 0, true);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
