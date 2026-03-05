package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.ClientAerialHandler;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerAerialMixin {


    // Inject into travel for state handling. todo head? tail?
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void wallRunTravel(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        IAerialMode cap = AerialModeData.getCap(self);
        if (cap == null) return;

        ClientAerialHandler.handleWallRuns(self, cap);
        ClientAerialHandler.handleAirJumps(self);
        // ci.cancel();  // Prevent vanilla travel from overriding our velocity
    }

}
