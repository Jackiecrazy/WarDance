package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.QiCosts;
import jackiecrazy.wardance.handlers.EntityHandler;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class KickPacket {
    private int mob;

    public KickPacket(int id) {
        mob=id;
    }

    public static class Encoder implements BiConsumer<KickPacket, FriendlyByteBuf> {

        @Override
        public void accept(KickPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.mob);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, KickPacket> {

        @Override
        public KickPacket apply(FriendlyByteBuf packetBuffer) {
            return new KickPacket(packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<KickPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(KickPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null) {
                    Entity target=sender.level().getEntity(updateClientPacket.mob);
                    if (FlyingWeaponData.getCap(sender).getHeldBlock() != null) {
                        HitResult destination = ProjectileUtil.getHitResultOnViewVector(sender, EntitySelector.LIVING_ENTITY_STILL_ALIVE, 32);
                        Vec3 loc = destination.getLocation();
                        if (destination.getType() == HitResult.Type.ENTITY) {
                            loc = GeneralUtils.getExactCollision(((EntityHitResult) destination).getEntity(), sender.getEyePosition(), sender.getEyePosition().add(sender.getLookAngle().scale(32)));
                        }else if (target!=null){
                            loc=target.getEyePosition();
                        }
                        FlyingWeaponData.getCap(sender).yeet(null, loc);
                    } else {
                        HitResult destination = ProjectileUtil.getHitResultOnViewVector(sender, EntitySelector.LIVING_ENTITY_STILL_ALIVE, 3);
                        if (destination instanceof EntityHitResult hit && CombatData.getCap(sender).consumePosture(QiCosts.KICK) == 0) {
                            CombatUtils.kick(sender, hit.getEntity(), false);
                        }else if(target instanceof LivingEntity e&&e.distanceToSqr(sender)<9){
                            CombatUtils.kick(sender, e, false);
                        }
                    }
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
