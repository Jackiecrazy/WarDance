package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.handlers.EntityHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ShoutPacket {
    private final ResourceLocation voice;

    public ShoutPacket(ResourceLocation sound) {
        voice = sound;
    }

    public static class ShoutEncoder implements BiConsumer<ShoutPacket, PacketBuffer> {

        @Override
        public void accept(ShoutPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeResourceLocation(updateClientPacket.voice);
        }
    }

    public static class ShoutDecoder implements Function<PacketBuffer, ShoutPacket> {

        @Override
        public ShoutPacket apply(PacketBuffer packetBuffer) {
            return new ShoutPacket(packetBuffer.readResourceLocation());
        }
    }

    public static class ShoutHandler implements BiConsumer<ShoutPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ShoutPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                PlayerEntity uke = contextSupplier.get().getSender();
                SoundEvent se = ForgeRegistries.SOUND_EVENTS.getValue(updateClientPacket.voice);
                if (uke == null) return;
                if (se == null) se = SoundEvents.PILLAGER_AMBIENT;
                uke.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), se, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                EntityHandler.alertTracker.put(new Tuple<>(uke.level, new BlockPos(uke.getX(), uke.getY(), uke.getZ())), (float) StealthConfig.shout);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
