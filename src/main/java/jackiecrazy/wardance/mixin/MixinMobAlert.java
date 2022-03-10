package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.handlers.EntityHandler;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MixinMobAlert {

    @Inject(method = "playHurtSound", at = @At("TAIL"))
    private void alert(DamageSource source, CallbackInfo ci) {
        MobEntity me = ((MobEntity) (Object) this);
        if (StealthUtils.stealthMap.getOrDefault(me.getType().getRegistryName(), StealthUtils.STEALTH).isQuiet())
            return;
        float volume = ((MixinMobSound) me).callGetSoundVolume();
        EntityHandler.alertTracker.put(new Tuple<>(me.level, new BlockPos(me.getX(), me.getY(), me.getZ())), (float) (volume * StealthConfig.blockPerVolume));
    }

}
