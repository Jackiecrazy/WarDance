package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.client.screen.scroll.ScrollScreen;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenScrollScreenPacket {
    private static final ResourceLocation DUMMY = new ResourceLocation("wardance:thisisadummy");
    private Skill[] l;
    private boolean off;

    public OpenScrollScreenPacket(boolean offhand, Skill... list) {
        l = list;
        off = offhand;
    }

    public static class Encoder implements BiConsumer<OpenScrollScreenPacket, FriendlyByteBuf> {

        @Override
        public void accept(OpenScrollScreenPacket updateSkillPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeBoolean(updateSkillPacket.off);
            packetBuffer.writeInt(updateSkillPacket.l.length);
            for (Skill s : updateSkillPacket.l)
                if (s != null)
                    packetBuffer.writeResourceLocation(s.getRegistryName());
                else packetBuffer.writeResourceLocation(DUMMY);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, OpenScrollScreenPacket> {

        @Override
        public OpenScrollScreenPacket apply(FriendlyByteBuf packetBuffer) {
            boolean buffer = packetBuffer.readBoolean();
            int size = packetBuffer.readInt();
            Skill[] read = new Skill[size];
            for (int a = 0; a < size; a++) {
                read[a] = Skill.getSkill(packetBuffer.readResourceLocation());
            }
            return new OpenScrollScreenPacket(buffer, read);
        }
    }

    public static class Handler implements BiConsumer<OpenScrollScreenPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(OpenScrollScreenPacket updateSkillPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();

                //hard no go
                if (!PermissionData.getCap(sender).canEnterCombatMode()) {
                    return;
                }
                Minecraft.getInstance().setScreen(new ScrollScreen(updateSkillPacket.off, updateSkillPacket.l));
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
