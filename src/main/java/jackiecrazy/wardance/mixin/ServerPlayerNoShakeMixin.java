package jackiecrazy.wardance.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerNoShakeMixin extends Player {
    public ServerPlayerNoShakeMixin(Level p_250508_,
                                    BlockPos p_250289_,
                                    float p_251702_,
                                    GameProfile p_252153_) {
        super(p_250508_, p_250289_, p_251702_, p_252153_);
    }

    @Inject(method = "indicateDamage", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void bogusShakeValues(double pitch, double yaw, CallbackInfo ci) {
        //cancel damage shake by inputting invalid values and relying on moar mixins
        if(pitch==-99999d && yaw==-99999d){
            hurtDir=-99999;
        }
    }
}
