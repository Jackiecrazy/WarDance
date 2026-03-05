package jackiecrazy.wardance.networking.sync;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateWeaponRenderPacket {
    boolean main;
    boolean weapon, shadow, after, trail;

    public UpdateWeaponRenderPacket(boolean isMainHand, FlyingWeaponEffect... fx) {
        main = isMainHand;
        weapon = Arrays.stream(fx).anyMatch(a -> a == FlyingWeaponEffect.WEAPON);
        shadow = Arrays.stream(fx).anyMatch(a -> a == FlyingWeaponEffect.BIG_SHADOW);
        after = Arrays.stream(fx).anyMatch(a -> a == FlyingWeaponEffect.AFTERIMAGE);
        trail = Arrays.stream(fx).anyMatch(a -> a == FlyingWeaponEffect.TRAIL);
    }

    public UpdateWeaponRenderPacket(boolean isMainHand,
                                    boolean showWeapon,
                                    boolean showShadow,
                                    boolean showAfter,
                                    boolean showTrail) {
        main = isMainHand;
        weapon = showWeapon;
        shadow = showShadow;
        after = showAfter;
        trail = showTrail;
    }

    public static class Encoder implements BiConsumer<UpdateWeaponRenderPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateWeaponRenderPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);

            packetBuffer.writeBoolean(packet.weapon);
            packetBuffer.writeBoolean(packet.shadow);
            packetBuffer.writeBoolean(packet.after);
            packetBuffer.writeBoolean(packet.trail);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, UpdateWeaponRenderPacket> {

        @Override
        public UpdateWeaponRenderPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateWeaponRenderPacket(packetBuffer.readBoolean(), packetBuffer.readBoolean(), packetBuffer.readBoolean(), packetBuffer.readBoolean(), packetBuffer.readBoolean());
        }
    }

    public static class Handler implements BiConsumer<UpdateWeaponRenderPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateWeaponRenderPacket packet,
                           Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null) {
                    List<FlyingWeaponEffect> fx = new ArrayList<>();
                    if (packet.trail) fx.add(FlyingWeaponEffect.TRAIL);
                    if (packet.shadow) fx.add(FlyingWeaponEffect.BIG_SHADOW);
                    if (packet.weapon) fx.add(FlyingWeaponEffect.WEAPON);
                    if (packet.after) fx.add(FlyingWeaponEffect.AFTERIMAGE);
                    for (InteractionHand h : InteractionHand.values()) {
                        WeaponStats.WeaponInfo wi = WeaponStats.lookupStats(sender.getItemInHand(h));
                        if (wi != null && FlyingWeaponData.getCap(sender).getWeapon(h) != null) {
                            FlyingWeaponData.getCap(sender).setRender(h, fx.toArray(new FlyingWeaponEffect[fx.size()]));
                        }
                    }
                }});
                contextSupplier.get().setPacketHandled(true);
            }
        }
    }
