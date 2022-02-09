package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EvokeSkillPacket {

    public EvokeSkillPacket() {
    }

    public static class EvokeEncoder implements BiConsumer<EvokeSkillPacket, PacketBuffer> {

        @Override
        public void accept(EvokeSkillPacket updateClientPacket, PacketBuffer packetBuffer) {
        }
    }

    public static class EvokeDecoder implements Function<PacketBuffer, EvokeSkillPacket> {

        @Override
        public EvokeSkillPacket apply(PacketBuffer packetBuffer) {
            return new EvokeSkillPacket();
        }
    }

    public static class EvokeHandler implements BiConsumer<EvokeSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(EvokeSkillPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                final ISkillCapability cap = CasterData.getCap(contextSupplier.get().getSender());
                if (cap.getHolsteredSkill() != null)
                    cap.changeSkillState(cap.getHolsteredSkill(), Skill.STATE.ACTIVE);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
