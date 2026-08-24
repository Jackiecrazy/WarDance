package jackiecrazy.wardance.networking.combat;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.flyingweapon.IFlyingWeapon;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThrowPacket {
    boolean main;
    Vec3 destination;
    int next;

    public ThrowPacket(boolean isMainHand, Vec3 pos, int next) {
        main = isMainHand;
        destination = pos;
        this.next = next;
    }

    public static class Encoder implements BiConsumer<ThrowPacket, FriendlyByteBuf> {

        @Override
        public void accept(ThrowPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(packet.main);
            packetBuffer.writeVector3f(packet.destination.toVector3f());
            packetBuffer.writeInt(packet.next);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, ThrowPacket> {

        @Override
        public ThrowPacket apply(FriendlyByteBuf packetBuffer) {
            return new ThrowPacket(packetBuffer.readBoolean(), new Vec3(packetBuffer.readVector3f()), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<ThrowPacket, Supplier<NetworkEvent.Context>> {

        private static boolean swapFromEnderChest(int packet, ServerPlayer player, InteractionHand h) {
            return QuiverData.getData(player).swapWithHand(player, h, packet);
        }

        @Override
        public void accept(ThrowPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer player = contextSupplier.get().getSender();
                InteractionHand h = packet.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (player == null) return;
                //have a weapon, yeet!
                final ItemStack held = player.getItemInHand(h);
//                if (!held.isEmpty()) {
                    WeaponInteractions.InteractionGroup ig = WeaponStats.getSweepInfo(held, player, WeaponStats.AttackState.THROW, false, null);

                    final IFlyingWeapon cap = FlyingWeaponData.getCap(player);
                    CombatUtils.throw_vec = packet.destination.subtract(player.getEyePosition()).normalize();
                    CombatUtils.setAttackType(player, WeaponStats.AttackState.THROW);
                    if (CombatUtils.processWeaponInteraction(player, null, h, player.getAttributeValue(ForgeMod.ENTITY_REACH.get()))) {
                        if ((player.getItemInHand(h).isEmpty()||ig.forceNextWeapon()||packet.next>=0) && swapFromEnderChest(packet.next, player, h))
                            cap.forceRefreshWeapons();
                    }
//                }
//                else if (!WeaponStats.DESPERATION.isEmpty() && CombatData.getCap(player).consumeSpirit(6)) {
//                    //desperation throw
//                    StylishData.getCap(player).addCombo(0.1f, "desperatethrow");
//                    Level level = player.level();
//                    GhostBlockEntity fwe = new GhostBlockEntity(WarEntities.FLYING_BLOCK.get(), level);
//                    Item desperate = WeaponStats.DESPERATION.get(WarDance.rand.nextInt(WeaponStats.DESPERATION.size()));
//                    fwe.setHeldItem(new ItemStack(desperate));
//                    fwe.setOwner(player);
//                    fwe.setPosRaw(player.getX(), player.getEyeY(), player.getZ());
//                    fwe.setInteractionRange(1);
//                    fwe.setState(FlyingItemEntity.STATE.THROW_NATURAL);
//                    fwe.yeet(packet.destination, 2);
//                    level.addFreshEntity(fwe);
//                    if (packet.next >= 0) {
//                        player.setItemInHand(h, player.getEnderChestInventory().removeItem(packet.next, 999));
//                        QuiverData.getData(player).sync(player);
//                    }
//                }
                QuiverData.getData(player).sync(player);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
