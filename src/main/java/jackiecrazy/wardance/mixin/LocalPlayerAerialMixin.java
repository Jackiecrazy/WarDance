package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.ClientAerialHandler;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
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
        //self.setDeltaMovement(Vec3.ZERO);
        // ci.cancel();  // Prevent vanilla travel from overriding our velocity
    }


//    @Inject(method = "tick", at = @At("TAIL"))
//    private void forceWallCling(CallbackInfo ci) {
//        LocalPlayer player = (LocalPlayer) (Object) this;
//
//        IAerialMode cap = AerialModeData.getCap(player); // your cap
//        if (cap.getWallDir() == null) {
//            return;
//        }
//
//        Direction wallDir = cap.getWallDir();
//        Vec3 normal = Vec3.atLowerCornerOf(wallDir.getNormal()).normalize(); // outward from wall
//
//        Vec3 delta = player.getDeltaMovement();
//
//        // Kill any movement away from the wall
//        double outwardDot = delta.dot(normal);
//        if (outwardDot > 0.0) {
//            delta = delta.subtract(normal.scale(outwardDot));
//        }
//
//        // Full projection onto wall plane
//        delta = delta.subtract(normal.scale(delta.dot(normal)));
//
//        // Tiny constant push INTO the wall (this fights floating-point drift)
//        delta = delta.add(normal.scale(-0.012));
//
//        player.setDeltaMovement(delta);
//
//        // THIS IS THE CRITICAL PART — zero all player input for the next tick
//        player.xxa = 0.0F;
//        player.zza = 0.0F;
//        player.yya = 0.0F;   // also kills jump input
//    }

}
