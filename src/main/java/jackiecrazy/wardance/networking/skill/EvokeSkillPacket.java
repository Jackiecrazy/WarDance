package jackiecrazy.wardance.networking.skill;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EvokeSkillPacket {
    private int coyote;

    public EvokeSkillPacket(int tempMob) {
        coyote=tempMob;
    }

    public static class Encoder implements BiConsumer<EvokeSkillPacket, FriendlyByteBuf> {

        @Override
        public void accept(EvokeSkillPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.coyote);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, EvokeSkillPacket> {

        @Override
        public EvokeSkillPacket apply(FriendlyByteBuf packetBuffer) {
            return new EvokeSkillPacket(packetBuffer.readInt());
        }
    }

    public static class Handler implements BiConsumer<EvokeSkillPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(EvokeSkillPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                final ISkillCapability cap = CasterData.getCap(contextSupplier.get().getSender());
                SkillUtils.temp=contextSupplier.get().getSender().level().getEntity(updateClientPacket.coyote);
                //comment area is redundant due to the same check in skillcapability
                if (cap.getHolsteredSkill() != null)// && cap.getSkillState(cap.getHolsteredSkill()) == Skill.STATE.HOLSTERED)
                    cap.changeSkillState(cap.getHolsteredSkill(), Skill.STATE.ACTIVE);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
