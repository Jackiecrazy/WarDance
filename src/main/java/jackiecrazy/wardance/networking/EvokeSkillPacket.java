package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EvokeSkillPacket {

    public EvokeSkillPacket() {
    }

    public static class EvokeEncoder implements BiConsumer<EvokeSkillPacket, FriendlyByteBuf> {

        @Override
        public void accept(EvokeSkillPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
        }
    }

    public static class EvokeDecoder implements Function<FriendlyByteBuf, EvokeSkillPacket> {

        @Override
        public EvokeSkillPacket apply(FriendlyByteBuf packetBuffer) {
            return new EvokeSkillPacket();
        }
    }

    public static class EvokeHandler implements BiConsumer<EvokeSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(EvokeSkillPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                final ISkillCapability cap = CasterData.getCap(contextSupplier.get().getSender());
                //comment area is redundant due to the same check in skillcapability
                if (cap.getHolsteredSkill() != null)// && cap.getSkillState(cap.getHolsteredSkill()) == Skill.STATE.HOLSTERED)
                    cap.changeSkillState(cap.getHolsteredSkill(), Skill.STATE.ACTIVE);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
