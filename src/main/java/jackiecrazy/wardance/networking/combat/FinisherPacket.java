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
    /*
    spitballin:

    sneaking both guards and charges a heavy attack.
    Charging consumes all of your heavy charges over 2 seconds,
    and you can input a move/attack command at any point to perform the heavy, consuming all points.
    The heavy has two states. If you are fully charged it will be a finisher, otherwise it will be a standard heavy attack.
    with the last attack always being capable of breaching.

    A bar on the screen tells you when you pass specific breakpoints for the next attack,
    and turns red when you reach the final phase.
    Any attack restores 1 spirit, the last attack refills all spirit.

    This requires several parts.
    mechanical: count sneaking time and track last swung hand (over 100 tick reset to main), on movement input if sneaking:
        if over threshold, send heavy packet to server
        otherwise do nothing
    on receive heavy packet:
        if charge time reaches threshold on packet indicated hand execute heavy for that hand, forward request to flying weapon cap
        reset all heavy charges
    client display:
        when sneaking read last swung hand and grab its list of heavy breakpoints,
        show them as a bar on the screen like the horse jump bar. The amount you can reach is green,
        if you have less than 10 heavy charge the inaccessible region is in black
        if you are above max heavy point the overflowing is in red to tell you to release
    json:
        each stage of heavy is represented as a list of actions.
        directly allow parsing a motionmanager as a swing action.
     */

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
