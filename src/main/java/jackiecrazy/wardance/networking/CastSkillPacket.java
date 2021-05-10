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

public class CastSkillPacket {
    private final ResourceLocation sk;

    public CastSkillPacket(ResourceLocation skill) {
        sk = skill;
    }

    public static class CombatEncoder implements BiConsumer<CastSkillPacket, PacketBuffer> {

        @Override
        public void accept(CastSkillPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeResourceLocation(updateClientPacket.sk);
        }
    }

    public static class CombatDecoder implements Function<PacketBuffer, CastSkillPacket> {

        @Override
        public CastSkillPacket apply(PacketBuffer packetBuffer) {
            return new CastSkillPacket(packetBuffer.readResourceLocation());
        }
    }

    public static class CombatHandler implements BiConsumer<CastSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(CastSkillPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                Skill s = GameRegistry.findRegistry(Skill.class).getValue(updateClientPacket.sk);
                if (s != null)
                    if (s.checkAndCast(contextSupplier.get().getSender()))
                        WarDance.LOGGER.debug("successfully cast " + updateClientPacket.sk);
                    else WarDance.LOGGER.debug("failed to cast " + updateClientPacket.sk);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
