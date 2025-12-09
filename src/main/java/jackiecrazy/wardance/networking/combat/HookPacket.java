package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
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
    int targetID;

    public HookPacket(boolean isMainHand, Entity target) {
        main = isMainHand;
        targetID = target.getId();
    }

    public HookPacket(boolean isMainHand, int target) {
        main = isMainHand;
        targetID = target;
    }

    public static class Encoder implements BiConsumer<HookPacket, FriendlyByteBuf> {

        @Override
        public void accept(HookPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);
            packetBuffer.writeInt(packet.targetID);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, HookPacket> {

        @Override
        public HookPacket apply(FriendlyByteBuf packetBuffer) {
            return new HookPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<HookPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(HookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = packet.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (sender == null) return;
                Entity tgt = sender.level().getEntity(packet.targetID);
                //hook to weapon
                if (tgt instanceof FlyingWeaponEntity fwe) {
                    fwe.setTetheringEntity(sender);
                } else
                    FlyingWeaponData.getCap(sender).yeet(h, sender.getEyePosition().add(sender.getLookAngle().scale(10)));
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
