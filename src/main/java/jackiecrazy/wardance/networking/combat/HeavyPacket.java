package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HeavyPacket {
    boolean main;
    WeaponStats.AttackType state;
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

    public HeavyPacket(boolean isMainHand, WeaponStats.AttackType movestate) {
        main = isMainHand;
        state=movestate;
    }

    public static class Encoder implements BiConsumer<HeavyPacket, FriendlyByteBuf> {

        @Override
        public void accept(HeavyPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.state.ordinal());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, HeavyPacket> {

        @Override
        public HeavyPacket apply(FriendlyByteBuf packetBuffer) {
            return new HeavyPacket(packetBuffer.readBoolean(), WeaponStats.AttackType.values()[packetBuffer.readInt()]);
        }
    }

    public static class Handler implements BiConsumer<HeavyPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(HeavyPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = updateClientPacket.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                CombatUtils.setAttackType(sender, updateClientPacket.state);
                CombatUtils.processWeaponInteraction(sender, null, h, sender.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
                //CombatUtils.scheduleFinisher(sender, h, WeaponStats.AttackType.STANDING);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
