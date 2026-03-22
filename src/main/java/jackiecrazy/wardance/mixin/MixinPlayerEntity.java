package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.AerialCapability;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.config.GeneralConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity {


    protected MixinPlayerEntity(EntityType<? extends LivingEntity> p_i48577_1_, Level p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Redirect(method = "attack", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDist:F", opcode = Opcodes.GETFIELD))
    private float noSweep(Player player) {
        if (GeneralConfig.betterSweep) return Float.MAX_VALUE;
        return walkDist;
    }

    @Inject(method = "isStayingOnGroundSurface", at = @At("RETURN"), cancellable = true)
    private void sticky(CallbackInfoReturnable<Boolean> cir){
        if(!cir.getReturnValue()&& AerialModeData.getCap(this).enforcedNoOff()) {
            cir.setReturnValue(true);
        }
    }
}
