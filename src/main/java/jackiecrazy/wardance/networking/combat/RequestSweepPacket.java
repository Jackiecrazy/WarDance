package jackiecrazy.wardance.networking.combat;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.TemporaryMoveTranslator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestSweepPacket {
    boolean main;
    int id;
    boolean finish;

    public RequestSweepPacket(boolean isMainHand, int ignoreID, boolean finisher) {
        main = isMainHand;
        id = ignoreID;
        finish = finisher;
    }

    public RequestSweepPacket(boolean isMainHand, Entity ignore, boolean finisher) {
        main = isMainHand;
        if (ignore == null) id = -1;
        else id = ignore.getId();
        finish = finisher;
    }

    public static class RequestSweepEncoder implements BiConsumer<RequestSweepPacket, FriendlyByteBuf> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
            packetBuffer.writeInt(updateClientPacket.id);
            packetBuffer.writeBoolean(updateClientPacket.finish);
        }
    }

    public static class RequestSweepDecoder implements Function<FriendlyByteBuf, RequestSweepPacket> {

        @Override
        public RequestSweepPacket apply(FriendlyByteBuf packetBuffer) {
            return new RequestSweepPacket(packetBuffer.readBoolean(), packetBuffer.readInt(), packetBuffer.readBoolean());
        }
    }

    public static class RequestSweepHandler implements BiConsumer<RequestSweepPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                InteractionHand h = updateClientPacket.main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                boolean shouldAttack = true;
                if (sender == null) return;
                float cool = CombatUtils.getCooledAttackStrength(sender, h, 1f);
                if ((GeneralConfig.dual || updateClientPacket.main) && cool >= 0.9f) {
                    //TODO move this finisher stuff somewhere else
                    if (!sender.hasEffect(MobEffects.BLINDNESS)) {
                        if (updateClientPacket.finish) {
                            shouldAttack = false;
                            if (!StylishData.getCap(sender).canTrigger() && !sender.getAbilities().instabuild) {
                                sender.displayClientMessage(Component.literal("Not enough Finisher Charge! Currently " + StylishData.getCap(sender).getTriggerBar()), true);
                                return;
                            }
                            if (!StylishData.getCap(sender).isCombatMode()) return;
                            if (CombatData.getCap(sender).getHandBind(h) > 0) return;//don't even try dude
                            StylishData.getCap(sender).resetTriggerBar();
                            WeaponStats.SWEEPSTATE s = CombatUtils.getSweepState(sender);
                            WeaponStats.SweepInfo info = WeaponStats.getSweepInfo(sender.getItemInHand(h), s);
                            TemporaryMoveTranslator.scheduleFinisher(sender, h, info);
                            CombatData.getCap(sender).setSpirit(CombatData.getCap(sender).getMaxSpirit());
                        } else {
                            CombatUtils.sweep(sender, sender.level().getEntity(updateClientPacket.id), h, GeneralUtils.getAttributeValueSafe(sender, ForgeMod.ENTITY_REACH.get()));
                        }
                    }
                }
                //main attack. Temporary place to drop it.
                Entity e = sender.level().getEntity(updateClientPacket.id);
                if (e != null && shouldAttack) {
                    CombatUtils.setHandCooldown(sender, h, cool, false);
                    CombatUtils.attack(sender, e, h == InteractionHand.OFF_HAND);
                }
                CombatUtils.setHandCooldown(sender, h, 0, true);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
