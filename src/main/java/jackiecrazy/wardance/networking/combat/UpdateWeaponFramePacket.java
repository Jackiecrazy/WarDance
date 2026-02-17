package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateWeaponFramePacket {
    WeaponStats.AttackType ordinal;

    public UpdateWeaponFramePacket(WeaponStats.AttackType to) {
        ordinal = to;
    }

    public static class Encoder implements BiConsumer<UpdateWeaponFramePacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateWeaponFramePacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(packet.ordinal.ordinal());
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateWeaponFramePacket> {

        @Override
        public UpdateWeaponFramePacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateWeaponFramePacket(WeaponStats.AttackType.values()[packetBuffer.readInt()]);
        }
    }

    public static class Handler implements BiConsumer<UpdateWeaponFramePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateWeaponFramePacket packet,
                           Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null) {
                    for(InteractionHand h:InteractionHand.values()) {
                        WeaponStats.WeaponInfo wi = WeaponStats.lookupStats(sender.getItemInHand(h));
                        if (wi != null && FlyingWeaponData.getCap(sender).getWeapon(h) != null) {
                            MotionManager mm = wi.idle_frame();
                            switch (packet.ordinal) {
                                case GUARD_COUNTER -> mm = wi.guard_frame();
                                case THROW -> mm = wi.aim_frame();
                                case DRAW_ATTACK -> mm = wi.swap_frame();
                            }
                            FlyingWeaponData.getCap(sender).getWeapon(h).setIdlePose(mm);
                        }
                    }
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
