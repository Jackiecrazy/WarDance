package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.handlers.EntityHandler;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class MixinSoundAlert {

    @Shadow
    public abstract ServerWorld getLevel();

    @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At("TAIL"))
    private void alert(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, CallbackInfo ci) {
        if (StealthUtils.soundMap.containsKey(soundIn))
            EntityHandler.alertTracker.put(new Tuple<>(this.getLevel(), new BlockPos(x, y, z)), (float) (StealthUtils.soundMap.get(soundIn)));
    }

    @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At("TAIL"))
    private void alertMoving(PlayerEntity player, Entity entityIn, SoundEvent eventIn, SoundCategory category, float volume, float pitch, CallbackInfo ci) {
        double x = entityIn.getX(), y = entityIn.getY(), z = entityIn.getZ();
        if (StealthUtils.soundMap.containsKey(eventIn))
            EntityHandler.alertTracker.put(new Tuple<>(this.getLevel(), new BlockPos(x, y, z)), (float) (StealthUtils.soundMap.get(eventIn)));
    }

}
