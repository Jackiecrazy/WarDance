package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponCapability;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThrowPacket {
    boolean main;
    Vec3 destination;

    public ThrowPacket(boolean isMainHand, Vec3 pos) {
        main = isMainHand;
        destination = pos;
    }

    public static class Encoder implements BiConsumer<ThrowPacket, FriendlyByteBuf> {

        @Override
        public void accept(ThrowPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);
            packetBuffer.writeVector3f(packet.destination.toVector3f());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, ThrowPacket> {

        @Override
        public ThrowPacket apply(FriendlyByteBuf packetBuffer) {
            return new ThrowPacket(packetBuffer.readBoolean(), new Vec3(packetBuffer.readVector3f()));
        }
    }

    public static class Handler implements BiConsumer<ThrowPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ThrowPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = packet.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                //have a weapon, yeet!
                if (!sender.getItemInHand(h).isEmpty())
                    FlyingWeaponData.getCap(sender).yeet(h, packet.destination);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
