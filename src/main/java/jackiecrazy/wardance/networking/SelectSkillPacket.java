package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectSkillPacket {
    private final ResourceLocation sk;

    public SelectSkillPacket(ResourceLocation skill) {
        sk = skill;
    }

    public static class CombatEncoder implements BiConsumer<SelectSkillPacket, PacketBuffer> {

        @Override
        public void accept(SelectSkillPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeResourceLocation(updateClientPacket.sk);
        }
    }

    public static class CombatDecoder implements Function<PacketBuffer, SelectSkillPacket> {

        @Override
        public SelectSkillPacket apply(PacketBuffer packetBuffer) {
            return new SelectSkillPacket(packetBuffer.readResourceLocation());
        }
    }

    public static class CombatHandler implements BiConsumer<SelectSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SelectSkillPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                Skill s = GameRegistry.findRegistry(Skill.class).getValue(updateClientPacket.sk);
                if (s != null)
                    if (s.onSelected(contextSupplier.get().getSender()))
                        WarDance.LOGGER.debug("successfully selected " + updateClientPacket.sk);
                    else WarDance.LOGGER.debug("failed to select " + updateClientPacket.sk);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
