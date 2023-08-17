package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.items.ScrollItem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LearnScrollPacket {
    private static final ResourceLocation DUMMY = new ResourceLocation("wardance:thisisadummy");
    private Skill l;
    private boolean offhand;

    public LearnScrollPacket(Skill s, boolean offhand) {
        this.offhand = offhand;
        l = s;
    }

    public static class Encoder implements BiConsumer<LearnScrollPacket, FriendlyByteBuf> {

        @Override
        public void accept(LearnScrollPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeResourceLocation(updateSkillPacket.l.getRegistryName());
            packetBuffer.writeBoolean(updateSkillPacket.offhand);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, LearnScrollPacket> {

        @Override
        public LearnScrollPacket apply(FriendlyByteBuf packetBuffer) {
            return new LearnScrollPacket(Skill.getSkill(packetBuffer.readResourceLocation()), packetBuffer.readBoolean());
        }
    }

    public static class Handler implements BiConsumer<LearnScrollPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(LearnScrollPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                //hard no go
                if (!PermissionData.getCap(sender).canEnterCombatMode()) {
                    return;
                }
                if (sender != null) {
                    ScrollItem.learnSkill(sender, sender.getItemInHand(updateSkillPacket.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND), updateSkillPacket.l);
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
