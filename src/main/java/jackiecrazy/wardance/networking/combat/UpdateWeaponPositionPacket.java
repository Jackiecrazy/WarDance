package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateWeaponPositionPacket {
    boolean main;
    Vec3 direction, offset;
    Vector4d renderOrientation;

    public UpdateWeaponPositionPacket(boolean isMainHand, MotionFrame mf) {
        main = isMainHand;
        direction = mf.direction();
        offset = mf.offset();
        renderOrientation = mf.renderOrientation();
    }

    public UpdateWeaponPositionPacket(boolean isMainHand,
                                      Vector3f dir, Vector3f off, Vector4d RO) {
        main = isMainHand;
        direction = new Vec3(dir);
        offset = new Vec3(off);
        renderOrientation = RO;
    }

    public static class Encoder implements BiConsumer<UpdateWeaponPositionPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateWeaponPositionPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);
            packetBuffer.writeVector3f(packet.direction.toVector3f());
            packetBuffer.writeFloat((float) packet.renderOrientation.x);
            packetBuffer.writeFloat((float) packet.renderOrientation.y);
            packetBuffer.writeFloat((float) packet.renderOrientation.z);
            packetBuffer.writeFloat((float) packet.renderOrientation.w);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateWeaponPositionPacket> {

        @Override
        public UpdateWeaponPositionPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateWeaponPositionPacket(packetBuffer.readBoolean(), packetBuffer.readVector3f(), packetBuffer.readVector3f(), new Vector4d(packetBuffer.readFloat(), packetBuffer.readFloat(), packetBuffer.readFloat(), packetBuffer.readFloat()));
        }
    }

    public static class Handler implements BiConsumer<UpdateWeaponPositionPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateWeaponPositionPacket packet,
                           Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null) {
                    //FlyingWeaponData.getCap(sender).(packet.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, fx.toArray(new FlyingWeaponEffect[fx.size()]));
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
