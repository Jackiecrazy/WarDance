package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncQuiverPacket;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SwapAttackPacket {
    boolean main;
    int nextSlot;

    public SwapAttackPacket(boolean isMainHand, int slot) {
        main = isMainHand;
        nextSlot = slot;
    }

    public static class Encoder implements BiConsumer<SwapAttackPacket, FriendlyByteBuf> {

        @Override
        public void accept(SwapAttackPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.nextSlot);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, SwapAttackPacket> {

        @Override
        public SwapAttackPacket apply(FriendlyByteBuf packetBuffer) {
            return new SwapAttackPacket(packetBuffer.readBoolean(), packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<SwapAttackPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SwapAttackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer p = contextSupplier.get().getSender();
                InteractionHand h = packet.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (p == null) return;
                if ((GeneralConfig.dual || packet.main)) {
                    //stuff the old one somewhere
                    final ItemStack nextItem = p.getEnderChestInventory().removeItem(packet.nextSlot, 999);

                    //this needs special handling
                    WeaponStats.AttackType s = WeaponStats.AttackType.DRAW_ATTACK;
                    WeaponInteractions.InteractionGroup group = WeaponStats.getSweepInfo(nextItem, p, s, false);
                    if (CombatUtils.getCooledAttackStrength(p, h, 1f) < group.getMinimumCooldown()){
                        p.getEnderChestInventory().setItem(packet.nextSlot,nextItem);
                        return;
                    }

                    if (p.getEnderChestInventory().addItem(p.getItemInHand(h)).isEmpty()) {
                        StylishData.getCap(p).addCombo(0.1f, "swap");
                        p.setItemInHand(h, nextItem);
                        FlyingWeaponData.getCap(p).forceRefreshWeapons();
                        CombatUtils.setAttackType(p, WeaponStats.AttackType.DRAW_ATTACK);
                        CombatUtils.processWeaponInteraction(p, null, h, GeneralUtils.getAttributeValueSafe(p, ForgeMod.ENTITY_REACH.get()));
                    } else p.getEnderChestInventory().addItem(nextItem);
                    CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncQuiverPacket(p));
                }
                CombatUtils.setHandCooldown(p, h, 0, true);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
