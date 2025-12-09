package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.entity.WarEntities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HookPacket {
    boolean main;
    Vec3 destination;

    public HookPacket(boolean isMainHand, Vec3 pos) {
        main = isMainHand;
        destination = pos;
    }

    public static class Encoder implements BiConsumer<HookPacket, FriendlyByteBuf> {

        @Override
        public void accept(HookPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);
            packetBuffer.writeVector3f(packet.destination.toVector3f());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, HookPacket> {

        @Override
        public HookPacket apply(FriendlyByteBuf packetBuffer) {
            return new HookPacket(packetBuffer.readBoolean(), new Vec3(packetBuffer.readVector3f()));
        }
    }

    public static class Handler implements BiConsumer<HookPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(HookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = packet.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                //have a weapon, yeet!
                GrappleEntity grapple = new GrappleEntity(WarEntities.GRAPPLE.get(), sender.level());
                grapple.setOwner(sender);
                grapple.setTransitioning(false);
                grapple.setInteractionRange(1);
                grapple.setPosRaw(sender.getX(), sender.getY()+sender.getEyeHeight(), sender.getZ());
                grapple.setDeltaMovement(packet.destination.subtract(grapple.position()).normalize().scale(1.5));
                sender.level().addFreshEntity(grapple);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
