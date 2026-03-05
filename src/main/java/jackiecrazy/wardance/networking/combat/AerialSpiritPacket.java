package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.flyingweapon.IFlyingWeapon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AerialSpiritPacket {
    float amount;

    public AerialSpiritPacket(float consumed) {
        amount=consumed;
    }

    public static class Encoder implements BiConsumer<AerialSpiritPacket, FriendlyByteBuf> {

        @Override
        public void accept(AerialSpiritPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeFloat(packet.amount);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, AerialSpiritPacket> {

        @Override
        public AerialSpiritPacket apply(FriendlyByteBuf packetBuffer) {
            return new AerialSpiritPacket(packetBuffer.readFloat());
        }
    }

    public static class Handler implements BiConsumer<AerialSpiritPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(AerialSpiritPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender == null) return;
                sender.resetFallDistance();
                //CombatData.getCap(sender).consumeSpirit(packet.amount);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
